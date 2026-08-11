package com.phapalesai.dhanapala.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A per-category spending limit tied to a specific budget period (BudgetEntity.id). */
@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val budgetId: Long,
    val category: String,
    val amount: Double,
    val notifiedExceeded: Boolean = false
)
