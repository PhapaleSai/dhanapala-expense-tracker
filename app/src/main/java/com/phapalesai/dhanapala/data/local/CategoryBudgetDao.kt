package com.phapalesai.dhanapala.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryBudgetDao {
    @Query("SELECT * FROM category_budgets WHERE budgetId = :budgetId")
    fun observeForBudget(budgetId: Long): Flow<List<CategoryBudgetEntity>>

    @Query("SELECT * FROM category_budgets WHERE budgetId = :budgetId")
    suspend fun getForBudget(budgetId: Long): List<CategoryBudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CategoryBudgetEntity): Long

    @Query("UPDATE category_budgets SET notifiedExceeded = :value WHERE id = :id")
    suspend fun setNotifiedExceeded(id: Long, value: Boolean)

    @Query("DELETE FROM category_budgets WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM category_budgets")
    suspend fun getAll(): List<CategoryBudgetEntity>

    @Query("DELETE FROM category_budgets")
    suspend fun deleteAll()
}
