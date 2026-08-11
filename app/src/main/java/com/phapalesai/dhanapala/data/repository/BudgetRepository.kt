package com.phapalesai.dhanapala.data.repository

import com.phapalesai.dhanapala.data.local.AppSettingsDao
import com.phapalesai.dhanapala.data.local.AppSettingsEntity
import com.phapalesai.dhanapala.data.local.BudgetDao
import com.phapalesai.dhanapala.data.local.BudgetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepository(
    private val budgetDao: BudgetDao,
    private val settingsDao: AppSettingsDao
) {
    fun observeActive(nowMillis: Long): Flow<BudgetEntity?> = budgetDao.observeActive(nowMillis)

    suspend fun getActiveOnce(nowMillis: Long): BudgetEntity? = budgetDao.getActiveOnce(nowMillis)

    /** Creates a new budget period, or replaces the currently active one's amount (same id, dates, resets notified flags). */
    suspend fun setBudget(startDateMillis: Long, endDateMillis: Long, amount: Double, existingId: Long? = null): Long =
        budgetDao.upsert(
            BudgetEntity(
                id = existingId ?: 0,
                startDateMillis = startDateMillis,
                endDateMillis = endDateMillis,
                amount = amount
            )
        )

    suspend fun markNotified80(id: Long) = budgetDao.setNotified80(id, true)

    suspend fun markNotifiedExceeded(id: Long) = budgetDao.setNotifiedExceeded(id, true)

    fun observeSettings(): Flow<AppSettingsEntity> = settingsDao.observe().map { it ?: AppSettingsEntity() }

    suspend fun updateSettings(settings: AppSettingsEntity) = settingsDao.upsert(settings)

    suspend fun getAllBudgetsOnce(): List<BudgetEntity> = budgetDao.getAll()

    /** Wipes and re-inserts every budget period, preserving ids so category_budgets.budgetId stays valid. */
    suspend fun replaceAllBudgets(budgets: List<BudgetEntity>) {
        budgetDao.deleteAll()
        budgets.forEach { budgetDao.upsert(it) }
    }
}
