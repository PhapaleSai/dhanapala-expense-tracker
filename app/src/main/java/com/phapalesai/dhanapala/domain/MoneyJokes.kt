package com.phapalesai.dhanapala.domain

import kotlin.random.Random

/**
 * Light, harmless money jokes shown once per app session -- pure fun, no
 * roast/judgment (that's Bhai Mode's job). Picked with a caller-supplied
 * Random so it stays stable for the lifetime of one HomeViewModel instance
 * instead of changing on every recomposition.
 */
object MoneyJokes {

    private val en = listOf(
        "Why don't bankers ever get lost? They always follow the interest.",
        "I told my wallet a joke. It didn't laugh -- it's been empty for weeks.",
        "Money can't buy happiness, but it can buy snacks, and that's basically the same thing.",
        "Why did the piggy bank go to therapy? Too many people kept breaking it.",
        "I'm not saying I'm broke, but my wallet just filed for emotional support.",
        "A budget is just a wish list with rules attached.",
        "ATMs are the only machines where you insert money and still walk away sad.",
        "I asked my bank for a loan. They asked if I was joking. I said no, that's your job.",
        "My savings account and I have a great relationship -- we barely talk.",
        "I've started saving for retirement. Should be done by the year 3025.",
        "My bank statement is basically a horror novel with monthly sequels.",
        "The best financial advice I ever got: don't check your balance right after payday.",
        "Credit cards are just adult IOUs with better graphics.",
        "I'm on a seafood diet -- I see food delivery apps and my money disappears.",
        "Why did the rupee break up with the dollar? Too much exchange rate drama.",
        "My budgeting app sends alerts. My bank account sends condolences.",
        "I don't chase money. It's faster than me and better at cardio.",
        "Every time I make a budget, my expenses read it as a challenge."
    )

    private val hi = listOf(
        "Salary aayi thi kal, ab wo bhi UPI ke bhoot ban gayi.",
        "Bank balance dekhne ka matlab hai apna dil todna, free mein.",
        "Budget banaya tha is mahine ke liye, wo bhi 5 tarikh ko retire ho gaya.",
        "ATM se paisa nikalna aur usse wapas jama karna -- dono hi ek jaisa dukhta hai.",
        "Mera wallet aur mera dil dono hi khaali hain is mahine.",
        "EMI aisi cheez hai jo birthday se zyada regularly yaad aati hai.",
        "Savings account khola tha bachat ke liye, ab wo bhi Swiggy ka fan ban gaya.",
        "Paisa bacha ke rakha tha, phir sale aa gayi -- RIP savings.",
        "Credit card limit dekh ke lagta hai bank mujhse zyada mujh pe trust karta hai.",
        "Mahine ke start mein main CEO hota hoon, end mein intern bhi nahi bacha.",
        "Bhai ne kaha invest karo, maine socha 'invest' Zomato mein bhi hota hai kya.",
        "Paisa udta hai ye sunte the, ab dekh bhi liya -- seedha wallet se bahar.",
        "Salary din pe main rich, 15 tarikh ko main philosopher -- sab kuch illusion hai.",
        "Budget app bola '80% kharch ho gaya', maine bola tujhe kisne bulaya.",
        "Mummy bolti hai bachat karo, main bolta hoon UPI hi meri bachat hai.",
        "Paisa hath ka mail hai -- bas mere hath bahut zyada dhote hain."
    )

    private val mr = listOf(
        "Pagar aali ki don divsat crorepati zalyasarkhi feeling yete, mag pun wallet rikaam.",
        "Budget banavla hota, to pan 5 tarkhela sutti gheun basla.",
        "Bank balance baghitla ki manala shanti nahi, dukhach jaast hoto.",
        "Salary divshi mi seth, mahinya akheris mi bhikari -- hach normal cycle ahe.",
        "ATM cha vapar itka vadhla ki tyala mazha naav pathach ahe.",
        "Savings account ughadla hota bachativa mhanun, to pan aata Swiggy cha fan zala.",
        "EMI hi ashi goshta ahe ji birthday peksha jasta niyamit aathvan karun dete.",
        "Paisa udto mhanaycha, aata swatah baghitla -- seedha khishatun baher.",
        "Credit card cha limit baghun vatta bank mazyahun jasta majhyavar vishwas thevto.",
        "Mummy mhanate bachat kar, mi mhanto UPI hich mazi bachat ahe.",
        "Budget app ne message pathavla '80% kharch zala', mi mhanto tula koni bolavla hota ka.",
        "Pagar zali ki mi philosopher hoto -- sagla kahi tatpurta ahe."
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
