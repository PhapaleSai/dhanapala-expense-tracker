package com.phapalesai.dhanapala.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType { DEBIT, CREDIT }

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["dedupeHash"], unique = true)]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val dateMillis: Long,
    val sender: String?,
    val merchant: String?,
    val description: String?,
    val sourceSmsId: String?,
    val dedupeHash: String,
    val category: String,
    val isManual: Boolean,
    val createdAt: Long
)
