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

/**
 * Message pools for Bhai Mode: 3 roast levels x 3 languages x 9 spending/budget
 * categories, ~12 messages per category per level (100+ per level per language
 * across all categories combined). Keep it playful, never aggressive or offensive
 * even at Savage — exaggerated/dramatic mockery, not real insults.
 */
object BhaiMessages {

    private val en = mapOf(
        MILD to mapOf(
            SMALL_SPEND to listOf(
                "That's fine, small expense.", "No worries, totally normal.",
                "A little spend never hurt anyone.", "That's within reason.",
                "Nothing to flag here.", "Small stuff, keep going.",
                "That's just everyday spending.", "All good on this one.",
                "Perfectly reasonable purchase.", "Not even worth a second thought.",
                "Tiny expense, tiny deal.", "That one barely counts."
            ),
            MODERATE_SPEND to listOf(
                "Keep an eye on this one.", "A bit more than usual, that's okay.",
                "Noted, nothing alarming though.", "A little bigger than your usual, still fine.",
                "Worth remembering, not worth worrying.", "That nudged your spending a bit.",
                "A moderate one — keep tracking.", "Slightly above average today.",
                "Just keeping tabs on this.", "That's a bit more than a snack run.",
                "A little splurge, no big deal.", "Worth a glance next time you check in."
            ),
            LARGE_SPEND to listOf(
                "That was a big one today — just flagging it.", "Bigger expense than usual, noted.",
                "That's a significant one for today.", "Worth keeping in mind going forward.",
                "A sizable purchase — just noting it.", "That's on the higher side today.",
                "Big spend, make sure it was planned.", "That one stands out a bit.",
                "Noted — a chunkier expense than most.", "That's a fair bit more than usual.",
                "Worth a mental note for later.", "A larger one — nothing more than that."
            ),
            UNDER_50 to listOf(
                "All good, budget's healthy.", "You're doing fine so far.",
                "Steady pace this month.", "Nothing to worry about yet.",
                "Comfortably within budget.", "You're tracking well.",
                "Budget's in good shape.", "Early days, all looks fine.",
                "No concerns at this point.", "You're pacing nicely.",
                "Plenty of room left.", "So far so good."
            ),
            BETWEEN_50_75 to listOf(
                "Half your budget's gone, just keep watching.", "Past the halfway mark now.",
                "You've used a fair chunk, still okay.", "Worth checking in on spending pace.",
                "Halfway through, still manageable.", "A good time to review your spending.",
                "More than half used, keep an eye out.", "Budget's moving along steadily.",
                "You're over the midpoint now.", "Still fine, just stay aware.",
                "Halfway there, pace looks okay.", "Worth a quick check-in on your spending."
            ),
            BETWEEN_75_90 to listOf(
                "75% used, getting close to the limit.", "Budget's getting tight now.",
                "Worth slowing down a little.", "Most of the budget's used up.",
                "Getting close to the ceiling.", "Time to be a bit more careful.",
                "Budget's mostly spent for the month.", "Not much room left now.",
                "A good time to pace yourself.", "Budget's thinning out.",
                "Worth watching closely from here.", "Getting near the edge of your budget."
            ),
            BETWEEN_90_100 to listOf(
                "Almost at the limit, be careful now.", "Just a little room left.",
                "Very close to the budget ceiling.", "Time to hold off on extra spending.",
                "Budget's nearly used up.", "Not much left to spend now.",
                "Right at the edge of your budget.", "Worth pausing before the next purchase.",
                "Almost there — spend carefully.", "Just a sliver of budget remains.",
                "Getting very close to zero.", "Best to hold steady from here."
            ),
            OVER_BUDGET to listOf(
                "You've gone a little over budget this month.", "Budget's exceeded, worth a look.",
                "A bit past the limit this time.", "Slightly over — worth reviewing next month.",
                "You've crossed the budget line.", "Over budget, but not by a lot.",
                "This month ran a bit high.", "Budget's been surpassed a little.",
                "A modest overspend this month.", "You went past the target slightly.",
                "Budget's in the red, just a touch.", "Worth planning a bit differently next month."
            ),
            ZERO_REMAINING to listOf(
                "You're right at your budget limit now.", "Zero left — plan carefully from here.",
                "Budget's fully used for this period.", "Right at the line, nothing more to spend.",
                "You've reached your budget exactly.", "No room left — worth being careful.",
                "Budget's spent, right on the mark.", "You've used it all up for now.",
                "That's the full budget, used up.", "Nothing left in the tank for this period.",
                "Right at zero — a good time to pause.", "Budget's exactly spent."
            )
        ),
        MEDIUM to mapOf(
            SMALL_SPEND to listOf(
                "Chill, small expense.", "No tension, that's normal spending.",
                "That's basically pocket change, bro.", "Barely worth mentioning tbh.",
                "Your wallet didn't even feel that one.", "Small potatoes, don't stress.",
                "That's rounding error money.", "Wallet's fine, chill out.",
                "Not even a blip on the radar.", "That's snack money, no drama.",
                "Chillax, that's nothing.", "Your wallet said 'that's it?'"
            ),
            MODERATE_SPEND to listOf(
                "Bro, check your wallet once in a while 👀", "Money's leaving, you know that right? 😂",
                "Bro your card is getting a workout today.", "That's a proper dent, not gonna lie.",
                "UPI's having a good day off you.", "Wallet's starting to notice you, bro.",
                "That one actually moved the needle.", "Bro spent like he had a plan (he didn't).",
                "Card's earning its frequent flyer miles.", "That's not pocket change anymore, bro.",
                "Wallet just raised an eyebrow.", "Bro's spending arc is escalating."
            ),
            LARGE_SPEND to listOf(
                "Careful bro, money doesn't grow on trees 🌳💸", "That's salary money, not lottery winnings 💀",
                "Bro really said 'treat yourself' today 😂", "That's a whole vibe shift in your balance.",
                "Wallet just filed a complaint.", "Bro's account just gasped.",
                "That's not a purchase, that's an event.", "UPI needs a moment after that one.",
                "Bro really went for it today 💸", "That's a 'explain to future you' kind of spend.",
                "Wallet took that one personally.", "Big spender energy today, huh bro."
            ),
            UNDER_50 to listOf(
                "You're in control, nice and steady 😎", "Budget's still safe.",
                "Look at you, being all responsible.", "Budget's chilling, you're chilling.",
                "Nice pace, keep it up bro.", "Wallet's happy with you today.",
                "You're playing this smart, bro.", "Budget's comfortably in the green.",
                "Steady hands, steady wallet.", "You're actually killing it this month.",
                "Bro's being financially responsible?? shocking.", "Budget status: thriving."
            ),
            BETWEEN_50_75 to listOf(
                "Careful now, halfway through the budget 👀", "Half the budget's gone already.",
                "50% down, bro, pace yourself.", "Wallet's halfway to empty.",
                "Budget's at the halfway checkpoint.", "Bro's spending is picking up speed.",
                "Half gone, half to go — stay sharp.", "That's the midpoint, bro, eyes open.",
                "Budget's dipping past 50%.", "Halfway mark hit, tighten up a bit.",
                "Wallet's getting a little nervous.", "50%+ gone, bro, keep watching."
            ),
            BETWEEN_75_90 to listOf(
                "Bro, 75% of the budget is gone 😭", "Half the month left and budget's in the ICU.",
                "Budget's on thin ice now, bro.", "75% down, this is getting spicy.",
                "Wallet's sending SOS signals.", "Bro, budget's basically running on fumes.",
                "75%+ gone, time to hit the brakes.", "Budget's gasping for air here.",
                "Bro really tested the budget's patience.", "Getting real close to broke energy.",
                "Budget's in the danger zone now.", "75% gone, bro, this is not a drill."
            ),
            BETWEEN_90_100 to listOf(
                "Bro please stop 😭", "Wallet's in the ICU 🚨",
                "90%+ gone, bro, this is critical.", "Budget's on life support.",
                "One wrong move and it's game over.", "Bro, the wallet is begging for mercy.",
                "90% down, brace for impact.", "This is the final boss of budgets.",
                "Wallet's flatlining, bro.", "Almost there — as in, almost broke.",
                "90%+ used, bro, red alert.", "Budget's hanging by a thread."
            ),
            OVER_BUDGET to listOf(
                "Congratulations 🎉 you officially wrecked the budget.", "Budget: ₹%d\nYou: ₹%d\nApp: 🤡",
                "Budget said bye-bye a while ago.", "Bro broke the budget and kept going.",
                "That's not overspending, that's overachieving (badly).", "Budget's officially retired for the month.",
                "Bro really said 'budget who?' 😂", "This month's budget: rest in peace.",
                "Congrats, you unlocked overspender status.", "Budget got left in the dust, bro.",
                "That number's not supposed to be red, bro.", "Bro's spending arc has no ceiling apparently."
            ),
            ZERO_REMAINING to listOf(
                "₹0 remaining. The next transaction better be a CREDIT 💀", "Financial independence ❌\nFinancial emergency ✅",
                "Wallet's at rock bottom, bro.", "Zero left, bro, that's the whole story.",
                "Budget's tapped out completely.", "Bro, the tank is officially empty.",
                "That's a hard zero, no cushion left.", "Wallet said 'that's all I got, bro.'",
                "Zero remaining, time to freeze the cards.", "Bro really hit rock bottom on budget.",
                "Nothing left, bro, nothing at all.", "Budget's flatlined at zero."
            )
        ),
        SAVAGE to mapOf(
            SMALL_SPEND to listOf(
                "Bro really tracked a snack purchase 💀", "Congrats on the ₹50 flex 🙄",
                "This transaction doesn't even deserve a notification.", "Bro really flexed his loose change 💀",
                "That's not spending, that's a rounding error with attitude.", "Imagine getting roasted for THIS amount 😭",
                "Bro's biggest financial decision of the day: this.", "The audacity to spend and expect applause.",
                "That's an insult to the word 'expense.'", "Bro really made history with ₹20.",
                "This is the financial equivalent of a shrug.", "Certified nothing-burger of a transaction."
            ),
            MODERATE_SPEND to listOf(
                "Wallet filed a missing person report 🚨", "Bro's UPI app needs a cooldown timer 😭",
                "Bro's card is basically on a diet now.", "Wallet just sent a strongly worded email.",
                "That's not a purchase, that's a hostage situation.", "Bro's spending like the app doesn't track this 💀",
                "UPI is starting to recognize your face.", "That's the third one today, bro. THIRD.",
                "Wallet's therapist bill just went up.", "Bro out here funding someone's whole business.",
                "That's not shopping, that's a hobby now.", "Wallet's filing for emotional damages."
            ),
            LARGE_SPEND to listOf(
                "Spending like you inherited a fortune 💀", "RBI didn't give you a personal treasury, bro 😂",
                "Bro's account just had a near-death experience.", "That's not a transaction, that's a natural disaster.",
                "Wallet's filing a police report.", "Bro spent that like he prints money at home.",
                "That number should come with a warning label.", "Bro really said 'I have no relationship with savings.'",
                "Wallet just watched its whole life flash by.", "That's generational wealth... leaving your account.",
                "Bro's balance dropped like a mic.", "This transaction has main character energy, and not in a good way."
            ),
            UNDER_50 to listOf(
                "Look at you, financially responsible for once 😏", "Shockingly under control today 👀",
                "Who even are you right now, bro.", "Suspiciously well-behaved budget today.",
                "Bro discovered self-control, someone alert the news.", "This is not the bro I know.",
                "Budget's confused why you're being normal.", "Bro's playing 4D chess with his money apparently.",
                "This restraint is almost concerning.", "Bro's wallet is shocked into silence.",
                "Financial glow-up loading...", "Who's impersonating my broke friend right now."
            ),
            BETWEEN_50_75 to listOf(
                "Halfway gone and it's not even month-end 😂", "Expenses are getting a little too interesting 😂",
                "Bro's speedrunning his own budget.", "Halfway to broke, world record pace.",
                "Budget's aging like milk, not wine.", "Bro's on a spending sprint nobody asked for.",
                "50%+ gone and the month just started, bro.", "This budget has trust issues now.",
                "Bro's really out here testing fate.", "Budget's halfway to a crime scene.",
                "The wallet saw this coming and still got hurt.", "Bro's spending pace could qualify for the Olympics."
            ),
            BETWEEN_75_90 to listOf(
                "Budget's on life support at this point 🚨", "Time to hit the brakes, seriously.",
                "Bro's budget is on its last legs, literally.", "75%+ gone, this is a full-blown emergency.",
                "Wallet's writing its will right now.", "Bro really said 'consequences later.'",
                "Budget's begging for a restraining order.", "This is not a drill, bro, this is a crisis.",
                "Bro's spending like the month has 60 days.", "Budget's flatline is imminent.",
                "Wallet's on hospice care at this point.", "Bro's playing financial Russian roulette."
            ),
            BETWEEN_90_100 to listOf(
                "Wallet is on its deathbed 💀", "One more transaction and it's over 😭",
                "Bro's account is basically a ghost town.", "This is the wallet's final boss battle.",
                "90%+ gone, bro, say your goodbyes.", "Wallet's flatlining as we speak.",
                "Bro's about to hit rock bottom at terminal velocity.", "This is financial free-fall, bro.",
                "Wallet's last breath is being drawn right now.", "Bro's playing chicken with bankruptcy.",
                "The countdown to zero has officially begun.", "Wallet's writing its obituary."
            ),
            OVER_BUDGET to listOf(
                "Budget has been obliterated 💀", "The accountant would cry looking at this 😂",
                "Bro didn't just break the budget, he cremated it.", "This isn't overspending, this is a full financial crime.",
                "Budget's not just dead, it's been buried and mourned.", "Bro really said 'rules are for other people.'",
                "That number is a cry for help, bro.", "Budget got nuked from orbit.",
                "This is what financial chaos looks like, bro.", "Bro's spending has entered its villain arc.",
                "The budget didn't stand a chance against you.", "This overspend deserves its own documentary."
            ),
            ZERO_REMAINING to listOf(
                "Forget your UPI PIN for a while 😂", "Until next salary, it's God's mercy only 🙏",
                "Bro's wallet is now just a decorative item.", "Zero left, bro, time to live off vibes.",
                "The wallet has achieved true minimalism: nothing.", "Bro's account balance and his soul are both empty now.",
                "This is rock bottom, bro, and you're still digging.", "Wallet's officially unemployed until further notice.",
                "Bro, delete the UPI app for your own safety.", "Zero remaining — congrats on reaching financial nirvana (the bad kind).",
                "This is the wallet equivalent of a blackout.", "Bro's account just went into witness protection."
            )
        )
    )

    private val hi = mapOf(
        MILD to mapOf(
            SMALL_SPEND to listOf(
                "Chalta hai bhai 😊", "Koi baat nahi, chota kharcha hai.",
                "Itna toh normal hai bhai.", "Chhota sa kharcha, tension mat le.",
                "Yeh toh kuch bhi nahi hai bhai.", "Bilkul thik hai, chinta mat kar.",
                "Itna toh chalta rehta hai.", "Chota kharcha, bada dil bhai.",
                "Sab normal hai iss mein.", "Yeh amount toh gin ne layak bhi nahi.",
                "Aaram se, koi issue nahi.", "Chalta hai yaar, chill kar."
            ),
            MODERATE_SPEND to listOf(
                "Bhai thoda dhyan rakhna iss baar.", "Thoda zyada hai, par theek hai.",
                "Ek baar dekh le apna balance bhai.", "Thoda sa jyada tha, par chalega.",
                "Note kar le bhai, itna zyada nahi.", "Yeh thoda sochne wali baat hai.",
                "Dhyan se bhai, thoda upar gaya.", "Normal se thoda zyada tha yeh.",
                "Bas thoda alert rehna bhai.", "Yeh kharcha thoda notice-worthy hai.",
                "Thoda dhyan de bhai iss par.", "Chalega, par next baar dhyan rakhna."
            ),
            LARGE_SPEND to listOf(
                "Bhai bada kharcha tha ye, dhyan rakhna.", "Thoda bada tha ye, but chalega.",
                "Yeh ek bada transaction tha bhai.", "Aage se dhyan rakhna iss tarah ke kharche mein.",
                "Bada amount tha, note kar le.", "Yeh kharcha thoda zyada bada tha bhai.",
                "Dhyan se, yeh normal se kaafi zyada tha.", "Bhai yeh ek significant kharcha tha.",
                "Iska hisaab rakhna zaroori hai bhai.", "Thoda planned hona chahiye tha shayad.",
                "Yeh amount yaad rakhne layak hai.", "Bada kharcha, bas itna hi kahunga bhai."
            ),
            UNDER_50 to listOf(
                "Sab thik hai, budget safe hai.", "Abhi tak sab control mein hai.",
                "Bahut accha chal raha hai bhai.", "Budget abhi bilkul healthy hai.",
                "Koi tension nahi abhi tak.", "Sahi pace pe chal raha hai bhai.",
                "Abhi tak sab kuch normal hai.", "Budget mein abhi bahut jagah hai.",
                "Sab kuch control mein chal raha hai.", "Achha pace hai bhai, aise hi chal.",
                "Abhi koi worry ki baat nahi.", "Sab set hai abhi tak bhai."
            ),
            BETWEEN_50_75 to listOf(
                "Aadha budget gaya hai, dhyan rakhna.", "Half raste mein aa gaye ho.",
                "Aadha se zyada nikal gaya bhai.", "Budget ka aadha hissa khatam.",
                "Bhai aadha budget ud gaya hai.", "Yeh sahi time hai dhyan dene ka.",
                "Aadhe se upar nikal gaye bhai.", "Budget check karne ka time hai.",
                "Halfway point cross ho gaya hai.", "Thoda dhyan rakhna ab se bhai.",
                "Aadha khatam, aadha baaki hai.", "Bhai ab thoda alert rehna."
            ),
            BETWEEN_75_90 to listOf(
                "75% budget gaya hai, dhyan se.", "Budget thoda tight ho raha hai.",
                "Bhai 75% se upar nikal gaye.", "Ab thoda careful rehna padega.",
                "Budget ka bada hissa khatam ho gaya.", "Bahut kam bacha hai ab bhai.",
                "Ab dhyan se kharch karna bhai.", "Budget khatam hone ke kareeb hai.",
                "Thoda slow down karna padega bhai.", "Bahut zyada nikal gaya hai bhai.",
                "Ab bahut savdhaan rehna hoga.", "Budget ki limit paas aa rahi hai."
            ),
            BETWEEN_90_100 to listOf(
                "Bas thoda hi bacha hai, sambhal ke.", "Limit ke bahut kareeb ho.",
                "Bhai budget khatam hone wala hai.", "Bahut kam bacha hai ab bhai.",
                "Ab ek dum careful rehna bhai.", "Budget ki edge pe ho abhi.",
                "Thoda sa hi reh gaya hai bhai.", "Ab agla kharcha soch samajh ke.",
                "Bilkul limit ke paas pahunch gaye.", "Budget khatam hone hi wala hai.",
                "Bahut thoda margin bacha hai bhai.", "Ab ruk ke sochna padega bhai."
            ),
            OVER_BUDGET to listOf(
                "Iss mahine budget thoda cross ho gaya.", "Budget se thoda upar chale gaye ho.",
                "Bhai budget paar ho gaya hai.", "Thoda zyada kharch ho gaya iss baar.",
                "Budget cross kar diya hai bhai.", "Limit se thoda aage nikal gaye.",
                "Iss mahine thoda zyada ho gaya.", "Budget se bahar chale gaye bhai.",
                "Thoda over ho gaya iss baar.", "Agli baar thoda plan karna bhai.",
                "Budget ki seema paar ho gayi.", "Thoda adjust karna padega agli baar."
            ),
            ZERO_REMAINING to listOf(
                "Budget ka limit aa gaya hai ab.", "Zero bacha hai — dhyan se chalna.",
                "Bhai budget bilkul khatam ho gaya.", "Ab kuch nahi bacha bhai budget mein.",
                "Poora budget use ho gaya hai.", "Bhai ab zero reh gaya hai.",
                "Budget bilkul exact khatam hua hai.", "Ab kuch bhi extra nahi bacha.",
                "Poora khatam, kuch bhi nahi bacha.", "Bhai ab bilkul zero pe ho.",
                "Budget ki poori limit use ho gayi.", "Ab bilkul kuch bhi nahi bacha bhai."
            )
        ),
        MEDIUM to mapOf(
            SMALL_SPEND to listOf(
                "Chalta hai bhai 😌", "Itna toh banta hai 😂",
                "Yeh toh chai paani jaisa hai bhai.", "Itne mein toh kuch hota bhi nahi bhai.",
                "Wallet ko pata bhi nahi chala bhai.", "Chillar hai bhai, tension kaisi.",
                "Itna toh roz hota hai bhai.", "Wallet bola 'itna hi tha?' 😂",
                "Bhai yeh toh notification layak bhi nahi.", "Chota mota kharcha hai bhai, chill.",
                "Itne mein toh kuch aata bhi nahi ab.", "Bhai yeh amount toh joke hai."
            ),
            MODERATE_SPEND to listOf(
                "Bhai... wallet ki taraf bhi dekh le kabhi 👀", "UPI button ko thoda rest de bhai.",
                "Bhai card ko toh gym bhej diya aaj.", "Wallet thoda serious ho raha hai bhai.",
                "Yeh third baar hai aaj bhai.", "UPI ko aaj bhai ka number yaad ho gaya.",
                "Bhai kharche mein rhythm aa gaya hai.", "Wallet ne aaj notice le liya hai.",
                "Yeh chhota kharcha nahi tha bhai.", "Bhai spending mode on hai kya aaj?",
                "Card thoda thak gaya hoga bhai.", "Wallet bhi soch raha hai 'phir se?'"
            ),
            LARGE_SPEND to listOf(
                "BHAI DHYAN SE, PAISE PED PE NAHI AATE 🌳💸", "Bhai salary aayi hai, IPL contract nahi mila 💀",
                "Bhai yeh toh ek event ban gaya 😂", "Wallet ne complaint file kar di bhai.",
                "Bhai account ne gehri saans li abhi.", "Itna bada kharcha, bhai kuch soch ke kiya?",
                "UPI ko thoda break chahiye tha bhai.", "Bhai aaj toh full spender mode mein tha.",
                "Yeh kharcha future-you explain karega bhai.", "Wallet ne yeh personally le liya bhai.",
                "Bhai aaj toh dhoom macha di kharche mein.", "Itna paisa, bhai kuch plan bhi tha?"
            ),
            UNDER_50 to listOf(
                "Mast bhai, control mein hai 😎", "Budget abhi safe hai.",
                "Bhai aaj toh responsible ban gaya.", "Budget chill hai, tu bhi chill kar.",
                "Achha pace hai bhai, aise hi rakh.", "Wallet khush hai bhai aaj.",
                "Bhai smart move chal raha hai.", "Budget comfortably green mein hai.",
                "Steady bhai, control mein hai sab.", "Bhai iss mahine toh kamaal kar raha.",
                "Bhai responsible? Shocking hai yeh.", "Budget status: mast chal raha hai."
            ),
            BETWEEN_50_75 to listOf(
                "Thoda sambhal ke chal bhai 👀", "Aadha budget gaya hai bhai.",
                "50% gaya bhai, thoda pace kar.", "Wallet aadha khali ho gaya bhai.",
                "Halfway point aa gaya bhai.", "Bhai speed thodi badh rahi hai kharche ki.",
                "Aadha gaya, aadha bacha, dhyan rakh.", "Yeh midpoint hai bhai, aankhen khuli rakh.",
                "50% se upar nikal gaya bhai.", "Halfway mark, thoda tight kar bhai.",
                "Wallet thoda nervous ho raha hai.", "50%+ gaya bhai, dhyan se chal."
            ),
            BETWEEN_75_90 to listOf(
                "Bhai budget ka 75% uda diya 😭", "Aadha mahina baaki hai aur budget ICU mein hai.",
                "Budget thin ice pe hai bhai.", "75% gaya, ab toh spicy ho gaya bhai.",
                "Wallet SOS bhej raha hai bhai.", "Bhai budget saans le raha hai bas.",
                "75%+ gaya, ab brake maar bhai.", "Budget hawa mein hai bhai.",
                "Bhai budget ka patience test ho gaya.", "Broke energy paas aa rahi hai bhai.",
                "Budget danger zone mein hai bhai.", "75% gaya bhai, yeh drill nahi hai."
            ),
            BETWEEN_90_100 to listOf(
                "BHAI BAS KAR 😭", "Wallet ICU mein hai 🚨",
                "90%+ gaya bhai, yeh critical hai.", "Budget life support pe hai.",
                "Ek galat move aur khatam bhai.", "Wallet bhai se maafi maang raha hai.",
                "90% gaya, ab impact ke liye ready ho.", "Yeh budget ka final boss hai bhai.",
                "Wallet flatline kar raha hai bhai.", "Almost khatam — matlab almost broke.",
                "90%+ gaya, red alert bhai.", "Budget dhaage pe latka hai bhai."
            ),
            OVER_BUDGET to listOf(
                "Congratulations 🎉 Budget ki officially maa-behen kar di.", "Budget: ₹%d\nYou: ₹%d\nApp: 🤡",
                "Budget bahut pehle bye bol chuka hai.", "Bhai budget tod ke aage nikal gaya.",
                "Yeh overspending nahi, overachievement hai (bura wala).", "Budget officially retire ho gaya bhai.",
                "Bhai bola 'budget kaun?' 😂", "Iss mahine ka budget: RIP.",
                "Congrats, overspender ka badge mil gaya.", "Budget peeche reh gaya bhai.",
                "Yeh number red nahi hona chahiye tha bhai.", "Bhai ke kharche ka koi ceiling nahi hai lagta."
            ),
            ZERO_REMAINING to listOf(
                "₹0 remaining. Ab agla transaction sirf CREDIT hona chahiye. 💀", "Financial independence ❌\nFinancial emergency ✅",
                "Wallet rock bottom pe hai bhai.", "Zero bacha bhai, bas yehi kahani hai.",
                "Budget bilkul khali ho gaya hai.", "Bhai tank bilkul empty hai ab.",
                "Yeh hard zero hai, koi cushion nahi.", "Wallet bola 'bas itna hi tha bhai.'",
                "Zero bacha, ab cards freeze kar de.", "Bhai rock bottom hit kar diya budget mein.",
                "Kuch nahi bacha bhai, bilkul kuch nahi.", "Budget flatline ho gaya zero pe."
            )
        ),
        SAVAGE to mapOf(
            SMALL_SPEND to listOf(
                "Itne se paise ke liye bhi notification aa gaya bhai 💀", "Bhai itna chota kharcha, phone bhi sharma gaya 😂",
                "Yeh transaction notification layak bhi nahi tha bhai.", "Bhai apna change flex kar raha hai 💀",
                "Yeh spending nahi, attitude wala rounding error hai.", "Itne amount pe roast? Ghor kalyug hai 😭",
                "Bhai ka aaj ka sabse bada decision: yeh tha.", "Itna kharch karke bhi taali maangna.",
                "Yeh 'expense' word ki toheen hai bhai.", "Bhai ne ₹20 se history bana di.",
                "Yeh financial shrug ke barabar hai.", "Certified nothing-burger transaction hai yeh."
            ),
            MODERATE_SPEND to listOf(
                "Bhai UPI pin bhi thak gaya hoga ab 😭", "Wallet ne resignation letter likh diya 💀",
                "Card ab diet pe hai bhai.", "Wallet ne strongly worded email bheja hai.",
                "Yeh purchase nahi, hostage situation hai.", "Bhai aise kharch kar raha jaise track nahi hoga 💀",
                "UPI ab bhai ka chehra pehchanta hai.", "Yeh third baar hai bhai, THIRD.",
                "Wallet ke therapy bills badh gaye.", "Bhai kisi ka business fund kar raha hai.",
                "Yeh shopping nahi, hobby ban gayi hai.", "Wallet emotional damage ka case file kar raha hai."
            ),
            LARGE_SPEND to listOf(
                "RBI ne tujhe personal account nahi diya hai bhai 😂", "Itna paisa uda raha hai jaise Ambani ka rishtedaar hai 💀",
                "Bhai ka account near-death experience se guzra.", "Yeh transaction nahi, natural disaster hai.",
                "Wallet ne police report file kar diya.", "Bhai aise kharch kar raha jaise ghar pe printer hai.",
                "Yeh number pe warning label hona chahiye.", "Bhai ne bola 'savings se koi rishta nahi.'",
                "Wallet ne apni poori zindagi flash hote dekhi.", "Yeh generational wealth hai... account se jaate hue.",
                "Bhai ka balance mic-drop ki tarah gira.", "Yeh transaction ka main character energy hai, bura wala."
            ),
            UNDER_50 to listOf(
                "Aaj toh bhai financially responsible ban gaya 😏", "Itna control? Kaun ho tum? 👀",
                "Yeh bhai kaun hai jo main jaanta tha?", "Aaj ka budget suspiciously well-behaved hai.",
                "Bhai ne self-control discover kar liya, someone alert the news.", "Yeh woh bhai nahi hai jo pehchanta tha.",
                "Budget confuse hai ki tu normal kyun hai.", "Bhai 4D chess khel raha hai apne paise se.",
                "Yeh restraint thoda concerning hai.", "Bhai ka wallet shock mein chup hai.",
                "Financial glow-up load ho raha hai...", "Kaun hai yeh mere broke dost ka imposter?"
            ),
            BETWEEN_50_75 to listOf(
                "Aadha mahina bhi nahi hua, aadha budget gayab 😂", "Expenses kuch zyada interesting ho rahe hain 😂",
                "Bhai apna hi budget speedrun kar raha hai.", "Broke tak halfway, world record pace hai.",
                "Budget milk ki tarah kharab ho raha hai, wine nahi.", "Bhai bin bulaye spending sprint pe hai.",
                "50%+ gaya aur mahina abhi shuru hua hai bhai.", "Iss budget ko trust issues ho gaye hain.",
                "Bhai fate ko test kar raha hai lagta hai.", "Budget crime scene ki taraf halfway hai.",
                "Wallet ko yeh pehle se pata tha, phir bhi dukh hua.", "Bhai ki spending pace Olympics qualify kar sakti hai."
            ),
            BETWEEN_75_90 to listOf(
                "Budget ventilator pe hai bhai 🚨", "Ab thoda brake laga bhai.",
                "Bhai ka budget aakhri saans le raha hai, literally.", "75%+ gaya, ab toh spicy ho gaya bhai.",
                "Wallet apna will likh raha hai abhi.", "Bhai ne bola 'consequences baad mein dekhenge.'",
                "Budget restraining order maang raha hai.", "Yeh drill nahi hai bhai, yeh crisis hai.",
                "Bhai aise kharch kar raha jaise mahine mein 60 din hain.", "Budget ka flatline bas hone wala hai.",
                "Wallet hospice care pe hai iss point.", "Bhai financial Russian roulette khel raha hai."
            ),
            BETWEEN_90_100 to listOf(
                "Wallet ne aakhri saans le li hai bhai 💀", "Ek aur transaction aur khatam 😭",
                "Bhai ka account bilkul ghost town hai.", "Yeh wallet ki final boss battle hai.",
                "90%+ gaya bhai, alvida keh de.", "Wallet abhi flatline kar raha hai.",
                "Bhai terminal velocity se rock bottom pe jaa raha hai.", "Yeh financial free-fall hai bhai.",
                "Wallet ki aakhri saans chal rahi hai.", "Bhai bankruptcy ke saath chicken khel raha hai.",
                "Zero tak countdown officially shuru ho gaya.", "Wallet apna obituary likh raha hai."
            ),
            OVER_BUDGET to listOf(
                "Bhai accountant ko kya muh dikhayega? 😂", "Budget naam ki cheez ko officially uda diya 💀",
                "Bhai ne budget tod ke usse cremate bhi kar diya.", "Yeh overspending nahi, poora financial crime hai.",
                "Budget sirf mara nahi, bury bhi ho gaya, maatam bhi hua.", "Bhai ne bola 'rules doosron ke liye hain.'",
                "Yeh number ek cry for help hai bhai.", "Budget orbit se nuke ho gaya.",
                "Yeh financial chaos ka scene hai bhai.", "Bhai ki spending ne villain arc shuru kar diya.",
                "Budget ke paas tere against koi chance nahi tha.", "Iss overspend ka apna documentary bante hai bhai."
            ),
            ZERO_REMAINING to listOf(
                "Bhai ab UPI PIN bhool ja.", "Ab agle salary tak Bhagwan bharose 🙏",
                "Bhai ka wallet ab sirf decoration hai.", "Zero bacha bhai, ab vibes pe jee.",
                "Wallet ne true minimalism achieve kar liya: kuch nahi.", "Bhai ka balance aur uski soul, dono khali hain ab.",
                "Yeh rock bottom hai bhai, aur tu abhi bhi khod raha hai.", "Wallet officially unemployed hai ab.",
                "Bhai, apni safety ke liye UPI app delete kar de.", "Zero bacha — financial nirvana mubarak ho (bura wala).",
                "Yeh wallet ka blackout version hai.", "Bhai ka account witness protection mein chala gaya."
            )
        )
    )

    private val mr = mapOf(
        MILD to mapOf(
            SMALL_SPEND to listOf(
                "Thik ahe re, lahan kharch ahech.", "Kahi problem nahi, evadhach ahe.",
                "Itka tar normal ahe re.", "Lahan kharch ahe, tension nako.",
                "He tar kahich nahi re.", "Bhalti thik ahe, kahi kalji nako.",
                "Itka tar chalatach aste.", "Lahan kharch, mothha dil re.",
                "Sagla normal ahe yat.", "Ha amount mojnyasarkha pan nahi.",
                "Aaram madhe, kahi issue nahi.", "Chalte re, chill kar."
            ),
            MODERATE_SPEND to listOf(
                "Jara laksh de re ya kharchakade.", "Thoda jasta ahe, pan thik ahe.",
                "Ekda bagh apla balance re.", "Thoda jasta hota, pan chalel.",
                "Note kar re, thoda jasta nahi.", "He jara vichar karnyasarkhe ahe.",
                "Laksh de re, thoda var gela.", "Normal peksha thoda jasta hota he.",
                "Bas thoda alert rahaycha re.", "Ha kharch thoda notice-worthy ahe.",
                "Jara laksh de re yavar.", "Chalel, pan pudhchya veli laksh theva."
            ),
            LARGE_SPEND to listOf(
                "Bhava mothha kharch hota ha, laksh theva.", "Jara mothha hota, pan chalel.",
                "Ha ek mothha transaction hota bhava.", "Pudhe asha kharchakade laksh theva.",
                "Mothha amount hota, note kar.", "Ha kharch jara jasta mothha hota bhava.",
                "Laksh de, he normal peksha khup jasta hota.", "Bhava ha ek significant kharch hota.",
                "Yacha hishob thevana garjeche ahe bhava.", "Thoda planned vhaayla have hote shayad.",
                "Ha amount laksh thevnyasarkha ahe.", "Mothha kharch, evadhach mhanen bhava."
            ),
            UNDER_50 to listOf(
                "Sagla thik ahe, budget safe ahe.", "Ajun control madhe ahe sagla.",
                "Khup chan chalu ahe bhava.", "Budget ajun ekdam healthy ahe.",
                "Kahi tension nahi ajun.", "Sahi pace var chalu ahe bhava.",
                "Ajun sagla normal ahe.", "Budget madhe khup jaga ahe ajun.",
                "Sagla control madhe chalu ahe.", "Changla pace ahe bhava, asach chal.",
                "Ajun kahi worry nahi.", "Sagla set ahe ajun bhava."
            ),
            BETWEEN_50_75 to listOf(
                "Ardha budget zala, laksh theva.", "Ardhya vatet aahes.",
                "Ardhya peksha jasta gela bhava.", "Budgetcha ardha bhaag sampla.",
                "Bhava ardha budget udla ahe.", "Hi sahi vel ahe laksh dyaychi.",
                "Ardhya peksha var gela bhava.", "Budget check karaycha vel ahe.",
                "Halfway point cross zala ahe.", "Thoda laksh theva ata pasun bhava.",
                "Ardha sampla, ardha baki ahe.", "Bhava ata thoda alert raha."
            ),
            BETWEEN_75_90 to listOf(
                "75% budget gela ahe, laksh theva.", "Budget jara tight hotoy.",
                "Bhava 75% peksha var gela.", "Ata thoda careful rahava lagel.",
                "Budgetcha mothha bhaag sampla ahe.", "Khup kami shillak ahe ata bhava.",
                "Ata laksh de kharch karayla bhava.", "Budget sampnyachya javal ahe.",
                "Thoda slow down karava lagel bhava.", "Khup jasta gela ahe bhava.",
                "Ata khup savdh rahava lagel.", "Budgetchi limit javal yetey."
            ),
            BETWEEN_90_100 to listOf(
                "Thoda ch shillak ahe, sambhal.", "Limit javal aali ahe.",
                "Bhava budget sampnyar ahe.", "Khup kami shillak ahe ata bhava.",
                "Ata ekdam careful raha bhava.", "Budgetchya edge var ahes ata.",
                "Thoda ch rahila ahe bhava.", "Ata pudhcha kharch vichar karun kar.",
                "Ekdam limit javal pochlas.", "Budget sampnyach ahe.",
                "Khup thoda margin shillak ahe bhava.", "Ata thambun vichar karava lagel bhava."
            ),
            OVER_BUDGET to listOf(
                "Ya mahinyat budget jara jasta zala.", "Budget peksha jasta kharch zala ahe.",
                "Bhava budget paar zala ahe.", "Thoda jasta kharch zala ya veli.",
                "Budget cross kela ahe bhava.", "Limit peksha pudhe gela bhava.",
                "Ya mahinyat thoda jasta zala.", "Budget bahercha gela bhava.",
                "Thoda over zala ya veli.", "Pudhchya veli thoda plan kar bhava.",
                "Budgetchi seema paar zali.", "Thoda adjust karava lagel pudhchya veli."
            ),
            ZERO_REMAINING to listOf(
                "Budgetchi limit aali ahe ata.", "Zero shillak ahe — sambhalun raha.",
                "Bhava budget ekdam sampla.", "Ata kahi shillak nahi budget madhe.",
                "Purna budget use zala ahe.", "Bhava ata zero rahila ahe.",
                "Budget ekdam exact sampla ahe.", "Ata kahi extra shillak nahi.",
                "Purna sampla, kahi shillak nahi.", "Bhava ata ekdam zero var ahes.",
                "Budgetchi purna limit use zali.", "Ata ekdam kahi shillak nahi bhava."
            )
        ),
        MEDIUM to mapOf(
            SMALL_SPEND to listOf(
                "Chalte re bhava 😌", "Itka tar banaticha 😂",
                "He tar chaha-paani sarkha ahe bhava.", "Evadhyat tar kahi hotach nahi bhava.",
                "Walletla kalalach nahi bhava.", "Sutti paise ahet bhava, tension kashala.",
                "Itka tar roj hoto bhava.", "Wallet mhanla 'evadhach hota?' 😂",
                "Bhava he notification yogya pan nahi.", "Lahan mothha kharch ahe bhava, chill.",
                "Evadhyat tar kahi yet nahi ata.", "Bhava ha amount joke ahe."
            ),
            MODERATE_SPEND to listOf(
                "Bhava... wallet kade pan bagh kadhi 👀", "UPI la zara rest de re.",
                "Bhava cardla gymla pathavla aaj.", "Wallet jara serious hotoy bhava.",
                "Hi tisri vel ahe aaj bhava.", "UPI la ata bhavacha number pathach zala.",
                "Bhava kharchat rhythm ala ahe.", "Wallet ne aaj notice ghetli ahe.",
                "Ha lahan kharch navta bhava.", "Bhava spending mode on ahe ka aaj?",
                "Card jara damla asel bhava.", "Wallet pan vichar karto 'punha?'"
            ),
            LARGE_SPEND to listOf(
                "Bhava paise zhadavar nahi ugat 🌳💸", "Pagar ali re, IPL contract nahi 💀",
                "Bhava he tar ek event zala 😂", "Wallet ne complaint file keli bhava.",
                "Bhava account ne khoal shwas ghetla ata.", "Evadha mothha kharch, bhava kahi vichar karun kela?",
                "UPI la thoda break have hota bhava.", "Bhava aaj full spender mode madhe hota.",
                "Ha kharch future-you explain karel bhava.", "Wallet ne he personally ghetla bhava.",
                "Bhava aaj kharchat dhoom keli.", "Evadhe paise, bhava kahi plan hota ka?"
            ),
            UNDER_50 to listOf(
                "Mast re, control madhe ahe 😎", "Budget ajun safe ahe.",
                "Aaj tar bhava responsible zalay.", "Budget chill ahe, tu pan chill kar.",
                "Changla pace ahe bhava, asach theva.", "Wallet khush ahe bhava aaj.",
                "Bhava smart move chalu ahe.", "Budget comfortably green madhe ahe.",
                "Steady bhava, sagla control madhe ahe.", "Bhava ya mahinyat kamal karto ahe.",
                "Bhava responsible? Shocking ahe he.", "Budget status: mast chalu ahe."
            ),
            BETWEEN_50_75 to listOf(
                "Jara sambhal re bhava 👀", "Ardha budget gela re.",
                "50% gela bhava, jara pace kar.", "Wallet ardha khali zala bhava.",
                "Halfway point ala bhava.", "Bhava kharchachi speed vadhtey.",
                "Ardha gela, ardha baki, laksh theva.", "Ha midpoint ahe bhava, dole ughade theva.",
                "50% peksha var gela bhava.", "Halfway mark, jara tight kar bhava.",
                "Wallet jara nervous hotoy.", "50%+ gela bhava, laksh theun chal."
            ),
            BETWEEN_75_90 to listOf(
                "Bhava budgetcha 75% udhla 😭", "Ardha mahina baki ahe ani budget ICU madhe ahe.",
                "Budget thin ice var ahe bhava.", "75% gela, ata spicy zala bhava.",
                "Wallet SOS pathavtoy bhava.", "Bhava budget bas shwas ghetoy.",
                "75%+ gela, ata brake mar bhava.", "Budget hawet ahe bhava.",
                "Bhava budgetcha patience test zala.", "Broke energy javal yetey bhava.",
                "Budget danger zone madhe ahe bhava.", "75% gela bhava, hi drill nahi ahe."
            ),
            BETWEEN_90_100 to listOf(
                "BHAVA BAS KAR 😭", "Wallet ICU madhe ahe 🚨",
                "90%+ gela bhava, he critical ahe.", "Budget life support var ahe.",
                "Ek chukichi move ani sampla bhava.", "Wallet bhavakade maafi maagtoy.",
                "90% gela, ata impact sathi ready ho.", "Ha budgetcha final boss ahe bhava.",
                "Wallet ata flatline hotoy bhava.", "Almost zala — mhanje almost broke.",
                "90%+ gela, red alert bhava.", "Budget dhaagyavar latakla ahe bhava."
            ),
            OVER_BUDGET to listOf(
                "Congratulations 🎉 budgetcha ant kelay officially.", "Budget: ₹%d\nTu: ₹%d\nApp: 🤡",
                "Budget khup adhi bye mhanun gela.", "Bhava budget todun pudhe gela.",
                "He overspending nahi, overachievement ahe (waeet wala).", "Budget officially retire zala bhava.",
                "Bhava mhanla 'budget kon?' 😂", "Ya mahinyacha budget: RIP.",
                "Congrats, overspender cha badge milala.", "Budget maage rahila bhava.",
                "Ha number red nasayla have hota bhava.", "Bhavachya kharchala koni ceiling nahi disat."
            ),
            ZERO_REMAINING to listOf(
                "₹0 shillak. Pudhcha transaction phakta CREDIT hava 💀", "Financial independence ❌\nFinancial emergency ✅",
                "Wallet rock bottom var ahe bhava.", "Zero shillak bhava, evadhich story ahe.",
                "Budget ekdam khali zala ahe.", "Bhava tank ekdam empty ahe ata.",
                "He hard zero ahe, kahi cushion nahi.", "Wallet mhanla 'evadhach hota bhava.'",
                "Zero shillak, ata cards freeze kar.", "Bhava rock bottom hit kela budget madhe.",
                "Kahi shillak nahi bhava, ekdam kahi nahi.", "Budget flatline zala zero var."
            )
        ),
        SAVAGE to mapOf(
            SMALL_SPEND to listOf(
                "Evadhya chotya kharchasathi pan notification ala 💀", "Evadhya paishasathi tension nako, wallet zop ghetoy 😂",
                "Ha transaction notification yogya pan navta bhava.", "Bhava apla change flex karto ahe 💀",
                "He spending nahi, attitude wala rounding error ahe.", "Evadhya amount var roast? Ghor kalyug ahe 😭",
                "Bhavacha aajcha sagalyat mothha decision: ha hota.", "Evadha kharch karun pan taali maagto.",
                "He 'expense' shabdacha apman ahe bhava.", "Bhavane ₹20 ne history banavli.",
                "He financial khaanda udavnyasarkhe ahe.", "Certified nothing-burger transaction ahe he."
            ),
            MODERATE_SPEND to listOf(
                "Bhava wallet ne rajinama dilay 💀", "UPI PIN pan damla asel ata 😭",
                "Card ata diet var ahe bhava.", "Wallet ne strongly worded email pathavla ahe.",
                "He purchase nahi, hostage situation ahe.", "Bhava asa kharch karto jasa track honarach nahi 💀",
                "UPI la ata bhavacha chehra olakhto.", "Hi tisri vel ahe bhava, TISRI.",
                "Walletchya therapy bills vadhle.", "Bhava konachatari business fund karto ahe.",
                "He shopping nahi, hobby zali ahe.", "Wallet emotional damage cha case file karto ahe."
            ),
            LARGE_SPEND to listOf(
                "Evadha kharch karto jasa Ambanicha natevaik ahe 💀", "RBI ne tula khajgi account nahi dila re 😂",
                "Bhavacha account near-death experience madhun gela.", "He transaction nahi, natural disaster ahe.",
                "Wallet ne police report file keli.", "Bhava asa kharch karto jasa gharat printer ahe.",
                "Ya number var warning label have hote.", "Bhava mhanla 'savings shi kahi nate nahi.'",
                "Wallet ne apla purna aayushya flash hotana pahila.", "He generational wealth ahe... account madhun jaat ahe.",
                "Bhavacha balance mic-drop sarkha padla.", "Ya transaction cha main character energy ahe, waeet wala."
            ),
            UNDER_50 to listOf(
                "Aaj tar bhava jababdar zalay 😏", "Evadha control? Kon ahes tu? 👀",
                "Ha bhava kon ahe je mala mahit hota?", "Aajcha budget suspiciously changla vagat ahe.",
                "Bhavane self-control discover kela, news sanga.", "Ha to bhava nahi je mala olakhat hota.",
                "Budget confuse ahe ki tu normal ka ahes.", "Bhava apalya paishane 4D chess khelto ahe.",
                "He restraint jara concerning ahe.", "Bhavacha wallet shock madhe gappa ahe.",
                "Financial glow-up load hot ahe...", "Kon ahe ha majhya broke mitracha imposter?"
            ),
            BETWEEN_50_75 to listOf(
                "Mahina ardha pan nahi zala, budget ardha sampla 😂", "Kharch jara jasta interesting hotay 😂",
                "Bhava apla budget speedrun karto ahe.", "Broke paryant halfway, world record pace ahe.",
                "Budget dudhasarkha kharab hoto ahe, wine sarkha nahi.", "Bhava na bolavlela spending sprint var ahe.",
                "50%+ gela ani mahina nukatach suru zala bhava.", "Ya budgetla trust issues zale ahet.",
                "Bhava fate la test karto ahe as vatate.", "Budget crime scene kade halfway ahe.",
                "Walletla he adhich mahit hota, tari dukh zala.", "Bhavachi spending pace Olympics qualify karu shakte."
            ),
            BETWEEN_75_90 to listOf(
                "Budget ventilator var ahe bhava 🚨", "Ata jara brake mar re.",
                "Bhavacha budget akher shwas ghetoy, literally.", "75%+ gela, he purna emergency ahe.",
                "Wallet apla will lihit ahe ata.", "Bhava mhanla 'consequences nantar baghu.'",
                "Budget restraining order maagtoy.", "Hi drill nahi bhava, hi crisis ahe.",
                "Bhava asa kharch karto jasa mahinyat 60 divas ahet.", "Budgetcha flatline lavkarach honar ahe.",
                "Wallet hospice care var ahe ya point la.", "Bhava financial Russian roulette khelto ahe."
            ),
            BETWEEN_90_100 to listOf(
                "Wallet ne akher shwas ghetla bhava 💀", "Ajun ek transaction ani sampla 😭",
                "Bhavacha account ekdam ghost town ahe.", "Hi walletchi final boss battle ahe.",
                "90%+ gela bhava, alvida sang.", "Wallet ata flatline karto ahe.",
                "Bhava terminal velocity ne rock bottom kade jato ahe.", "He financial free-fall ahe bhava.",
                "Walletcha akher shwas chalu ahe.", "Bhava bankruptcy sobat chicken khelto ahe.",
                "Zero paryant countdown officially suru zala.", "Wallet apla obituary lihit ahe."
            ),
            OVER_BUDGET to listOf(
                "Bhava accountant la tond kasa dakhavshil? 😂", "Budget navachi cheez officially udavun takli 💀",
                "Bhavane budget todun tyala cremate pan kela.", "He overspending nahi, purna financial crime ahe.",
                "Budget fakta mela nahi, bury pan zala, maatam pan zala.", "Bhava mhanla 'rules doosryansathi astat.'",
                "Ha number ek cry for help ahe bhava.", "Budget orbit madhun nuke zala.",
                "He financial chaos cha scene ahe bhava.", "Bhavachya kharchane villain arc suru kela.",
                "Budgetkade tuzya viruddh kahich chance navta.", "Ya overspend cha swatahacha documentary banel bhava."
            ),
            ZERO_REMAINING to listOf(
                "Bhava ata UPI PIN visar.", "Pudhchya pagarparyant Devach bharosa 🙏",
                "Bhavacha wallet ata phakta decoration ahe.", "Zero shillak bhava, ata vibes var jag.",
                "Wallet ne khara minimalism achieve kela: kahich nahi.", "Bhavacha balance ani tyacha soul, doghehi khali ahet ata.",
                "He rock bottom ahe bhava, ani tu ajun khodto ahes.", "Wallet officially unemployed ahe ata.",
                "Bhava, swatahchya safety sathi UPI app delete kar.", "Zero shillak — financial nirvana mubarak (waeet wala).",
                "He walletcha blackout version ahe.", "Bhavacha account witness protection madhe gela."
            )
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

    val moneyReceivedMessages: Map<RoastLanguage, List<String>> = mapOf(
        RoastLanguage.EN to listOf(
            "Cha-ching! 💰 Money in.",
            "Nice, someone likes you enough to pay you 😄",
            "Free money alert! (well, not free, but still) 🎉"
        ),
        RoastLanguage.HI to listOf(
            "Cha-ching! 💰 Paisa aaya.",
            "Wah bhai, kisi ne pyaar se paise bheje 😄",
            "Bhai ke account mein khushiyan aayi hain 🎉"
        ),
        RoastLanguage.MR to listOf(
            "Cha-ching! 💰 Paise ale.",
            "Vah bhava, konitari premane paise pathavle 😄",
            "Bhava chya account madhe khushi ali 🎉"
        )
    )
}
