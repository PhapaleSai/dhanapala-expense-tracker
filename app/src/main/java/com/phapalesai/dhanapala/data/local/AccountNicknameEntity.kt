package com.phapalesai.dhanapala.data.local

import androidx.room.Entity

/** Maps a raw SMS sender id (e.g. "JD-JSBLPN-S") to a friendly name (e.g. "Jana Bank"). */
@Entity(tableName = "account_nicknames", primaryKeys = ["senderPattern"])
data class AccountNicknameEntity(
    val senderPattern: String,
    val displayName: String
)
