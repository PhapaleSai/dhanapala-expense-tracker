package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.util.CurrencyFormat
import kotlin.random.Random

/**
 * Rule-based "chat" — no cloud LLM, no network calls. Extracts a rupee
 * amount from free text and compares it against the caller's actual
 * remaining budget, replying in the same Bhai persona used elsewhere.
 */
object DhanpalChatEngine {

    private val amountRegex = Regex(
        """(?:₹|rs\.?|inr)?\s*([\d,]+(?:\.\d+)?)\s*(k|thousand|hazar|lakhs?|lacs?|crores?|cr)?""",
        RegexOption.IGNORE_CASE
    )

    fun respond(userMessage: String, remaining: Double, language: RoastLanguage, random: Random = Random.Default): String {
        val amount = extractAmount(userMessage) ?: return genericResponse(language, random)
        return if (amount <= remaining) canAffordResponse(amount, remaining, language, random)
        else cannotAffordResponse(amount, remaining, language, random)
    }

    private fun extractAmount(text: String): Double? {
        val match = amountRegex.find(text) ?: return null
        val numberPart = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
        return numberPart * multiplierFor(match.groupValues[2])
    }

    private fun multiplierFor(word: String): Double = when (word.lowercase()) {
        "k", "thousand", "hazar" -> 1_000.0
        "lakh", "lakhs", "lac", "lacs" -> 100_000.0
        "crore", "crores", "cr" -> 10_000_000.0
        else -> 1.0
    }

    private fun canAffordResponse(amount: Double, remaining: Double, language: RoastLanguage, random: Random): String {
        val left = CurrencyFormat.rupees(remaining - amount)
        val pool = when (language) {
            RoastLanguage.EN -> listOf(
                "Yeah, you can afford that ✅ You'll have $left left 💰 — don't go celebrating with another purchase 😅",
                "Go for it 👍 Budget survives with $left to spare 🎉",
                "Technically yes 🤏 $left will remain, so don't push your luck right after 😏"
            )
            RoastLanguage.HI -> listOf(
                "Haan bhai, afford kar sakta hai ✅ $left bachega 💰 — usi khushi mein aur kuch mat khareed lena 😅",
                "Kar le 👍 budget bach jayega, $left reh jayega 🎉",
                "Technically haan 🤏 $left bachega, uske baad luck test mat karna 😏"
            )
            RoastLanguage.MR -> listOf(
                "Ho bhava, afford karu shaktos ✅ $left urel 💰 — tyach khushit ajun kahi ghenu naka 😅",
                "Kar 👍 budget vachel, $left urel 🎉",
                "Technically ho 🤏 $left urel, tyanantar luck test karu naka 😏"
            )
        }
        return pool.random(random)
    }

    private fun cannotAffordResponse(amount: Double, remaining: Double, language: RoastLanguage, random: Random): String {
        val short = CurrencyFormat.rupees(amount - remaining)
        val pool = when (language) {
            RoastLanguage.EN -> listOf(
                "Bro, no ❌ You're short by $short 💸 Maybe wait for salary 🙏",
                "Hard no 🚫 That's $short more than what's left in the budget 😬",
                "Wallet says absolutely not 🙅 you'd be $short in the hole 📉"
            )
            RoastLanguage.HI -> listOf(
                "Bhai, nahi ❌ $short kam pad raha hai 💸 Salary ka wait kar le 🙏",
                "Bilkul mana hai 🚫 budget se $short zyada hai yeh 😬",
                "Wallet keh raha hai bilkul nahi 🙅 $short minus mein chala jayega 📉"
            )
            RoastLanguage.MR -> listOf(
                "Bhava, nahi ❌ $short kami padtoy 💸 Pagarichi vaat bagh 🙏",
                "Ekdam nahi 🚫 budget peksha $short jasta ahe he 😬",
                "Wallet mhanto ekdam nahi 🙅 $short minus madhe jashil 📉"
            )
        }
        return pool.random(random)
    }

    private fun genericResponse(language: RoastLanguage, random: Random): String {
        val pool = when (language) {
            RoastLanguage.EN -> listOf(
                "Give me an amount, bro 🤔 — like 'can I afford 2000 shoes? 👟'",
                "Not sure what you're asking 🧐 Try mentioning a rupee amount 💰",
                "Bhai brain works best with numbers 🔢 Try again with an amount 🙌"
            )
            RoastLanguage.HI -> listOf(
                "Amount toh bata bhai 🤔 — jaise 'kya main 2000 ke shoes afford kar sakta hoon? 👟'",
                "Samajh nahi aaya 🧐 ek amount mention karke pooch 💰",
                "Bhai ka dimaag numbers pe chalta hai 🔢 amount ke saath dobara pooch 🙌"
            )
            RoastLanguage.MR -> listOf(
                "Amount sang bhava 🤔 — jasa 'mi 2000 che shoes afford karu shakto ka? 👟'",
                "Samajla nahi 🧐 ek amount mention karun vichar 💰",
                "Bhavacha dimag numbers var chalto 🔢 amount sobat punha vichar 🙌"
            )
        }
        return pool.random(random)
    }
}
