package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.domain.RoastCategory.BETWEEN_50_75
import com.phapalesai.dhanapala.domain.RoastCategory.BETWEEN_75_90
import com.phapalesai.dhanapala.domain.RoastCategory.BETWEEN_90_100
import com.phapalesai.dhanapala.domain.RoastCategory.LARGE_SPEND
import com.phapalesai.dhanapala.domain.RoastCategory.MODERATE_SPEND
import com.phapalesai.dhanapala.domain.RoastCategory.OVER_BUDGET
import com.phapalesai.dhanapala.domain.RoastCategory.SMALL_SPEND
import com.phapalesai.dhanapala.domain.RoastCategory.UNDER_50
import com.phapalesai.dhanapala.domain.RoastCategory.ZERO_REMAINING
import com.phapalesai.dhanapala.domain.RoastLevel.MEDIUM
import com.phapalesai.dhanapala.domain.RoastLevel.MILD
import com.phapalesai.dhanapala.domain.RoastLevel.SAVAGE

/** Message pools for Bhai Mode: 3 roast levels x 3 languages x 9 spending/budget categories. Keep it playful, never aggressive or offensive. */
object BhaiMessages {

    private val en = mapOf(
        MILD to mapOf(
            SMALL_SPEND to listOf("That's fine, small expense.", "No worries, totally normal."),
            MODERATE_SPEND to listOf("Keep an eye on this one.", "A bit more than usual, that's okay."),
            LARGE_SPEND to listOf("That was a big one today — just flagging it.", "Bigger expense than usual, noted."),
            UNDER_50 to listOf("All good, budget's healthy.", "You're doing fine so far."),
            BETWEEN_50_75 to listOf("Half your budget's gone, just keep watching.", "Past the halfway mark now."),
            BETWEEN_75_90 to listOf("75% used, getting close to the limit.", "Budget's getting tight now."),
            BETWEEN_90_100 to listOf("Almost at the limit, be careful now.", "Just a little room left."),
            OVER_BUDGET to listOf("You've gone a little over budget this month.", "Budget's exceeded, worth a look."),
            ZERO_REMAINING to listOf("You're right at your budget limit now.", "Zero left — plan carefully from here.")
        ),
        MEDIUM to mapOf(
            SMALL_SPEND to listOf("Chill, small expense.", "No tension, that's normal spending."),
            MODERATE_SPEND to listOf("Bro, check your wallet once in a while 👀", "Money's leaving, you know that right? 😂"),
            LARGE_SPEND to listOf("Careful bro, money doesn't grow on trees 🌳💸", "That's salary money, not lottery winnings 💀"),
            UNDER_50 to listOf("You're in control, nice and steady 😎", "Budget's still safe."),
            BETWEEN_50_75 to listOf("Careful now, halfway through the budget 👀", "Half the budget's gone already."),
            BETWEEN_75_90 to listOf("Bro, 75% of the budget is gone 😭", "Half the month left and budget's in the ICU."),
            BETWEEN_90_100 to listOf("Bro please stop 😭", "Wallet's in the ICU 🚨"),
            OVER_BUDGET to listOf("Congratulations 🎉 you officially wrecked the budget.", "Budget: ₹%d\nYou: ₹%d\nApp: 🤡"),
            ZERO_REMAINING to listOf("₹0 remaining. The next transaction better be a CREDIT 💀", "Financial independence ❌\nFinancial emergency ✅")
        ),
        SAVAGE to mapOf(
            SMALL_SPEND to listOf("Bro really tracked a snack purchase 💀", "Congrats on the ₹50 flex 🙄"),
            MODERATE_SPEND to listOf("Wallet filed a missing person report 🚨", "Bro's UPI app needs a cooldown timer 😭"),
            LARGE_SPEND to listOf("Spending like you inherited a fortune 💀", "RBI didn't give you a personal treasury, bro 😂"),
            UNDER_50 to listOf("Look at you, financially responsible for once 😏", "Shockingly under control today 👀"),
            BETWEEN_50_75 to listOf("Halfway gone and it's not even month-end 😂", "Expenses are getting a little too interesting 😂"),
            BETWEEN_75_90 to listOf("Budget's on life support at this point 🚨", "Time to hit the brakes, seriously."),
            BETWEEN_90_100 to listOf("Wallet is on its deathbed 💀", "One more transaction and it's over 😭"),
            OVER_BUDGET to listOf("Budget has been obliterated 💀", "The accountant would cry looking at this 😂"),
            ZERO_REMAINING to listOf("Forget your UPI PIN for a while 😂", "Until next salary, it's God's mercy only 🙏")
        )
    )

    private val hi = mapOf(
        MILD to mapOf(
            SMALL_SPEND to listOf("Chalta hai bhai 😊", "Koi baat nahi, chota kharcha hai."),
            MODERATE_SPEND to listOf("Bhai thoda dhyan rakhna iss baar.", "Thoda zyada hai, par theek hai."),
            LARGE_SPEND to listOf("Bhai bada kharcha tha ye, dhyan rakhna.", "Thoda bada tha ye, but chalega."),
            UNDER_50 to listOf("Sab thik hai, budget safe hai.", "Abhi tak sab control mein hai."),
            BETWEEN_50_75 to listOf("Aadha budget gaya hai, dhyan rakhna.", "Half raste mein aa gaye ho."),
            BETWEEN_75_90 to listOf("75% budget gaya hai, dhyan se.", "Budget thoda tight ho raha hai."),
            BETWEEN_90_100 to listOf("Bas thoda hi bacha hai, sambhal ke.", "Limit ke bahut kareeb ho."),
            OVER_BUDGET to listOf("Iss mahine budget thoda cross ho gaya.", "Budget se thoda upar chale gaye ho."),
            ZERO_REMAINING to listOf("Budget ka limit aa gaya hai ab.", "Zero bacha hai — dhyan se chalna.")
        ),
        MEDIUM to mapOf(
            SMALL_SPEND to listOf("Chalta hai bhai 😌", "Itna toh banta hai 😂"),
            MODERATE_SPEND to listOf("Bhai... wallet ki taraf bhi dekh le kabhi 👀", "UPI button ko thoda rest de bhai."),
            LARGE_SPEND to listOf("BHAI DHYAN SE, PAISE PED PE NAHI AATE 🌳💸", "Bhai salary aayi hai, IPL contract nahi mila 💀"),
            UNDER_50 to listOf("Mast bhai, control mein hai 😎", "Budget abhi safe hai."),
            BETWEEN_50_75 to listOf("Thoda sambhal ke chal bhai 👀", "Aadha budget gaya hai bhai."),
            BETWEEN_75_90 to listOf("Bhai budget ka 75% uda diya 😭", "Aadha mahina baaki hai aur budget ICU mein hai."),
            BETWEEN_90_100 to listOf("BHAI BAS KAR 😭", "Wallet ICU mein hai 🚨"),
            OVER_BUDGET to listOf("Congratulations 🎉 Budget ki officially maa-behen kar di.", "Budget: ₹%d\nYou: ₹%d\nApp: 🤡"),
            ZERO_REMAINING to listOf("₹0 remaining. Ab agla transaction sirf CREDIT hona chahiye. 💀", "Financial independence ❌\nFinancial emergency ✅")
        ),
        SAVAGE to mapOf(
            SMALL_SPEND to listOf("Itne se paise ke liye bhi notification aa gaya bhai 💀", "Bhai itna chota kharcha, phone bhi sharma gaya 😂"),
            MODERATE_SPEND to listOf("Bhai UPI pin bhi thak gaya hoga ab 😭", "Wallet ne resignation letter likh diya 💀"),
            LARGE_SPEND to listOf("RBI ne tujhe personal account nahi diya hai bhai 😂", "Itna paisa uda raha hai jaise Ambani ka rishtedaar hai 💀"),
            UNDER_50 to listOf("Aaj toh bhai financially responsible ban gaya 😏", "Itna control? Kaun ho tum? 👀"),
            BETWEEN_50_75 to listOf("Aadha mahina bhi nahi hua, aadha budget gayab 😂", "Expenses kuch zyada interesting ho rahe hain 😂"),
            BETWEEN_75_90 to listOf("Budget ventilator pe hai bhai 🚨", "Ab thoda brake laga bhai."),
            BETWEEN_90_100 to listOf("Wallet ne aakhri saans le li hai bhai 💀", "Ek aur transaction aur khatam 😭"),
            OVER_BUDGET to listOf("Bhai accountant ko kya muh dikhayega? 😂", "Budget naam ki cheez ko officially uda diya 💀"),
            ZERO_REMAINING to listOf("Bhai ab UPI PIN bhool ja.", "Ab agle salary tak Bhagwan bharose 🙏")
        )
    )

    private val mr = mapOf(
        MILD to mapOf(
            SMALL_SPEND to listOf("Thik ahe re, lahan kharch ahech.", "Kahi problem nahi, evadhach ahe."),
            MODERATE_SPEND to listOf("Jara laksh de re ya kharchakade.", "Thoda jasta ahe, pan thik ahe."),
            LARGE_SPEND to listOf("Bhava mothha kharch hota ha, laksh theva.", "Jara mothha hota, pan chalel."),
            UNDER_50 to listOf("Sagla thik ahe, budget safe ahe.", "Ajun control madhe ahe sagla."),
            BETWEEN_50_75 to listOf("Ardha budget zala, laksh theva.", "Ardhya vatet aahes."),
            BETWEEN_75_90 to listOf("75% budget gela ahe, laksh theva.", "Budget jara tight hotoy."),
            BETWEEN_90_100 to listOf("Thoda ch shillak ahe, sambhal.", "Limit javal aali ahe."),
            OVER_BUDGET to listOf("Ya mahinyat budget jara jasta zala.", "Budget peksha jasta kharch zala ahe."),
            ZERO_REMAINING to listOf("Budgetchi limit aali ahe ata.", "Zero shillak ahe — sambhalun raha.")
        ),
        MEDIUM to mapOf(
            SMALL_SPEND to listOf("Chalte re bhava 😌", "Itka tar banaticha 😂"),
            MODERATE_SPEND to listOf("Bhava... wallet kade pan bagh kadhi 👀", "UPI la zara rest de re."),
            LARGE_SPEND to listOf("Bhava paise zhadavar nahi ugat 🌳💸", "Pagar ali re, IPL contract nahi 💀"),
            UNDER_50 to listOf("Mast re, control madhe ahe 😎", "Budget ajun safe ahe."),
            BETWEEN_50_75 to listOf("Jara sambhal re bhava 👀", "Ardha budget gela re."),
            BETWEEN_75_90 to listOf("Bhava budgetcha 75% udhla 😭", "Ardha mahina baki ahe ani budget ICU madhe ahe."),
            BETWEEN_90_100 to listOf("BHAVA BAS KAR 😭", "Wallet ICU madhe ahe 🚨"),
            OVER_BUDGET to listOf("Congratulations 🎉 budgetcha ant kelay officially.", "Budget: ₹%d\nTu: ₹%d\nApp: 🤡"),
            ZERO_REMAINING to listOf("₹0 shillak. Pudhcha transaction phakta CREDIT hava 💀", "Financial independence ❌\nFinancial emergency ✅")
        ),
        SAVAGE to mapOf(
            SMALL_SPEND to listOf("Evadhya chotya kharchasathi pan notification ala 💀", "Evadhya paishasathi tension nako, wallet zop ghetoy 😂"),
            MODERATE_SPEND to listOf("Bhava wallet ne rajinama dilay 💀", "UPI PIN pan damla asel ata 😭"),
            LARGE_SPEND to listOf("Evadha kharch karto jasa Ambanicha natevaik ahe 💀", "RBI ne tula khajgi account nahi dila re 😂"),
            UNDER_50 to listOf("Aaj tar bhava jababdar zalay 😏", "Evadha control? Kon ahes tu? 👀"),
            BETWEEN_50_75 to listOf("Mahina ardha pan nahi zala, budget ardha sampla 😂", "Kharch jara jasta interesting hotay 😂"),
            BETWEEN_75_90 to listOf("Budget ventilator var ahe bhava 🚨", "Ata jara brake mar re."),
            BETWEEN_90_100 to listOf("Wallet ne akher shwas ghetla bhava 💀", "Ajun ek transaction ani sampla 😭"),
            OVER_BUDGET to listOf("Bhava accountant la tond kasa dakhavshil? 😂", "Budget navachi cheez officially udavun takli 💀"),
            ZERO_REMAINING to listOf("Bhava ata UPI PIN visar.", "Pudhchya pagarparyant Devach bharosa 🙏")
        )
    )

    private val byLanguage = mapOf(
        RoastLanguage.EN to en,
        RoastLanguage.HI to hi,
        RoastLanguage.MR to mr
    )

    fun pool(language: RoastLanguage, level: RoastLevel, category: RoastCategory): List<String> =
        byLanguage[language]?.get(level)?.get(category) ?: emptyList()

    val salaryMessages: Map<RoastLanguage, List<String>> = mapOf(
        RoastLanguage.EN to listOf(
            "Salary credited 🤑",
            "You just became Warren Buffett for a day 😎",
            "Salary's in! Don't start spending immediately 😂"
        ),
        RoastLanguage.HI to listOf(
            "Salary credited 🤑",
            "Aaj toh bhai Warren Buffett ban gaya 😎",
            "Salary aa gayi! Abhi se kharcha mat shuru kar dena 😂"
        ),
        RoastLanguage.MR to listOf(
            "Pagar jama zali 🤑",
            "Aaj tar bhava Warren Buffett zalas 😎",
            "Pagar aali! Lagech kharch suru karu nakos 😂"
        )
    )
}
