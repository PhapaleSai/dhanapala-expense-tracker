package com.phapalesai.dhanapala.ui.screens.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.domain.BudgetCalculator
import com.phapalesai.dhanapala.domain.SpendingInsights
import com.phapalesai.dhanapala.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class CategorySpend(val category: String, val amount: Double, val percentOfSpend: Double)
data class DailySpend(val date: LocalDate, val amount: Double)

data class AnalyticsUiState(
    val byCategory: List<CategorySpend> = emptyList(),
    val byDay: List<DailySpend> = emptyList(),
    val budget: Double = 0.0,
    val spent: Double = 0.0,
    val credited: Double = 0.0,
    val remaining: Double = 0.0,
    val percentUsed: Double = 0.0,
    val transactionCount: Int = 0,
    val avgDailySpend: Double = 0.0,
    val topCategory: CategorySpend? = null,
    val topDay: DailySpend? = null,
    val projectedPeriodEndSpend: Double = 0.0,
    val weekdayAvgSpend: Double = 0.0,
    val weekendAvgSpend: Double = 0.0
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val zone = ZoneId.systemDefault()
    private val nowMillis = System.currentTimeMillis()

    private val activeBudget = app.budgetRepository.observeActive(nowMillis)
    private val periodTransactions = activeBudget.flatMapLatest { budget ->
        val range = budget?.let { it.startDateMillis to it.endDateMillis }
            ?: DateUtils.monthRangeMillis(YearMonth.now()).let { it.first to it.last }
        app.transactionRepository.observeBetween(range.first, range.second)
    }

    val uiState: StateFlow<AnalyticsUiState> = combine(periodTransactions, activeBudget) { transactions, budgetEntity ->
        val debits = transactions.filter { it.type == TransactionType.DEBIT }
        val totalSpend = debits.sumOf { it.amount }.coerceAtLeast(0.01)

        val byCategory = debits
            .groupBy { it.category }
            .map { (category, txs) ->
                val amount = txs.sumOf { it.amount }
                CategorySpend(category, amount, (amount / totalSpend) * 100.0)
            }
            .sortedByDescending { it.amount }

        val byDay = debits
            .groupBy { Instant.ofEpochMilli(it.dateMillis).atZone(zone).toLocalDate() }
            .map { (date, txs) -> DailySpend(date, txs.sumOf { it.amount }) }
            .sortedBy { it.date }

        val periodStart = budgetEntity?.let { Instant.ofEpochMilli(it.startDateMillis).atZone(zone).toLocalDate() }
        val periodEnd = budgetEntity?.let { Instant.ofEpochMilli(it.endDateMillis).atZone(zone).toLocalDate() }
        val summary = if (periodEnd != null) {
            BudgetCalculator.calculate(budgetEntity.amount, transactions, periodEnd = periodEnd)
        } else {
            BudgetCalculator.calculate(0.0, transactions)
        }
        val daysElapsed = byDay.map { it.date }.distinct().size.coerceAtLeast(1)

        val today = LocalDate.now()
        val projectedPeriodEndSpend = if (periodStart != null && periodEnd != null) {
            SpendingInsights.projectPeriodEndSpend(summary.spent, periodStart, periodEnd, today)
        } else {
            summary.spent
        }
        val weekdayWeekend = SpendingInsights.weekdayVsWeekendAverage(byDay.associate { it.date to it.amount })

        AnalyticsUiState(
            byCategory = byCategory,
            byDay = byDay,
            budget = summary.budget,
            spent = summary.spent,
            credited = summary.credited,
            remaining = summary.remaining,
            percentUsed = summary.percentUsed,
            transactionCount = debits.size,
            avgDailySpend = summary.spent / daysElapsed,
            topCategory = byCategory.maxByOrNull { it.amount },
            topDay = byDay.maxByOrNull { it.amount },
            projectedPeriodEndSpend = projectedPeriodEndSpend,
            weekdayAvgSpend = weekdayWeekend.weekdayAvg,
            weekendAvgSpend = weekdayWeekend.weekendAvg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())
}
