package com.phapalesai.dhanapala.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.repository.ScanResult
import com.phapalesai.dhanapala.domain.BhaiMessageEngine
import com.phapalesai.dhanapala.domain.BudgetCalculator
import com.phapalesai.dhanapala.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val transactionRepo = app.transactionRepository
    private val budgetRepo = app.budgetRepository
    private val smsReader = app.smsReader

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
            bhaiMessage = if (settings.bhaiModeEnabled) BhaiMessageEngine.budgetReaction(summary) else null,
            hasBudgetSet = budgetEntity != null,
            settings = settings,
            isScanning = isScanning,
            lastScanResult = lastScanResult,
            hasSmsPermission = hasSmsPermission
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun onSmsPermissionResult(granted: Boolean) {
        _hasSmsPermission.value = granted
        if (granted) scanSms()
    }

    fun scanSms() {
        viewModelScope.launch {
            _isScanning.value = true
            val messages = smsReader.readInbox(limit = 1000)
            _lastScanResult.value = transactionRepo.scanMessages(messages)
            _isScanning.value = false
        }
    }

    fun setBudget(amount: Double) {
        viewModelScope.launch { budgetRepo.setBudget(monthKey, amount) }
    }
}
