package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class RecurringItem(
    val category: String,
    val amount: Double,
    val occurrences: Int,
    val lastDate: LocalDate,
    val nextExpectedDate: LocalDate,
    val isMonthly: Boolean
)

/**
 * Flags (category, exact amount) pairs from real SMS-derived debits that
 * repeat at a roughly-monthly or roughly-weekly cadence -- subscriptions and
 * recurring bills tend to charge the same amount each cycle, so an exact
 * match is deliberately used rather than a fuzzy one.
 */
object RecurringDetector {
    private val zone = ZoneId.systemDefault()

    fun detect(transactions: List<TransactionEntity>, minOccurrences: Int = 2): List<RecurringItem> {
        return transactions
            .filter { it.type == TransactionType.DEBIT && !it.isManual }
            .groupBy { it.category to it.amount }
            .mapNotNull { (key, txs) ->
                if (txs.size < minOccurrences) return@mapNotNull null
                val dates = txs.map { Instant.ofEpochMilli(it.dateMillis).atZone(zone).toLocalDate() }
                    .distinct()
                    .sorted()
                if (dates.size < minOccurrences) return@mapNotNull null
                val gaps = dates.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b) }
                val avgGap = gaps.average()
                val isMonthly = avgGap in 25.0..35.0
                val isWeekly = avgGap in 6.0..8.0
                if (!isMonthly && !isWeekly) return@mapNotNull null
                val cadence = if (isMonthly) 30L else 7L
                RecurringItem(
                    category = key.first,
                    amount = key.second,
                    occurrences = dates.size,
                    lastDate = dates.last(),
                    nextExpectedDate = dates.last().plusDays(cadence),
                    isMonthly = isMonthly
                )
            }
            .sortedByDescending { it.amount }
    }

    /** Sum of amounts for items on a roughly-monthly cadence, weekly ones converted to a monthly-equivalent (x4.33). */
    fun estimatedMonthlyTotal(items: List<RecurringItem>): Double =
        items.sumOf { if (it.isMonthly) it.amount else it.amount * 4.33 }
}
