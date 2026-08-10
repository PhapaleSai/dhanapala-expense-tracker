package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.util.DateUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Pure budget math: DEBIT transactions count as spending, CREDIT does not
 * reduce the budget (shown separately). No Android/Room dependency so this
 * is trivially unit-testable.
 */
object BudgetCalculator {

    /**
     * @param periodEnd the last day the budget covers — defaults to the end
     * of the current calendar month, but callers with a custom budget period
     * (e.g. 10 Aug – 25 Aug) pass that period's own end date instead.
     */
    fun calculate(
        budget: Double,
        periodTransactions: List<TransactionEntity>,
        today: LocalDate = LocalDate.now(),
        periodEnd: LocalDate = today.withDayOfMonth(today.lengthOfMonth())
    ): BudgetSummary {
        val spent = periodTransactions.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
        val credited = periodTransactions.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
        val remaining = budget - spent
        val percentUsed = if (budget > 0) (spent / budget) * 100.0 else 0.0

        val daysRemaining = (ChronoUnit.DAYS.between(today, periodEnd).toInt() + 1).coerceAtLeast(0)
        val recommendedDaily = if (remaining > 0 && daysRemaining > 0) remaining / daysRemaining else 0.0

        val todayRange = DateUtils.dayRangeMillis(today)
        val todaySpending = periodTransactions
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
