package com.phapalesai.dhanapala.data.parser

import com.phapalesai.dhanapala.data.sms.RawSms
import java.security.MessageDigest

/**
 * Stable identifier for an SMS-derived transaction, used to prevent duplicate
 * inserts when the same SMS is scanned more than once. Prefers the SMS id
 * itself; combined with sender/date/amount/body so a hash collision would
 * require all of those to match too.
 */
object DedupeHash {
    fun forSms(sms: RawSms, amount: Double): String {
        val input = "${sms.id}|${sms.address}|${sms.dateMillis}|$amount|${sms.body}"
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }
}
