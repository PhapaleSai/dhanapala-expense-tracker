package com.phapalesai.dhanapala.data.parser

import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.data.sms.RawSms

/**
 * Turns a raw SMS into a ParsedTransaction, or null if it doesn't look like
 * a bank/payment transaction message.
 *
 * An SMS only counts as a transaction if it has BOTH a rupee amount AND a
 * debit/credit keyword — this deliberately excludes OTP/promo messages that
 * merely mention money or "payment" without being a transaction record.
 */
class SmsTransactionParser {

    fun parse(sms: RawSms): ParsedTransaction? {
        val amount = extractAmount(sms.body) ?: return null
        val type = classify(sms.body) ?: return null
        return ParsedTransaction.from(sms, type, amount)
    }

    private fun extractAmount(body: String): Double? {
        for (pattern in AmountPatterns.patterns) {
            val match = pattern.find(body) ?: continue
            val raw = match.groupValues[1].replace(",", "")
            val value = raw.toDoubleOrNull()
            if (value != null) return value
        }
        return null
    }

    /**
     * Credit keywords are checked first: a message like "Rs 500 credited via
     * UPI" contains a weak debit-ish word ("UPI") but is unambiguously a
     * credit, so credit indicators take priority over debit ones.
     */
    private fun classify(body: String): TransactionType? {
        val lower = body.lowercase()
        if (TransactionKeywords.CREDIT.any { lower.contains(it) }) return TransactionType.CREDIT
        if (TransactionKeywords.DEBIT.any { lower.contains(it) }) return TransactionType.DEBIT
        return null
    }
}
