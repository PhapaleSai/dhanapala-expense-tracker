package com.phapalesai.dhanapala.data.parser

/**
 * Keyword lists used to classify an SMS as DEBIT or CREDIT.
 * Checked as case-insensitive substrings, so e.g. "debit" also matches "debited".
 * Add bank-specific keywords here as new formats are discovered.
 */
object TransactionKeywords {
    val DEBIT = listOf(
        "debited", "debit", "spent", "paid", "purchase", "withdrawn",
        "deducted", "transaction", "upi", "atm", "pos"
    )

    val CREDIT = listOf(
        "credited", "credit", "received", "deposited", "salary",
        "refund", "cashback"
    )
}
