package com.phapalesai.dhanapala.notification

import android.content.Context
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.data.repository.BudgetRepository
import com.phapalesai.dhanapala.data.repository.CategoryBudgetRepository
import com.phapalesai.dhanapala.data.repository.ScanResult
import com.phapalesai.dhanapala.data.repository.TransactionRepository
import com.phapalesai.dhanapala.domain.BhaiMessageEngine
import com.phapalesai.dhanapala.domain.BudgetCalculator
import com.phapalesai.dhanapala.domain.BudgetNotificationDecider
import com.phapalesai.dhanapala.domain.BudgetNotifyTier
import com.phapalesai.dhanapala.domain.roastLanguageEnum
import com.phapalesai.dhanapala.domain.roastLevelEnum
import com.phapalesai.dhanapala.widget.WidgetUpdater
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

/**
 * Turns a scan's newly-inserted transactions into notifications. Shared by
 * HomeViewModel (foreground rescans) and SmsReceiver (live SMS arrivals) so
 * both paths notify identically — every debit and every credit, not just
 * ones crossing the large-expense threshold.
 */
class TransactionAlertService(
    private val context: Context,
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
    private val categoryBudgetRepo: CategoryBudgetRepository,
    private val notifier: DhanapalaNotifier
) {
    private val zone = ZoneId.systemDefault()

    suspend fun notifyForScan(result: ScanResult) {
        if (result.insertedTransactions.isNotEmpty()) {
            WidgetUpdater.refresh(context)
        }

        val settings = budgetRepo.observeSettings().first()
        if (!settings.notificationsEnabled) return

        for (tx in result.insertedTransactions) {
            when {
                tx.type == TransactionType.CREDIT && BhaiMessageEngine.isLikelySalary(tx.amount) ->
                    notifier.notifySalaryCredit(tx.amount, BhaiMessageEngine.salaryMessage(settings.roastLanguageEnum))
                tx.type == TransactionType.CREDIT ->
                    notifier.notifyMoneyReceived(tx.amount, BhaiMessageEngine.moneyReceivedMessage(settings.roastLanguageEnum))
                tx.type == TransactionType.DEBIT ->
                    notifier.notifyExpense(
                        tx.amount,
                        BhaiMessageEngine.spendingReaction(
                            tx.amount,
                            settings.largeExpenseThreshold,
                            settings.roastLanguageEnum,
                            settings.roastLevelEnum
                        )
                    )
            }
        }

        val nowMillis = System.currentTimeMillis()
        val budgetEntity = budgetRepo.getActiveOnce(nowMillis) ?: return
        if (budgetEntity.amount <= 0) return
        val periodTransactions = transactionRepo.getBetweenOnce(budgetEntity.startDateMillis, budgetEntity.endDateMillis)
        val periodEnd = Instant.ofEpochMilli(budgetEntity.endDateMillis).atZone(zone).toLocalDate()
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

        checkCategoryBudgets(budgetEntity.id, periodTransactions)
    }

    private suspend fun checkCategoryBudgets(budgetId: Long, periodTransactions: List<com.phapalesai.dhanapala.data.local.TransactionEntity>) {
        val categoryBudgets = categoryBudgetRepo.getForBudget(budgetId)
        for (cb in categoryBudgets) {
            if (cb.notifiedExceeded || cb.amount <= 0) continue
            val spent = periodTransactions
                .filter { it.type == TransactionType.DEBIT && it.category == cb.category }
                .sumOf { it.amount }
            if (spent >= cb.amount) {
                notifier.notifyCategoryBudgetExceeded(cb.category, spent, cb.amount)
                categoryBudgetRepo.markNotifiedExceeded(cb.id)
            }
        }
    }
}
