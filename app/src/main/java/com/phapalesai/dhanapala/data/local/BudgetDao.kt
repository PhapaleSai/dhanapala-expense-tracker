package com.phapalesai.dhanapala.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month")
    fun observeForMonth(month: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE month = :month")
    suspend fun getForMonth(month: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)

    @Query("UPDATE budgets SET notified80 = :value WHERE month = :month")
    suspend fun setNotified80(month: String, value: Boolean)

    @Query("UPDATE budgets SET notifiedExceeded = :value WHERE month = :month")
    suspend fun setNotifiedExceeded(month: String, value: Boolean)
}
