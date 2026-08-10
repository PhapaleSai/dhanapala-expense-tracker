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
    fun observeBudget(month: String): Flow<BudgetEntity?> = budgetDao.observeForMonth(month)

    suspend fun getBudgetOnce(month: String): BudgetEntity? = budgetDao.getForMonth(month)

    suspend fun setBudget(month: String, amount: Double) = budgetDao.upsert(BudgetEntity(month, amount))

    suspend fun markNotified80(month: String) = budgetDao.setNotified80(month, true)

    suspend fun markNotifiedExceeded(month: String) = budgetDao.setNotifiedExceeded(month, true)

    fun observeSettings(): Flow<AppSettingsEntity> = settingsDao.observe().map { it ?: AppSettingsEntity() }

    suspend fun updateSettings(settings: AppSettingsEntity) = settingsDao.upsert(settings)
}
