package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.data.parser.AmountPatterns

/**
 * Best-effort total amount from OCR'd receipt text. Receipts vary too much
 * to parse reliably, so this always feeds a prefill the user can edit
 * before saving, never a value trusted as-is.
 */
object ReceiptTextParser {
    private val numberRegex = Regex("""[0-9][0-9,]*(?:\.[0-9]{1,2})?""")

    fun extractTotal(text: String): Double? {
        val totalLine = text.lines().lastOrNull { line ->
            val lower = line.lowercase()
            lower.contains("total") && !lower.contains("subtotal") && !lower.contains("sub-total") && !lower.contains("sub total")
        }
        totalLine?.let { line ->
            numberRegex.findAll(line).lastOrNull()?.value?.toAmountOrNull()?.let { return it }
        }

        AmountPatterns.patterns.firstNotNullOfOrNull { pattern -> pattern.find(text)?.groupValues?.get(1) }
            ?.toAmountOrNull()
            ?.let { return it }

        return numberRegex.findAll(text).mapNotNull { it.value.toAmountOrNull() }.maxOrNull()
    }

    private fun String.toAmountOrNull(): Double? = replace(",", "").toDoubleOrNull()
}
