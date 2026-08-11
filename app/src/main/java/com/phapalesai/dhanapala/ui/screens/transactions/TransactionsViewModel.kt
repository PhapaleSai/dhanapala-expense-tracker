package com.phapalesai.dhanapala.ui.screens.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID

data class TransactionsFilter(
    val type: TransactionType? = null,
    val category: String? = null,
    val query: String = "",
    val month: YearMonth? = null,
    val tag: String? = null
)

private fun TransactionEntity.tagList(): List<String> =
    tags?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as DhanapalaApplication).transactionRepository

    private val _filter = MutableStateFlow(TransactionsFilter())
    val filter: StateFlow<TransactionsFilter> = _filter

    val transactions: StateFlow<List<TransactionEntity>> = combine(repo.observeAll(), _filter) { all, filter ->
        all.filter { tx ->
            (filter.type == null || tx.type == filter.type) &&
                (filter.category == null || tx.category == filter.category) &&
                (filter.month == null || monthOf(tx.dateMillis) == filter.month) &&
                (filter.tag == null || tx.tagList().contains(filter.tag)) &&
                (filter.query.isBlank() || matchesQuery(tx, filter.query))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableMonths: StateFlow<List<YearMonth>> = repo.observeAll()
        .map { list -> list.map { monthOf(it.dateMillis) }.distinct().sortedDescending() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableTags: StateFlow<List<String>> = repo.observeAll()
        .map { list -> list.flatMap { it.tagList() }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun monthOf(dateMillis: Long): YearMonth =
        YearMonth.from(Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()))

    private fun matchesQuery(tx: TransactionEntity, query: String): Boolean {
        val q = query.trim()
        return tx.description?.contains(q, ignoreCase = true) == true ||
            tx.sender?.contains(q, ignoreCase = true) == true ||
            tx.merchant?.contains(q, ignoreCase = true) == true ||
            tx.category.contains(q, ignoreCase = true)
    }

    fun setTypeFilter(type: TransactionType?) {
        _filter.value = _filter.value.copy(type = type)
    }

    fun setCategoryFilter(category: String?) {
        _filter.value = _filter.value.copy(category = category)
    }

    fun setMonthFilter(month: YearMonth?) {
        _filter.value = _filter.value.copy(month = month)
    }

    fun setTagFilter(tag: String?) {
        _filter.value = _filter.value.copy(tag = tag)
    }

    fun setQuery(query: String) {
        _filter.value = _filter.value.copy(query = query)
    }

    fun updateCategory(id: Long, category: String) {
        viewModelScope.launch { repo.updateCategory(id, category) }
    }

    fun updateTags(id: Long, tags: String) {
        viewModelScope.launch { repo.updateTags(id, tags.ifBlank { null }) }
    }

    fun delete(transaction: TransactionEntity) {
        viewModelScope.launch { repo.delete(transaction) }
    }

    fun addManual(
        amount: Double,
        type: TransactionType,
        category: String,
        description: String,
        dateMillis: Long,
        tags: String = "",
        receiptPhotoPath: String? = null
    ) {
        viewModelScope.launch {
            repo.addManual(
                TransactionEntity(
                    amount = amount,
                    type = type,
                    dateMillis = dateMillis,
                    sender = null,
                    merchant = null,
                    description = description.ifBlank { null },
                    sourceSmsId = null,
                    dedupeHash = "manual-${UUID.randomUUID()}",
                    category = category,
                    isManual = true,
                    createdAt = System.currentTimeMillis(),
                    tags = tags.ifBlank { null },
                    receiptPhotoPath = receiptPhotoPath
                )
            )
        }
    }
}
