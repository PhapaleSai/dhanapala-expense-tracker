package com.phapalesai.dhanapala.data.repository

import com.phapalesai.dhanapala.data.local.TransactionDao
import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.parser.CategoryGuesser
import com.phapalesai.dhanapala.data.parser.SmsTransactionParser
import com.phapalesai.dhanapala.data.sms.RawSms
import kotlinx.coroutines.flow.Flow

data class ScanResult(
    val scanned: Int,
    val inserted: Int,
    val duplicates: Int,
    val notTransactions: Int
)

/**
 * Turns raw SMS into stored transactions. Never touches the SMS inbox itself
 * (read-only) and never re-inserts an SMS that's already been saved, thanks
 * to the unique dedupeHash index on the transactions table.
 */
class TransactionRepository(
    private val dao: TransactionDao,
    private val parser: SmsTransactionParser = SmsTransactionParser()
) {

    fun observeAll(): Flow<List<TransactionEntity>> = dao.observeAll()

    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>> =
        dao.observeBetween(startMillis, endMillis)

    suspend fun scanMessages(messages: List<RawSms>): ScanResult {
        var inserted = 0
        var duplicates = 0
        var notTransactions = 0

        for (sms in messages) {
            val parsed = parser.parse(sms)
            if (parsed == null) {
                notTransactions++
                continue
            }
            val entity = TransactionEntity(
                amount = parsed.amount,
                type = parsed.type,
                dateMillis = parsed.dateMillis,
                sender = parsed.sender,
                merchant = null,
                description = parsed.rawBody,
                sourceSmsId = parsed.sourceSmsId,
                dedupeHash = parsed.dedupeHash,
                category = CategoryGuesser.guess(parsed.rawBody, parsed.type),
                isManual = false,
                createdAt = System.currentTimeMillis()
            )
            val rowId = dao.insert(entity)
            if (rowId != -1L) inserted++ else duplicates++
        }

        return ScanResult(
            scanned = messages.size,
            inserted = inserted,
            duplicates = duplicates,
            notTransactions = notTransactions
        )
    }

    suspend fun addManual(entity: TransactionEntity): Long = dao.insert(entity)

    suspend fun updateCategory(id: Long, category: String) = dao.updateCategory(id, category)

    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)

    suspend fun deleteAll() = dao.deleteAll()
}
