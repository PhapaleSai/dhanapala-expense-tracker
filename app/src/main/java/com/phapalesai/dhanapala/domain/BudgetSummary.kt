package com.phapalesai.dhanapala.domain

data class BudgetSummary(
    val budget: Double,
    val spent: Double,
    val credited: Double,
    val remaining: Double,
    val percentUsed: Double,
    val daysRemainingInMonth: Int,
    val recommendedDailySpend: Double,
    val todaySpending: Double
)
