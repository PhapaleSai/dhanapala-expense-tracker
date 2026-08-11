package com.phapalesai.dhanapala.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 0,
    val largeExpenseThreshold: Double = 1000.0,
    val bhaiModeEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = false,
    val roastLevel: String = "MEDIUM",
    val roastLanguage: String = "HI",
    val userName: String = "",
    val biometricLockEnabled: Boolean = false
)
