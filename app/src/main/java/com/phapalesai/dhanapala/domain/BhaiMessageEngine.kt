package com.phapalesai.dhanapala.domain

import kotlin.random.Random

object BhaiMessageEngine {

    fun spendingReaction(
        amount: Double,
        largeExpenseThreshold: Double,
        random: Random = Random.Default
    ): String = when {
        amount >= largeExpenseThreshold -> BhaiMessages.largeSpend.random(random)
        amount >= 500 -> BhaiMessages.moderateSpend.random(random)
        else -> BhaiMessages.smallSpend.random(random)
    }

    fun budgetReaction(summary: BudgetSummary, random: Random = Random.Default): String = when {
        summary.remaining == 0.0 -> BhaiMessages.zeroRemaining.random(random)
        summary.remaining < 0 -> overBudgetMessage(summary, random)
        summary.percentUsed >= 90 -> BhaiMessages.between90and100.random(random)
        summary.percentUsed >= 75 -> BhaiMessages.between75and90.random(random)
        summary.percentUsed >= 50 -> BhaiMessages.between50and75.random(random)
        else -> BhaiMessages.under50.random(random)
    }

    private fun overBudgetMessage(summary: BudgetSummary, random: Random): String {
        val template = BhaiMessages.overBudget.random(random)
        return if (template.contains("%d")) {
            template.format(summary.budget.toInt(), summary.spent.toInt())
        } else {
            template
        }
    }

    fun salaryMessage(random: Random = Random.Default): String = BhaiMessages.salaryCredit.random(random)

    /**
     * Heuristic only, not a claim of certainty — a big credit that looks like
     * a salary. The caller can also flag this explicitly (e.g. category == Salary).
     */
    fun isLikelySalary(amount: Double, salaryThreshold: Double = 10_000.0): Boolean =
        amount >= salaryThreshold
}
