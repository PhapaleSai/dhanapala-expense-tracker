package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import java.time.LocalDate
import java.time.ZoneId

data class Badge(val id: String, val emoji: String, val title: String, val subtitle: String)

object StreakCalculator {

    private val zone = ZoneId.systemDefault()

    /** Consecutive days ending today with zero DEBIT spend recorded. */
    fun currentZeroSpendStreak(transactions: List<TransactionEntity>, today: LocalDate = LocalDate.now()): Int {
        val debitDays = transactions
            .filter { it.type == TransactionType.DEBIT }
            .map { java.time.Instant.ofEpochMilli(it.dateMillis).atZone(zone).toLocalDate() }
            .toSet()
        var streak = 0
        var day = today
        while (day !in debitDays) {
            streak++
            day = day.minusDays(1)
            if (streak > 3650) break
        }
        return streak
    }

    fun earnedBadges(transactions: List<TransactionEntity>, today: LocalDate = LocalDate.now()): List<Badge> {
        val streak = currentZeroSpendStreak(transactions, today)
        val badges = mutableListOf<Badge>()
        if (streak >= 3) badges.add(Badge("streak_3", "🔥", "3-Day Streak", "Zero-spend for 3 days straight"))
        if (streak >= 7) badges.add(Badge("streak_7", "🔥🔥", "7-Day Streak", "A full week without a single debit"))
        if (streak >= 30) badges.add(Badge("streak_30", "🏆", "30-Day Streak", "A whole month of zero-spend discipline"))

        val lastSunday = today.minusDays((today.dayOfWeek.value % 7).toLong())
        val sundayHadDebit = transactions.any {
            it.type == TransactionType.DEBIT &&
                java.time.Instant.ofEpochMilli(it.dateMillis).atZone(zone).toLocalDate() == lastSunday
        }
        if (!sundayHadDebit) {
            badges.add(Badge("upi_free_sunday", "🧘", "UPI-Free Sunday", "Survived Sunday without a single UPI hit"))
        }
        return badges
    }
}
