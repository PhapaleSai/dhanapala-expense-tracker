package com.phapalesai.dhanapala.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A shared-expense group (trip, event) — a running Splitwise-style tab among named participants. */
@Entity(tableName = "split_groups")
data class SplitGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Comma-separated participant names. */
    val participants: String,
    val createdAt: Long
)
