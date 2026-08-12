package com.phapalesai.dhanapala.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "split_expenses")
data class SplitExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val description: String,
    val amount: Double,
    val paidBy: String,
    /** Comma-separated names sharing this expense — usually the whole group, but not always. */
    val splitAmong: String,
    val createdAt: Long
)
