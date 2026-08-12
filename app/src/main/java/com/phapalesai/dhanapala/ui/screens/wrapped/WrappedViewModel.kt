package com.phapalesai.dhanapala.ui.screens.wrapped

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.domain.BhaiMessageEngine
import com.phapalesai.dhanapala.domain.StreakCalculator
import com.phapalesai.dhanapala.domain.roastLanguageEnum
import com.phapalesai.dhanapala.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random

data class WrappedStats(
    val periodLabel: String = "",
    val totalSpent: Double = 0.0,
    val topCategory: String? = null,
    val topCategoryAmount: Double = 0.0,
    val biggestExpenseCategory: String? = null,
    val biggestExpenseAmount: Double = 0.0,
    val transactionCount: Int = 0,
    val zeroSpendStreak: Int = 0,
    val highlightLine: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class WrappedViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val zone = ZoneId.systemDefault()

    private val activeBudget = app.budgetRepository.observeActive(System.currentTimeMillis())
    private val periodTransactions = activeBudget.flatMapLatest { budget ->
        val range = budget?.let { it.startDateMillis to it.endDateMillis }
            ?: DateUtils.monthRangeMillis(YearMonth.now()).let { it.first to it.last }
        app.transactionRepository.observeBetween(range.first, range.second)
    }

    val stats: StateFlow<WrappedStats> = combine(
        periodTransactions,
        activeBudget,
        app.transactionRepository.observeAll(),
        app.budgetRepository.observeSettings()
    ) { transactions, budgetEntity, allTransactions, settings ->
        val periodStart = budgetEntity?.let { Instant.ofEpochMilli(it.startDateMillis).atZone(zone).toLocalDate() }
        val periodEnd = budgetEntity?.let { Instant.ofEpochMilli(it.endDateMillis).atZone(zone).toLocalDate() }
        val formatter = DateTimeFormatter.ofPattern("d MMM")
        val periodLabel = if (periodStart != null && periodEnd != null) {
            "${periodStart.format(formatter)} – ${periodEnd.format(formatter)}"
        } else {
            YearMonth.now().month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + YearMonth.now().year
        }

        val debits = transactions.filter { it.type == TransactionType.DEBIT }
        val totalSpent = debits.sumOf { it.amount }
        val topCategory = debits.groupBy { it.category }.maxByOrNull { (_, txs) -> txs.sumOf { it.amount } }
        val biggest = debits.maxByOrNull { it.amount }
        val streak = StreakCalculator.currentZeroSpendStreak(allTransactions, LocalDate.now())

        val highlight = BhaiMessageEngine.spendingReaction(
            amount = totalSpent,
            largeExpenseThreshold = settings.largeExpenseThreshold,
            language = settings.roastLanguageEnum,
            random = Random(periodLabel.hashCode())
        )

        WrappedStats(
            periodLabel = periodLabel,
            totalSpent = totalSpent,
            topCategory = topCategory?.key,
            topCategoryAmount = topCategory?.value?.sumOf { it.amount } ?: 0.0,
            biggestExpenseCategory = biggest?.category,
            biggestExpenseAmount = biggest?.amount ?: 0.0,
            transactionCount = debits.size,
            zeroSpendStreak = streak,
            highlightLine = highlight
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WrappedStats())
}
