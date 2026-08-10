package com.phapalesai.dhanapala.domain

import kotlin.random.Random

object BhaiMessageEngine {

    fun spendingReaction(
        amount: Double,
        largeExpenseThreshold: Double,
        language: RoastLanguage = RoastLanguage.HI,
        level: RoastLevel = RoastLevel.MEDIUM,
        random: Random = Random.Default
    ): String {
        val category = when {
            amount >= largeExpenseThreshold -> RoastCategory.LARGE_SPEND
            amount >= 500 -> RoastCategory.MODERATE_SPEND
            else -> RoastCategory.SMALL_SPEND
        }
        return pick(language, level, category, random)
    }

    fun budgetReaction(
        summary: BudgetSummary,
        language: RoastLanguage = RoastLanguage.HI,
        level: RoastLevel = RoastLevel.MEDIUM,
        random: Random = Random.Default
    ): String {
        if (summary.remaining == 0.0) return pick(language, level, RoastCategory.ZERO_REMAINING, random)
        val category = when {
            summary.remaining < 0 -> RoastCategory.OVER_BUDGET
            summary.percentUsed >= 90 -> RoastCategory.BETWEEN_90_100
            summary.percentUsed >= 75 -> RoastCategory.BETWEEN_75_90
            summary.percentUsed >= 50 -> RoastCategory.BETWEEN_50_75
            else -> RoastCategory.UNDER_50
        }
        val template = pick(language, level, category, random)
        return if (category == RoastCategory.OVER_BUDGET && template.contains("%d")) {
            template.format(summary.budget.toInt(), summary.spent.toInt())
        } else {
            template
        }
    }

    fun salaryMessage(language: RoastLanguage = RoastLanguage.HI, random: Random = Random.Default): String =
        (BhaiMessages.salaryMessages[language] ?: BhaiMessages.salaryMessages.getValue(RoastLanguage.HI)).random(random)

    /**
     * Heuristic only, not a claim of certainty — a big credit that looks like
     * a salary. The caller can also flag this explicitly (e.g. category == Salary).
     */
    fun isLikelySalary(amount: Double, salaryThreshold: Double = 10_000.0): Boolean =
        amount >= salaryThreshold

    private fun pick(language: RoastLanguage, level: RoastLevel, category: RoastCategory, random: Random): String {
        val pool = BhaiMessages.pool(language, level, category)
        return if (pool.isEmpty()) "" else pool.random(random)
    }
}
