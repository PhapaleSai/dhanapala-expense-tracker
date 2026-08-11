package com.phapalesai.dhanapala.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.data.repository.ScanResult
import com.phapalesai.dhanapala.domain.BhaiMessageEngine
import com.phapalesai.dhanapala.domain.BudgetCalculator
import com.phapalesai.dhanapala.domain.MoneyJokes
import com.phapalesai.dhanapala.domain.MoneySavingTips
import com.phapalesai.dhanapala.domain.WelcomeMessages
import com.phapalesai.dhanapala.domain.roastLanguageEnum
import com.phapalesai.dhanapala.domain.roastLevelEnum
import com.phapalesai.dhanapala.util.DateUtils
import com.phapalesai.dhanapala.widget.WidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.random.Random

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val transactionRepo = app.transactionRepository
    private val budgetRepo = app.budgetRepository
    private val categoryBudgetRepo = app.categoryBudgetRepository
    private val smsReader = app.smsReader
    private val alertService = app.transactionAlertService
    private val jokeSeed = Random.nextInt()
    private val welcomeSeed = Random.nextInt()

    private val zone = ZoneId.systemDefault()
    private val nowMillis = System.currentTimeMillis()

    private val _isScanning = MutableStateFlow(false)
    private val _lastScanResult = MutableStateFlow<ScanResult?>(null)
    private val _hasSmsPermission = MutableStateFlow(false)
    private val scanState = combine(_isScanning, _lastScanResult) { scanning, result -> scanning to result }

    private val activeBudget = budgetRepo.observeActive(nowMillis)

    private val periodTransactions = activeBudget.flatMapLatest { budget ->
        val range = budget?.let { it.startDateMillis to it.endDateMillis }
            ?: DateUtils.monthRangeMillis(YearMonth.now()).let { it.first to it.last }
        transactionRepo.observeBetween(range.first, range.second)
    }

    private val categoryBudgetsForActivePeriod = activeBudget.flatMapLatest { budget ->
        budget?.let { categoryBudgetRepo.observeForBudget(it.id) } ?: flowOf(emptyList())
    }

    // combine() tops out at 5 positional flows, so hasSmsPermission and the
    // category budgets list are bundled into one extra flow.
    private val extras = combine(_hasSmsPermission, categoryBudgetsForActivePeriod) { hasSmsPermission, categoryBudgets ->
        hasSmsPermission to categoryBudgets
    }

    val uiState: StateFlow<HomeUiState> = combine(
        periodTransactions,
        activeBudget,
        budgetRepo.observeSettings(),
        scanState,
        extras
    ) { transactions, budgetEntity, settings, (isScanning, lastScanResult), (hasSmsPermission, categoryBudgets) ->
        val periodStart = budgetEntity?.let {
            java.time.Instant.ofEpochMilli(it.startDateMillis).atZone(zone).toLocalDate()
        }
        val periodEnd = budgetEntity?.let {
            java.time.Instant.ofEpochMilli(it.endDateMillis).atZone(zone).toLocalDate()
        }
        val summary = if (periodEnd != null) {
            BudgetCalculator.calculate(budgetEntity.amount, transactions, periodEnd = periodEnd)
        } else {
            BudgetCalculator.calculate(0.0, transactions)
        }
        val categoryBudgetProgress = categoryBudgets.map { cb ->
            val spent = transactions
                .filter { it.type == TransactionType.DEBIT && it.category == cb.category }
                .sumOf { it.amount }
            CategoryBudgetProgress(id = cb.id, category = cb.category, budget = cb.amount, spent = spent)
        }
        HomeUiState(
            summary = summary,
            recentTransactions = transactions.take(5),
            bhaiMessage = if (settings.bhaiModeEnabled) {
                BhaiMessageEngine.budgetReaction(summary, settings.roastLanguageEnum, settings.roastLevelEnum)
            } else {
                null
            },
            hasBudgetSet = budgetEntity != null,
            settings = settings,
            isScanning = isScanning,
            lastScanResult = lastScanResult,
            hasSmsPermission = hasSmsPermission,
            moneyTip = moneyTipFor(transactions),
            moneyJoke = MoneyJokes.random(settings.roastLanguageEnum, Random(jokeSeed)),
            welcomeMessage = WelcomeMessages.random(settings.roastLanguageEnum, Random(welcomeSeed)),
            periodStart = periodStart,
            periodEnd = periodEnd,
            categoryBudgets = categoryBudgetProgress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    private fun moneyTipFor(transactions: List<TransactionEntity>): String {
        val recentCategory = transactions.firstOrNull { it.type == TransactionType.DEBIT }?.category
        val today = LocalDate.now().toEpochDay()
        val seed = today + (recentCategory?.hashCode() ?: 0)
        return MoneySavingTips.random(recentCategory, Random(seed))
    }

    fun onSmsPermissionResult(granted: Boolean) {
        _hasSmsPermission.value = granted
        if (granted) scanSms()
    }

    fun scanSms() {
        viewModelScope.launch {
            _isScanning.value = true
            val messages = smsReader.readInbox(limit = 1000)
            val result = transactionRepo.scanMessages(messages)
            _lastScanResult.value = result
            _isScanning.value = false
            alertService.notifyForScan(result)
        }
    }

    /** Sets the amount for the currently active period (or creates a calendar-month one if none exists yet). */
    fun setBudget(amount: Double) {
        viewModelScope.launch {
            val active = budgetRepo.getActiveOnce(nowMillis)
            if (active != null) {
                budgetRepo.setBudget(active.startDateMillis, active.endDateMillis, amount, existingId = active.id)
            } else {
                val range = DateUtils.monthRangeMillis(YearMonth.now())
                budgetRepo.setBudget(range.first, range.last, amount)
            }
            WidgetUpdater.refresh(getApplication())
        }
    }

    /** Creates a brand new budget for an explicit custom date range. */
    fun setCustomBudget(startDate: LocalDate, endDate: LocalDate, amount: Double) {
        viewModelScope.launch {
            val start = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
            budgetRepo.setBudget(start, end, amount)
            WidgetUpdater.refresh(getApplication())
        }
    }

    /** Edits the currently active period in place — amount and/or its date range. */
    fun editActiveBudgetPeriod(startDate: LocalDate, endDate: LocalDate, amount: Double) {
        viewModelScope.launch {
            val active = budgetRepo.getActiveOnce(nowMillis)
            val start = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
            budgetRepo.setBudget(start, end, amount, existingId = active?.id)
            WidgetUpdater.refresh(getApplication())
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            val current = budgetRepo.observeSettings().first()
            budgetRepo.updateSettings(current.copy(userName = name.trim()))
        }
    }

    /** Creates or replaces a category's limit for the currently active budget period. */
    fun setCategoryBudget(category: String, amount: Double) {
        viewModelScope.launch {
            val active = budgetRepo.getActiveOnce(nowMillis) ?: return@launch
            val existing = categoryBudgetRepo.getForBudget(active.id).firstOrNull { it.category == category }
            categoryBudgetRepo.setLimit(active.id, category, amount, existingId = existing?.id)
        }
    }

    fun deleteCategoryBudget(id: Long) {
        viewModelScope.launch { categoryBudgetRepo.delete(id) }
    }

}
