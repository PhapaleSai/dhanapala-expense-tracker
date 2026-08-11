package com.phapalesai.dhanapala.ui.screens.home

import com.phapalesai.dhanapala.data.local.AppSettingsEntity
import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.repository.ScanResult
import com.phapalesai.dhanapala.domain.BudgetCalculator
import com.phapalesai.dhanapala.domain.BudgetSummary
import java.time.LocalDate

data class HomeUiState(
    val summary: BudgetSummary = BudgetCalculator.calculate(0.0, emptyList()),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val bhaiMessage: String? = null,
    val hasBudgetSet: Boolean = false,
    val settings: AppSettingsEntity = AppSettingsEntity(),
    val isScanning: Boolean = false,
    val lastScanResult: ScanResult? = null,
    val hasSmsPermission: Boolean = false,
    val moneyTip: String? = null,
    val moneyJoke: String? = null,
    val periodStart: LocalDate? = null,
    val periodEnd: LocalDate? = null
)
