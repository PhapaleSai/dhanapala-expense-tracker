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
    val biometricLockEnabled: Boolean = false,
    /** How many times the user backed out of the Panic Button cooldown instead of proceeding. */
    val impulsesAvoided: Int = 0,
    /** Id of the last past budget period the month-end celebration overlay was already shown for. */
    val lastCelebratedBudgetId: Long = 0,
    /** Reads the Bhai Meter message aloud (on-device TTS) when it changes. Off by default — audio should be opt-in. */
    val voiceRoastsEnabled: Boolean = false,
    /** Full-screen incoming-call-style alert when the budget is exceeded. Off by default — quite intrusive. */
    val bhaiCallEnabled: Boolean = false
)
