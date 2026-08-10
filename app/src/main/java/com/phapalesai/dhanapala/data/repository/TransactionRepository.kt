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
    val notTransactions: Int,
    val insertedTransactions: List<TransactionEntity> = emptyList()
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
        var duplicates = 0
        var notTransactions = 0
        val insertedTransactions = mutableListOf<TransactionEntity>()

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
            if (rowId != -1L) insertedTransactions.add(entity.copy(id = rowId)) else duplicates++
        }

        recategorizeAll()

        return ScanResult(
            scanned = messages.size,
            inserted = insertedTransactions.size,
            duplicates = duplicates,
            notTransactions = notTransactions,
            insertedTransactions = insertedTransactions
        )
    }

    suspend fun getBetweenOnce(startMillis: Long, endMillis: Long): List<TransactionEntity> =
        dao.getBetween(startMillis, endMillis)

    suspend fun addManual(entity: TransactionEntity): Long = dao.insert(entity)

    suspend fun updateCategory(id: Long, category: String) = dao.updateCategory(id, category)

    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)

    suspend fun deleteAll() = dao.deleteAll()

    /**
     * Re-runs category detection against every stored SMS-derived transaction.
     * Needed because CategoryGuesser's keyword dictionary keeps growing —
     * transactions saved before a dictionary update would otherwise be stuck
     * with a stale category forever. Manual entries are left alone since the
     * user chose that category on purpose.
     */
    suspend fun recategorizeAll() {
        for (tx in dao.getAll()) {
            if (tx.isManual) continue
            val body = tx.description ?: continue
            val freshCategory = CategoryGuesser.guess(body, tx.type)
            if (freshCategory != tx.category) {
                dao.updateCategory(tx.id, freshCategory)
            }
        }
    }
}
