package com.phapalesai.dhanapala.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class BhaiMessageEngineTest {

    private fun summary(budget: Double, spent: Double) = BudgetSummary(
        budget = budget,
        spent = spent,
        credited = 0.0,
        remaining = budget - spent,
        percentUsed = if (budget > 0) spent / budget * 100.0 else 0.0,
        daysRemainingInMonth = 10,
        recommendedDailySpend = 0.0,
        todaySpending = 0.0
    )

    @Test
    fun `small spend picks from small pool`() {
        val msg = BhaiMessageEngine.spendingReaction(amount = 100.0, largeExpenseThreshold = 1000.0)
        assertTrue(msg in BhaiMessages.pool(RoastLanguage.HI, RoastLevel.MEDIUM, RoastCategory.SMALL_SPEND))
    }

    @Test
    fun `spend at or above threshold picks from large pool`() {
        val msg = BhaiMessageEngine.spendingReaction(amount = 1500.0, largeExpenseThreshold = 1000.0)
        assertTrue(msg in BhaiMessages.pool(RoastLanguage.HI, RoastLevel.MEDIUM, RoastCategory.LARGE_SPEND))
    }

    @Test
    fun `under 50 percent picks safe pool`() {
        val msg = BhaiMessageEngine.budgetReaction(summary(budget = 5000.0, spent = 1000.0))
        assertTrue(msg in BhaiMessages.pool(RoastLanguage.HI, RoastLevel.MEDIUM, RoastCategory.UNDER_50))
    }

    @Test
    fun `90 to 100 percent picks danger pool`() {
        val msg = BhaiMessageEngine.budgetReaction(summary(budget = 5000.0, spent = 4600.0))
        assertTrue(msg in BhaiMessages.pool(RoastLanguage.HI, RoastLevel.MEDIUM, RoastCategory.BETWEEN_90_100))
    }

    @Test
    fun `over budget formats numbers into the template message`() {
        val msg = BhaiMessageEngine.budgetReaction(summary(budget = 5000.0, spent = 5347.0))
        val pool = BhaiMessages.pool(RoastLanguage.HI, RoastLevel.MEDIUM, RoastCategory.OVER_BUDGET)
        assertTrue(msg in pool || msg.contains("5000"))
    }

    @Test
    fun `exactly zero remaining picks the zero pool`() {
        val msg = BhaiMessageEngine.budgetReaction(summary(budget = 5000.0, spent = 5000.0))
        assertTrue(msg in BhaiMessages.pool(RoastLanguage.HI, RoastLevel.MEDIUM, RoastCategory.ZERO_REMAINING))
    }

    @Test
    fun `english language picks from english pool`() {
        val msg = BhaiMessageEngine.spendingReaction(
            amount = 100.0,
            largeExpenseThreshold = 1000.0,
            language = RoastLanguage.EN
        )
        assertTrue(msg in BhaiMessages.pool(RoastLanguage.EN, RoastLevel.MEDIUM, RoastCategory.SMALL_SPEND))
    }

    @Test
    fun `savage level picks from savage pool not medium`() {
        val msg = BhaiMessageEngine.budgetReaction(
            summary(budget = 5000.0, spent = 4600.0),
            level = RoastLevel.SAVAGE
        )
        assertTrue(msg in BhaiMessages.pool(RoastLanguage.HI, RoastLevel.SAVAGE, RoastCategory.BETWEEN_90_100))
    }

    @Test
    fun `large credit is flagged as likely salary`() {
        assertTrue(BhaiMessageEngine.isLikelySalary(amount = 45000.0))
    }

    @Test
    fun `small credit is not flagged as salary`() {
        assertTrue(!BhaiMessageEngine.isLikelySalary(amount = 200.0))
    }
}
