package com.phapalesai.dhanapala.ui.screens.quickbudget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.AppSettingsEntity
import com.phapalesai.dhanapala.domain.BudgetCalculator
import com.phapalesai.dhanapala.domain.BudgetSummary
import com.phapalesai.dhanapala.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/** Read-only budget snapshot shared by the Panic Button and Broke-o-Meter screens — neither edits data. */
@OptIn(ExperimentalCoroutinesApi::class)
class QuickBudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val budgetRepo = app.budgetRepository
    private val transactionRepo = app.transactionRepository
    private val zone = ZoneId.systemDefault()

    val summary: StateFlow<BudgetSummary> = budgetRepo.observeActive(System.currentTimeMillis())
        .flatMapLatest { budget ->
            val range = budget?.let { it.startDateMillis to it.endDateMillis }
                ?: DateUtils.monthRangeMillis(YearMonth.now()).let { it.first to it.last }
            transactionRepo.observeBetween(range.first, range.second).map { transactions ->
                val periodEnd = budget?.let { Instant.ofEpochMilli(it.endDateMillis).atZone(zone).toLocalDate() }
                if (budget != null && periodEnd != null) {
                    BudgetCalculator.calculate(budget.amount, transactions, periodEnd = periodEnd)
                } else {
                    BudgetCalculator.calculate(0.0, transactions)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetCalculator.calculate(0.0, emptyList()))

    val settings: StateFlow<AppSettingsEntity> = budgetRepo.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsEntity())

    fun recordImpulseAvoided() {
        viewModelScope.launch {
            val current = budgetRepo.observeSettings().first()
            budgetRepo.updateSettings(current.copy(impulsesAvoided = current.impulsesAvoided + 1))
        }
    }
}
