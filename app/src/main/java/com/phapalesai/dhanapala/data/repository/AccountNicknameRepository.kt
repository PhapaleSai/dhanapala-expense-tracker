package com.phapalesai.dhanapala.data.repository

import com.phapalesai.dhanapala.data.local.AccountNicknameDao
import com.phapalesai.dhanapala.data.local.AccountNicknameEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AccountNicknameRepository(private val dao: AccountNicknameDao) {
    fun observeAll(): Flow<List<AccountNicknameEntity>> = dao.observeAll()
    suspend fun setNickname(senderPattern: String, displayName: String) =
        dao.upsert(AccountNicknameEntity(senderPattern, displayName))
    suspend fun delete(senderPattern: String) = dao.delete(senderPattern)

    suspend fun getAllOnce(): List<AccountNicknameEntity> = dao.observeAll().first()

    suspend fun replaceAll(items: List<AccountNicknameEntity>) {
        dao.deleteAll()
        items.forEach { dao.upsert(it) }
    }
}
