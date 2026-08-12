package com.phapalesai.dhanapala.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.data.repository.ScanResult
import com.phapalesai.dhanapala.domain.AnomalyDetector
import com.phapalesai.dhanapala.domain.BhaiMessageEngine
import com.phapalesai.dhanapala.domain.BudgetCalculator
import com.phapalesai.dhanapala.domain.MoneyHoroscope
import com.phapalesai.dhanapala.domain.MoneyJokes
import com.phapalesai.dhanapala.domain.MoneySavingTips
import com.phapalesai.dhanapala.domain.StreakCalculator
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
    private val _ghostMemory = MutableStateFlow<GhostMemory?>(null)
    private val _celebration = MutableStateFlow<CelebrationState?>(null)
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

    private data class ExtrasBundle(
        val hasSmsPermission: Boolean,
        val categoryBudgets: List<com.phapalesai.dhanapala.data.local.CategoryBudgetEntity>,
        val allTransactions: List<TransactionEntity>,
        val ghostMemory: GhostMemory?,
        val celebration: CelebrationState?
    )

    // combine() tops out at 5 positional flows, so everything beyond the
    // top-level 5 gets bundled into this one extra flow.
    private val extras = combine(
        _hasSmsPermission,
        categoryBudgetsForActivePeriod,
        transactionRepo.observeAll(),
        _ghostMemory,
        _celebration
    ) { hasSmsPermission, categoryBudgets, allTransactions, ghostMemory, celebration ->
        ExtrasBundle(hasSmsPermission, categoryBudgets, allTransactions, ghostMemory, celebration)
    }

    init {
        viewModelScope.launch {
            val lastMonthToday = LocalDate.now().minusMonths(1)
            val range = DateUtils.dayRangeMillis(lastMonthToday)
            val debitTxs = transactionRepo.getBetweenOnce(range.first, range.last)
                .filter { it.type == TransactionType.DEBIT }
            val total = debitTxs.sumOf { it.amount }
            if (total > 0) {
                val topCategory = debitTxs.groupBy { it.category }
                    .maxByOrNull { (_, txs) -> txs.sumOf { it.amount } }?.key
                _ghostMemory.value = GhostMemory(total, topCategory, lastMonthToday)
            }
        }
        viewModelScope.launch {
            val active = budgetRepo.getActiveOnce(nowMillis)
            if (active != null) return@launch
            val ended = budgetRepo.getMostRecentlyEndedOnce(nowMillis) ?: return@launch
            val settings = budgetRepo.observeSettings().first()
            if (settings.lastCelebratedBudgetId == ended.id) return@launch
            val transactions = transactionRepo.getBetweenOnce(ended.startDateMillis, ended.endDateMillis)
            val spent = transactions.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
            _celebration.value = CelebrationState(
                budgetId = ended.id,
                wasUnderBudget = spent <= ended.amount,
                amountOverOrUnder = kotlin.math.abs(ended.amount - spent)
            )
        }
    }

    fun dismissCelebration() {
        val current = _celebration.value ?: return
        _celebration.value = null
        viewModelScope.launch {
            val settings = budgetRepo.observeSettings().first()
            budgetRepo.updateSettings(settings.copy(lastCelebratedBudgetId = current.budgetId))
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        periodTransactions,
        activeBudget,
        budgetRepo.observeSettings(),
        scanState,
        extras
    ) { transactions, budgetEntity, settings, (isScanning, lastScanResult), extrasBundle ->
        val hasSmsPermission = extrasBundle.hasSmsPermission
        val categoryBudgets = extrasBundle.categoryBudgets
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
            moneyHoroscope = MoneyHoroscope.today(settings.roastLanguageEnum),
            welcomeMessage = WelcomeMessages.random(settings.roastLanguageEnum, Random(welcomeSeed)),
            periodStart = periodStart,
            periodEnd = periodEnd,
            categoryBudgets = categoryBudgetProgress,
            badges = StreakCalculator.earnedBadges(extrasBundle.allTransactions),
            anomalies = AnomalyDetector.detect(transactions, extrasBundle.allTransactions),
            ghostMemory = extrasBundle.ghostMemory,
            celebration = extrasBundle.celebration,
            impulsesAvoided = settings.impulsesAvoided
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
