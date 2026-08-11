package com.phapalesai.dhanapala.domain

import kotlin.random.Random

/**
 * A rotating personal welcome line shown under the greeting, once per app
 * session (same stability pattern as MoneyJokes) -- warm, not a roast, meant
 * to make opening the app feel worth doing.
 */
object WelcomeMessages {

    private val en = listOf(
        "Your money, your rules -- let's check in.",
        "Every rupee's got a story today.",
        "Ready to outsmart your spending?",
        "Let's see what today's got in store.",
        "Small steps, big savings.",
        "Your wallet missed you.",
        "Today's a good day to save something.",
        "Let's keep that budget honest.",
        "One glance, zero surprises.",
        "Your money deserves five minutes of your time."
    )

    private val hi = listOf(
        "Aaj ka hisaab-kitaab dekhte hain.",
        "Bhai, paisa dekhne ka time ho gaya.",
        "Chalo dekhte hain wallet ka mood aaj kaisa hai.",
        "Ek nazar budget pe, phir chill maar.",
        "Aaj bhi bachat karne ka mauka hai.",
        "Tera paisa, tera control.",
        "Chal dekhte hain kitna bacha hai.",
        "Thoda dhyan, thoda bachat.",
        "Aaj ka din, thoda hisaab ho jaaye.",
        "Wallet bula raha hai, ek baar dekh le."
    )

    private val mr = listOf(
        "Aaj cha hishob baghuya.",
        "Tujha paisa, tujhach control.",
        "Chal baghuya budget cha mood kasa ahe.",
        "Thodi bachat, thoda dhyan.",
        "Aaj pan kahi tari vachva.",
        "Ek nazar taka, mag relax kar.",
        "Tuza wallet vaat baghtoy.",
        "Aajcha divas, thoda hishob karuya.",
        "Paisa baghayla vel dilas, chan kelas.",
        "Chal, aaj cha kharcha check karuya."
    )

    fun random(language: RoastLanguage, random: Random = Random.Default): String {
        val pool = when (language) {
            RoastLanguage.EN -> en
            RoastLanguage.HI -> hi
            RoastLanguage.MR -> mr
        }
        return pool.random(random)
    }
}
