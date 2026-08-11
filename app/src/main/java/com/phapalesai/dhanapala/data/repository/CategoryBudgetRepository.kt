package com.phapalesai.dhanapala.data.repository

import com.phapalesai.dhanapala.data.local.CategoryBudgetDao
import com.phapalesai.dhanapala.data.local.CategoryBudgetEntity
import kotlinx.coroutines.flow.Flow

class CategoryBudgetRepository(private val dao: CategoryBudgetDao) {

    fun observeForBudget(budgetId: Long): Flow<List<CategoryBudgetEntity>> = dao.observeForBudget(budgetId)

    suspend fun getForBudget(budgetId: Long): List<CategoryBudgetEntity> = dao.getForBudget(budgetId)

    /** Creates or replaces a category's limit for a budget period (resets notifiedExceeded). */
    suspend fun setLimit(budgetId: Long, category: String, amount: Double, existingId: Long? = null): Long =
        dao.upsert(CategoryBudgetEntity(id = existingId ?: 0, budgetId = budgetId, category = category, amount = amount))

    suspend fun markNotifiedExceeded(id: Long) = dao.setNotifiedExceeded(id, true)

    suspend fun delete(id: Long) = dao.delete(id)
}
