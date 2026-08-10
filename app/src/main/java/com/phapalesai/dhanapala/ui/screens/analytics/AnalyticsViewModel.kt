package com.phapalesai.dhanapala.ui.screens.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.domain.BudgetCalculator
import com.phapalesai.dhanapala.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class CategorySpend(val category: String, val amount: Double)
data class DailySpend(val date: LocalDate, val amount: Double)

data class AnalyticsUiState(
    val byCategory: List<CategorySpend> = emptyList(),
    val byDay: List<DailySpend> = emptyList(),
    val budget: Double = 0.0,
    val spent: Double = 0.0,
    val remaining: Double = 0.0
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val monthKey = DateUtils.monthKey()
    private val monthRange = DateUtils.monthRangeMillis(YearMonth.now())

    val uiState: StateFlow<AnalyticsUiState> = combine(
        app.transactionRepository.observeBetween(monthRange.first, monthRange.last),
        app.budgetRepository.observeBudget(monthKey)
    ) { transactions, budgetEntity ->
        val debits = transactions.filter { it.type == TransactionType.DEBIT }
        val byCategory = debits
            .groupBy { it.category }
            .map { (category, txs) -> CategorySpend(category, txs.sumOf { it.amount }) }
            .sortedByDescending { it.amount }

        val byDay = debits
            .groupBy {
                Instant.ofEpochMilli(it.dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            }
            .map { (date, txs) -> DailySpend(date, txs.sumOf { it.amount }) }
            .sortedBy { it.date }

        val summary = BudgetCalculator.calculate(budgetEntity?.amount ?: 0.0, transactions)
        AnalyticsUiState(
            byCategory = byCategory,
            byDay = byDay,
            budget = summary.budget,
            spent = summary.spent,
            remaining = summary.remaining
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())
}
