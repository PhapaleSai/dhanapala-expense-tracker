package com.phapalesai.dhanapala.data.parser

import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.data.sms.RawSms

data class ParsedTransaction(
    val type: TransactionType,
    val amount: Double,
    val sourceSmsId: String,
    val sender: String?,
    val dateMillis: Long,
    val rawBody: String,
    val dedupeHash: String
) {
    companion object {
        fun from(sms: RawSms, type: TransactionType, amount: Double): ParsedTransaction =
            ParsedTransaction(
                type = type,
                amount = amount,
                sourceSmsId = sms.id,
                sender = sms.address,
                dateMillis = sms.dateMillis,
                rawBody = sms.body,
                dedupeHash = DedupeHash.forSms(sms, amount)
            )
    }
}
