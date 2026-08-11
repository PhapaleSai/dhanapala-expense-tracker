package com.phapalesai.dhanapala.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountNicknameDao {
    @Query("SELECT * FROM account_nicknames")
    fun observeAll(): Flow<List<AccountNicknameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AccountNicknameEntity)

    @Query("DELETE FROM account_nicknames WHERE senderPattern = :senderPattern")
    suspend fun delete(senderPattern: String)

    @Query("DELETE FROM account_nicknames")
    suspend fun deleteAll()
}
