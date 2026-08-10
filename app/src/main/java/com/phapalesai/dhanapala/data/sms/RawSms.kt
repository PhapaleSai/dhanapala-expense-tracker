package com.phapalesai.dhanapala.data.sms

data class RawSms(
    val id: String,
    val address: String?,
    val body: String,
    val dateMillis: Long
)
