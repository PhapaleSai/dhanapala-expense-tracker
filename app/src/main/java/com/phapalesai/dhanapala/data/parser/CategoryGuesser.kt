package com.phapalesai.dhanapala.data.parser

import com.phapalesai.dhanapala.data.local.Category
import com.phapalesai.dhanapala.data.local.TransactionType

/**
 * Deliberately simple heuristic category guess from SMS text. The user can
 * always override the category by hand, so this only needs to be a
 * reasonable starting point, not perfect classification.
 */
object CategoryGuesser {
    fun guess(body: String, type: TransactionType): String {
        val lower = body.lowercase()
        return when {
            lower.contains("salary") -> Category.SALARY
            lower.contains("refund") || lower.contains("cashback") -> Category.REFUND
            lower.contains("atm") || lower.contains("withdraw") -> Category.CASH_WITHDRAWAL
            lower.contains("upi") -> Category.UPI
            type == TransactionType.CREDIT -> Category.OTHER
            else -> Category.UNCATEGORIZED
        }
    }
}
