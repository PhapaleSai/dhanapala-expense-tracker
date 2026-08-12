package com.phapalesai.dhanapala.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitDao {
    @Insert
    suspend fun insertGroup(group: SplitGroupEntity): Long

    @Query("SELECT * FROM split_groups ORDER BY createdAt DESC")
    fun observeGroups(): Flow<List<SplitGroupEntity>>

    @Insert
    suspend fun insertExpense(expense: SplitExpenseEntity): Long

    @Query("SELECT * FROM split_expenses WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun observeExpenses(groupId: Long): Flow<List<SplitExpenseEntity>>

    @Query("DELETE FROM split_groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: Long)

    @Query("DELETE FROM split_expenses WHERE groupId = :groupId")
    suspend fun deleteExpensesForGroup(groupId: Long)

    @Query("DELETE FROM split_groups")
    suspend fun deleteAllGroups()

    @Query("DELETE FROM split_expenses")
    suspend fun deleteAllExpenses()
}
