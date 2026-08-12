package com.phapalesai.dhanapala.domain

/** Flavor text for the top-3 spots on the Accounts "shame leaderboard." */
object AccountShameCommentary {

    private val en = mapOf(
        1 to "🏆 Biggest Spender — this account is doing the most damage.",
        2 to "🥈 Runner-up in reckless spending.",
        3 to "🥉 Bronze medal in budget destruction."
    )
    private val hi = mapOf(
        1 to "🏆 Biggest Spender — iss account ne sabse zyada damage kiya hai.",
        2 to "🥈 Reckless spending mein runner-up.",
        3 to "🥉 Budget destruction mein bronze medal."
    )
    private val mr = mapOf(
        1 to "🏆 Biggest Spender — ya account ne sagalyat jasta damage kela ahe.",
        2 to "🥈 Reckless spending madhe runner-up.",
        3 to "🥉 Budget destruction madhe bronze medal."
    )

    fun forRank(rank: Int, language: RoastLanguage): String? {
        val pool = when (language) {
            RoastLanguage.EN -> en
            RoastLanguage.HI -> hi
            RoastLanguage.MR -> mr
        }
        return pool[rank]
    }
}
