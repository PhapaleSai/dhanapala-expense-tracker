package com.phapalesai.dhanapala.data.local

import androidx.room.Entity

@Entity(tableName = "budgets", primaryKeys = ["month"])
data class BudgetEntity(
    val month: String, // "yyyy-MM"
    val amount: Double
)
