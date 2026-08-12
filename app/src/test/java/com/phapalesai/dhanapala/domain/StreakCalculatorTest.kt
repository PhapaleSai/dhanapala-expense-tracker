package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StreakCalculatorTest {

    private val zone = ZoneId.systemDefault()
    private val today = LocalDate.of(2026, 8, 12)

    private fun debitOn(date: LocalDate) = TransactionEntity(
        amount = 100.0,
        type = TransactionType.DEBIT,
        dateMillis = date.atStartOfDay(zone).toInstant().toEpochMilli(),
        sender = "TEST-BANK",
        merchant = null,
        description = "test",
        sourceSmsId = "id-$date",
        dedupeHash = "hash-$date",
        category = "Food",
        isManual = false,
        createdAt = System.currentTimeMillis()
    )

    @Test
    fun `no transactions at all counts as a long streak`() {
        val streak = StreakCalculator.currentZeroSpendStreak(emptyList(), today)
        assertTrue(streak > 100)
    }

    @Test
    fun `a debit today breaks the streak immediately`() {
        val streak = StreakCalculator.currentZeroSpendStreak(listOf(debitOn(today)), today)
        assertEquals(0, streak)
    }

    @Test
    fun `a debit three days ago caps the streak at three`() {
        val transactions = listOf(debitOn(today.minusDays(3)))
        val streak = StreakCalculator.currentZeroSpendStreak(transactions, today)
        assertEquals(3, streak)
    }

    @Test
    fun `seven day streak earns the 7-day badge but not the 30-day badge`() {
        val transactions = listOf(debitOn(today.minusDays(10)))
        val badges = StreakCalculator.earnedBadges(transactions, today)
        assertTrue(badges.any { it.id == "streak_7" })
        assertTrue(badges.none { it.id == "streak_30" })
    }
}
