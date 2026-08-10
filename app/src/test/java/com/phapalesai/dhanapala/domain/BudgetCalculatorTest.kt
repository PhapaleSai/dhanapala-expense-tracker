package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class BudgetCalculatorTest {

    private fun tx(amount: Double, type: TransactionType, date: LocalDate) = TransactionEntity(
        amount = amount,
        type = type,
        dateMillis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() + 1000,
        sender = "AX-BANK",
        merchant = null,
        description = null,
        sourceSmsId = null,
        dedupeHash = "$amount-$type-$date",
        category = "Uncategorized",
        isManual = false,
        createdAt = 0
    )

    @Test
    fun `credits do not reduce spending, only debits do`() {
        val today = LocalDate.of(2026, 8, 10)
        val transactions = listOf(
            tx(500.0, TransactionType.DEBIT, today),
            tx(300.0, TransactionType.DEBIT, today),
            tx(2000.0, TransactionType.CREDIT, today),
            tx(200.0, TransactionType.DEBIT, today)
        )

        val summary = BudgetCalculator.calculate(budget = 5000.0, periodTransactions = transactions, today = today)

        assertEquals(1000.0, summary.spent, 0.001)
        assertEquals(2000.0, summary.credited, 0.001)
        assertEquals(4000.0, summary.remaining, 0.001)
    }

    @Test
    fun `percent used and recommended daily spend`() {
        val today = LocalDate.of(2026, 8, 10) // August has 31 days, so 22 days remain including today
        val transactions = listOf(tx(1760.0, TransactionType.DEBIT, today))

        val summary = BudgetCalculator.calculate(budget = 5000.0, periodTransactions = transactions, today = today)

        assertEquals(35.2, summary.percentUsed, 0.01)
        assertEquals(22, summary.daysRemainingInMonth)
        assertEquals(3240.0 / 22.0, summary.recommendedDailySpend, 0.01)
    }

    @Test
    fun `over budget gives zero recommended daily spend not negative`() {
        val today = LocalDate.of(2026, 8, 10)
        val transactions = listOf(tx(6000.0, TransactionType.DEBIT, today))

        val summary = BudgetCalculator.calculate(budget = 5000.0, periodTransactions = transactions, today = today)

        assertEquals(-1000.0, summary.remaining, 0.001)
        assertEquals(0.0, summary.recommendedDailySpend, 0.001)
    }

    @Test
    fun `today spending only counts todays debits`() {
        val today = LocalDate.of(2026, 8, 10)
        val yesterday = today.minusDays(1)
        val transactions = listOf(
            tx(430.0, TransactionType.DEBIT, today),
            tx(999.0, TransactionType.DEBIT, yesterday)
        )

        val summary = BudgetCalculator.calculate(budget = 5000.0, periodTransactions = transactions, today = today)

        assertEquals(430.0, summary.todaySpending, 0.001)
    }

    @Test
    fun `custom period end overrides the calendar-month default`() {
        // Custom period: 10 Aug - 20 Aug. Today is 10 Aug, so 11 days remain including today.
        val today = LocalDate.of(2026, 8, 10)
        val periodEnd = LocalDate.of(2026, 8, 20)
        val transactions = listOf(tx(500.0, TransactionType.DEBIT, today))

        val summary = BudgetCalculator.calculate(
            budget = 1100.0,
            periodTransactions = transactions,
            today = today,
            periodEnd = periodEnd
        )

        assertEquals(11, summary.daysRemainingInMonth)
        assertEquals(600.0 / 11.0, summary.recommendedDailySpend, 0.01)
    }
}
