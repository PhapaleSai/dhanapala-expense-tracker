package com.phapalesai.dhanapala.data.repository

import com.phapalesai.dhanapala.data.local.TransactionDao
import com.phapalesai.dhanapala.data.local.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory stand-in for Room, mirroring the unique dedupeHash constraint for JVM tests. */
class FakeTransactionDao : TransactionDao {

    private val store = LinkedHashMap<Long, TransactionEntity>()
    private var nextId = 1L
    private val state = MutableStateFlow<List<TransactionEntity>>(emptyList())

    private fun publish() {
        state.value = store.values.sortedByDescending { it.dateMillis }
    }

    override suspend fun insert(transaction: TransactionEntity): Long {
        val duplicate = store.values.any { it.dedupeHash == transaction.dedupeHash }
        if (duplicate) return -1L
        val id = nextId++
        store[id] = transaction.copy(id = id)
        publish()
        return id
    }

    override fun observeAll(): StateFlow<List<TransactionEntity>> = state

    override suspend fun getAll(): List<TransactionEntity> = store.values.sortedByDescending { it.dateMillis }

    override fun observeBetween(startMillis: Long, endMillis: Long) = state

    override suspend fun getBetween(startMillis: Long, endMillis: Long): List<TransactionEntity> =
        store.values.filter { it.dateMillis in startMillis..endMillis }.sortedByDescending { it.dateMillis }

    override suspend fun updateCategory(id: Long, category: String) {
        store[id]?.let { store[id] = it.copy(category = category) }
        publish()
    }

    override suspend fun delete(transaction: TransactionEntity) {
        store.remove(transaction.id)
        publish()
    }

    override suspend fun deleteAll() {
        store.clear()
        publish()
    }
}
