package com.phapalesai.dhanapala.data.repository

import com.phapalesai.dhanapala.data.local.SplitDao
import com.phapalesai.dhanapala.data.local.SplitExpenseEntity
import com.phapalesai.dhanapala.data.local.SplitGroupEntity
import kotlinx.coroutines.flow.Flow

class SplitRepository(private val dao: SplitDao) {

    fun observeGroups(): Flow<List<SplitGroupEntity>> = dao.observeGroups()

    fun observeExpenses(groupId: Long): Flow<List<SplitExpenseEntity>> = dao.observeExpenses(groupId)

    suspend fun createGroup(name: String, participants: List<String>): Long =
        dao.insertGroup(
            SplitGroupEntity(
                name = name,
                participants = participants.joinToString(","),
                createdAt = System.currentTimeMillis()
            )
        )

    suspend fun addExpense(groupId: Long, description: String, amount: Double, paidBy: String, splitAmong: List<String>): Long =
        dao.insertExpense(
            SplitExpenseEntity(
                groupId = groupId,
                description = description,
                amount = amount,
                paidBy = paidBy,
                splitAmong = splitAmong.joinToString(","),
                createdAt = System.currentTimeMillis()
            )
        )
}
