package com.phapalesai.dhanapala.data.parser

/**
 * Regex patterns for extracting a rupee amount from an SMS body.
 * Add new patterns here for bank formats that don't match the generic one
 * (e.g. amount written before the currency marker).
 */
object AmountPatterns {
    val patterns: List<Regex> = listOf(
        // ₹500, Rs 500, Rs. 500, INR 500, INR 1,250.50, Rs 1,250.00
        Regex("""(?:₹|Rs\.?|INR)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
    )
}
