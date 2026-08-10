package com.phapalesai.dhanapala.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE :nowMillis BETWEEN startDateMillis AND endDateMillis ORDER BY startDateMillis DESC LIMIT 1")
    fun observeActive(nowMillis: Long): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE :nowMillis BETWEEN startDateMillis AND endDateMillis ORDER BY startDateMillis DESC LIMIT 1")
    suspend fun getActiveOnce(nowMillis: Long): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity): Long

    @Query("UPDATE budgets SET notified80 = :value WHERE id = :id")
    suspend fun setNotified80(id: Long, value: Boolean)

    @Query("UPDATE budgets SET notifiedExceeded = :value WHERE id = :id")
    suspend fun setNotifiedExceeded(id: Long, value: Boolean)
}
