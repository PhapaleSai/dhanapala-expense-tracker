package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType

data class Anomaly(val transaction: TransactionEntity, val categoryAverage: Double, val multiplier: Double)

/**
 * Flags a transaction as unusual purely by comparing it to your own history for that
 * category — no ML, no external data. Needs a few prior same-category transactions
 * first, so a brand-new category's first purchase is never flagged.
 */
object AnomalyDetector {
    private const val MULTIPLIER_THRESHOLD = 3.0
    private const val MIN_HISTORY = 3

    fun detect(recentTransactions: List<TransactionEntity>, allTransactions: List<TransactionEntity>): List<Anomaly> {
        val debitsByCategory = allTransactions
            .filter { it.type == TransactionType.DEBIT }
            .groupBy { it.category }

        return recentTransactions
            .filter { it.type == TransactionType.DEBIT }
            .mapNotNull { tx ->
                val categoryHistory = debitsByCategory[tx.category].orEmpty().filter { it.id != tx.id }
                if (categoryHistory.size < MIN_HISTORY) return@mapNotNull null
                val average = categoryHistory.sumOf { it.amount } / categoryHistory.size
                if (average <= 0) return@mapNotNull null
                val multiplier = tx.amount / average
                if (multiplier >= MULTIPLIER_THRESHOLD) Anomaly(tx, average, multiplier) else null
            }
            .sortedByDescending { it.multiplier }
    }
}
