package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.util.DateUtils
import java.time.LocalDate

/**
 * Pure budget math: DEBIT transactions count as spending, CREDIT does not
 * reduce the budget (shown separately). No Android/Room dependency so this
 * is trivially unit-testable.
 */
object BudgetCalculator {

    fun calculate(
        budget: Double,
        monthTransactions: List<TransactionEntity>,
        today: LocalDate = LocalDate.now()
    ): BudgetSummary {
        val spent = monthTransactions.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
        val credited = monthTransactions.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
        val remaining = budget - spent
        val percentUsed = if (budget > 0) (spent / budget) * 100.0 else 0.0

        val daysInMonth = today.lengthOfMonth()
        val daysRemaining = daysInMonth - today.dayOfMonth + 1 // today counts as remaining
        val recommendedDaily = if (remaining > 0 && daysRemaining > 0) remaining / daysRemaining else 0.0

        val todayRange = DateUtils.dayRangeMillis(today)
        val todaySpending = monthTransactions
            .filter { it.type == TransactionType.DEBIT && it.dateMillis in todayRange }
            .sumOf { it.amount }

        return BudgetSummary(
            budget = budget,
            spent = spent,
            credited = credited,
            remaining = remaining,
            percentUsed = percentUsed,
            daysRemainingInMonth = daysRemaining,
            recommendedDailySpend = recommendedDaily,
            todaySpending = todaySpending
        )
    }
}
