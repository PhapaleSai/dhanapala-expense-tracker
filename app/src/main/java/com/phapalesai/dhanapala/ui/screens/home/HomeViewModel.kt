package com.phapalesai.dhanapala.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.data.repository.ScanResult
import com.phapalesai.dhanapala.domain.BhaiMessageEngine
import com.phapalesai.dhanapala.domain.BudgetCalculator
import com.phapalesai.dhanapala.domain.BudgetNotifyTier
import com.phapalesai.dhanapala.domain.BudgetNotificationDecider
import com.phapalesai.dhanapala.domain.roastLanguageEnum
import com.phapalesai.dhanapala.domain.roastLevelEnum
import com.phapalesai.dhanapala.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import kotlin.math.roundToInt

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val transactionRepo = app.transactionRepository
    private val budgetRepo = app.budgetRepository
    private val smsReader = app.smsReader
    private val notifier = app.notifier

    private val monthKey = DateUtils.monthKey()
    private val monthRange = DateUtils.monthRangeMillis(YearMonth.now())

    private val _isScanning = MutableStateFlow(false)
    private val _lastScanResult = MutableStateFlow<ScanResult?>(null)
    private val _hasSmsPermission = MutableStateFlow(false)
    private val scanState = combine(_isScanning, _lastScanResult) { scanning, result -> scanning to result }

    val uiState: StateFlow<HomeUiState> = combine(
        transactionRepo.observeBetween(monthRange.first, monthRange.last),
        budgetRepo.observeBudget(monthKey),
        budgetRepo.observeSettings(),
        scanState,
        _hasSmsPermission
    ) { transactions, budgetEntity, settings, (isScanning, lastScanResult), hasSmsPermission ->
        val summary = BudgetCalculator.calculate(budgetEntity?.amount ?: 0.0, transactions)
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

    private fun moneyTipFor(transactions: List<com.phapalesai.dhanapala.data.local.TransactionEntity>): String {
        val isFoodDeliveryContext = transactions.take(5).any { tx ->
            tx.type == TransactionType.DEBIT &&
                (tx.description?.let { com.phapalesai.dhanapala.data.parser.CategoryGuesser.isFoodDelivery(it) } == true)
        }
        val today = java.time.LocalDate.now().toEpochDay()
        val seed = today + if (isFoodDeliveryContext) 1_000_000 else 0
        return com.phapalesai.dhanapala.domain.MoneySavingTips.random(isFoodDeliveryContext, kotlin.random.Random(seed))
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

    fun setBudget(amount: Double) {
        viewModelScope.launch { budgetRepo.setBudget(monthKey, amount) }
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

        val budgetEntity = budgetRepo.getBudgetOnce(monthKey) ?: return
        if (budgetEntity.amount <= 0) return
        val monthTransactions = transactionRepo.getBetweenOnce(monthRange.first, monthRange.last)
        val summary = BudgetCalculator.calculate(budgetEntity.amount, monthTransactions)
        val tier = BudgetNotificationDecider.decide(
            summary.percentUsed,
            budgetEntity.notified80,
            budgetEntity.notifiedExceeded
        )
        when (tier) {
            BudgetNotifyTier.EXCEEDED -> {
                notifier.notifyBudgetExceeded()
                budgetRepo.markNotifiedExceeded(monthKey)
            }
            BudgetNotifyTier.EIGHTY_PERCENT -> {
                notifier.notifyBudgetThreshold(summary.percentUsed.roundToInt(), summary.remaining)
                budgetRepo.markNotified80(monthKey)
            }
            BudgetNotifyTier.NONE -> Unit
        }
    }
}
