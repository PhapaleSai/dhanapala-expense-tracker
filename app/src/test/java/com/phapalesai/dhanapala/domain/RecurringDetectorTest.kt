package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class RecurringDetectorTest {

    private val zone = ZoneId.systemDefault()

    private fun tx(daysAgo: Long, amount: Double, category: String = "Entertainment", manual: Boolean = false) =
        TransactionEntity(
            amount = amount,
            type = TransactionType.DEBIT,
            dateMillis = LocalDate.now().minusDays(daysAgo).atStartOfDay(zone).toInstant().toEpochMilli(),
            sender = "TEST-BANK",
            merchant = null,
            description = "Netflix subscription",
            sourceSmsId = "id-$daysAgo",
            dedupeHash = "hash-$daysAgo-$amount",
            category = category,
            isManual = manual,
            createdAt = System.currentTimeMillis()
        )

    @Test
    fun `three monthly-spaced same-amount debits are flagged as recurring`() {
        val transactions = listOf(tx(0, 649.0), tx(30, 649.0), tx(60, 649.0))
        val result = RecurringDetector.detect(transactions)
        assertEquals(1, result.size)
        assertEquals(649.0, result[0].amount, 0.01)
        assertTrue(result[0].isMonthly)
        assertEquals(3, result[0].occurrences)
    }

    @Test
    fun `a single occurrence is not recurring`() {
        val result = RecurringDetector.detect(listOf(tx(0, 649.0)))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `irregularly-spaced same-amount debits are not flagged`() {
        val transactions = listOf(tx(0, 200.0), tx(5, 200.0), tx(9, 200.0))
        val result = RecurringDetector.detect(transactions)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `manual entries are excluded from detection`() {
        val transactions = listOf(tx(0, 649.0, manual = true), tx(30, 649.0, manual = true), tx(60, 649.0, manual = true))
        val result = RecurringDetector.detect(transactions)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `estimated monthly total converts weekly items to a monthly equivalent`() {
        val items = listOf(
            RecurringItem("Food", 100.0, 3, LocalDate.now(), LocalDate.now(), isMonthly = true),
            RecurringItem("Fuel", 50.0, 3, LocalDate.now(), LocalDate.now(), isMonthly = false)
        )
        val total = RecurringDetector.estimatedMonthlyTotal(items)
        assertEquals(100.0 + 50.0 * 4.33, total, 0.01)
    }
}
