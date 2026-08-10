package com.phapalesai.dhanapala.domain

enum class BudgetNotifyTier { NONE, EIGHTY_PERCENT, EXCEEDED }

/** Decides whether a budget-threshold notification should fire, given what's already been sent this month. */
object BudgetNotificationDecider {
    fun decide(percentUsed: Double, notified80: Boolean, notifiedExceeded: Boolean): BudgetNotifyTier = when {
        percentUsed >= 100 && !notifiedExceeded -> BudgetNotifyTier.EXCEEDED
        percentUsed >= 80 && !notified80 -> BudgetNotifyTier.EIGHTY_PERCENT
        else -> BudgetNotifyTier.NONE
    }
}
