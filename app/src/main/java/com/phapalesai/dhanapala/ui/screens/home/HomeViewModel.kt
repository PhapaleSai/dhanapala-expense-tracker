package com.phapalesai.dhanapala.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.BudgetEntity
import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.data.parser.CategoryGuesser
import com.phapalesai.dhanapala.data.repository.ScanResult
import com.phapalesai.dhanapala.domain.BhaiMessageEngine
import com.phapalesai.dhanapala.domain.BudgetCalculator
import com.phapalesai.dhanapala.domain.BudgetNotificationDecider
import com.phapalesai.dhanapala.domain.BudgetNotifyTier
import com.phapalesai.dhanapala.domain.MoneySavingTips
import com.phapalesai.dhanapala.domain.roastLanguageEnum
import com.phapalesai.dhanapala.domain.roastLevelEnum
import com.phapalesai.dhanapala.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val transactionRepo = app.transactionRepository
    private val budgetRepo = app.budgetRepository
    private val smsReader = app.smsReader
    private val notifier = app.notifier

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

    val uiState: StateFlow<HomeUiState> = combine(
        periodTransactions,
        activeBudget,
        budgetRepo.observeSettings(),
        scanState,
        _hasSmsPermission
    ) { transactions, budgetEntity, settings, (isScanning, lastScanResult), hasSmsPermission ->
        val periodEnd = budgetEntity?.let {
            java.time.Instant.ofEpochMilli(it.endDateMillis).atZone(zone).toLocalDate()
        }
        val summary = if (periodEnd != null) {
            BudgetCalculator.calculate(budgetEntity.amount, transactions, periodEnd = periodEnd)
        } else {
            BudgetCalculator.calculate(0.0, transactions)
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
            moneyTip = moneyTipFor(transactions)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    private fun moneyTipFor(transactions: List<TransactionEntity>): String {
        val isFoodDeliveryContext = transactions.take(5).any { tx ->
            tx.type == TransactionType.DEBIT &&
                (tx.description?.let { CategoryGuesser.isFoodDelivery(it) } == true)
        }
        val today = LocalDate.now().toEpochDay()
        val seed = today + if (isFoodDeliveryContext) 1_000_000 else 0
        return MoneySavingTips.random(isFoodDeliveryContext, Random(seed))
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
            maybeNotify(result)
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
        }
    }

    /** Creates a brand new budget for an explicit custom date range. */
    fun setCustomBudget(startDate: LocalDate, endDate: LocalDate, amount: Double) {
        viewModelScope.launch {
            val start = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
            budgetRepo.setBudget(start, end, amount)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            val current = budgetRepo.observeSettings().first()
            budgetRepo.updateSettings(current.copy(userName = name.trim()))
        }
    }

    private suspend fun maybeNotify(result: ScanResult) {
        val settings = budgetRepo.observeSettings().first()
        if (!settings.notificationsEnabled) return

        for (tx in result.insertedTransactions) {
            when {
                tx.type == TransactionType.DEBIT && tx.amount >= settings.largeExpenseThreshold ->
                    notifier.notifyLargeExpense(
                        tx.amount,
                        BhaiMessageEngine.spendingReaction(
                            tx.amount,
                            settings.largeExpenseThreshold,
                            settings.roastLanguageEnum,
                            settings.roastLevelEnum
                        )
                    )
                tx.type == TransactionType.CREDIT && BhaiMessageEngine.isLikelySalary(tx.amount) ->
                    notifier.notifySalaryCredit(tx.amount, BhaiMessageEngine.salaryMessage(settings.roastLanguageEnum))
                tx.type == TransactionType.CREDIT && tx.amount >= 100 ->
                    notifier.notifyMoneyReceived(tx.amount, BhaiMessageEngine.moneyReceivedMessage(settings.roastLanguageEnum))
            }
        }

        val budgetEntity: BudgetEntity = budgetRepo.getActiveOnce(nowMillis) ?: return
        if (budgetEntity.amount <= 0) return
        val periodTransactions = transactionRepo.getBetweenOnce(budgetEntity.startDateMillis, budgetEntity.endDateMillis)
        val periodEnd = java.time.Instant.ofEpochMilli(budgetEntity.endDateMillis).atZone(zone).toLocalDate()
        val summary = BudgetCalculator.calculate(budgetEntity.amount, periodTransactions, periodEnd = periodEnd)
        val tier = BudgetNotificationDecider.decide(
            summary.percentUsed,
            budgetEntity.notified80,
            budgetEntity.notifiedExceeded
        )
        when (tier) {
            BudgetNotifyTier.EXCEEDED -> {
                notifier.notifyBudgetExceeded()
                budgetRepo.markNotifiedExceeded(budgetEntity.id)
            }
            BudgetNotifyTier.EIGHTY_PERCENT -> {
                notifier.notifyBudgetThreshold(summary.percentUsed.roundToInt(), summary.remaining)
                budgetRepo.markNotified80(budgetEntity.id)
            }
            BudgetNotifyTier.NONE -> Unit
        }
    }
}
