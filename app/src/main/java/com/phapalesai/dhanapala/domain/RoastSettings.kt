package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.data.local.AppSettingsEntity

enum class RoastLevel { MILD, MEDIUM, SAVAGE }

enum class RoastLanguage { EN, HI, MR }

enum class RoastCategory {
    SMALL_SPEND, MODERATE_SPEND, LARGE_SPEND,
    UNDER_50, BETWEEN_50_75, BETWEEN_75_90, BETWEEN_90_100,
    OVER_BUDGET, ZERO_REMAINING
}

val AppSettingsEntity.roastLevelEnum: RoastLevel
    get() = runCatching { RoastLevel.valueOf(roastLevel) }.getOrDefault(RoastLevel.MEDIUM)

val AppSettingsEntity.roastLanguageEnum: RoastLanguage
    get() = runCatching { RoastLanguage.valueOf(roastLanguage) }.getOrDefault(RoastLanguage.HI)
