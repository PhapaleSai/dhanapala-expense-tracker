package com.phapalesai.dhanapala.ui.screens.home

import com.phapalesai.dhanapala.data.local.AppSettingsEntity
import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.repository.ScanResult
import com.phapalesai.dhanapala.domain.Badge
import com.phapalesai.dhanapala.domain.BudgetCalculator
import com.phapalesai.dhanapala.domain.BudgetSummary
import java.time.LocalDate

data class CategoryBudgetProgress(
    val id: Long,
    val category: String,
    val budget: Double,
    val spent: Double
)

/** "On this day last month you spent ₹X" nostalgia card. */
data class GhostMemory(val amount: Double, val category: String?, val date: LocalDate)

/** Shown once, right after a budget period ends, before the next one is set up. */
data class CelebrationState(val budgetId: Long, val wasUnderBudget: Boolean, val amountOverOrUnder: Double)

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
    val moneyHoroscope: String? = null,
    val welcomeMessage: String? = null,
    val periodStart: LocalDate? = null,
    val periodEnd: LocalDate? = null,
    val categoryBudgets: List<CategoryBudgetProgress> = emptyList(),
    val badges: List<Badge> = emptyList(),
    val ghostMemory: GhostMemory? = null,
    val celebration: CelebrationState? = null,
    val impulsesAvoided: Int = 0
)
