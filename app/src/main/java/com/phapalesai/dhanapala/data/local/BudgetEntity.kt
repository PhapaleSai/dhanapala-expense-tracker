package com.phapalesai.dhanapala.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A budget applies to an explicit date range — usually a calendar month, but the user can pick any start/end date. */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val amount: Double,
    val notified80: Boolean = false,
    val notifiedExceeded: Boolean = false
)
