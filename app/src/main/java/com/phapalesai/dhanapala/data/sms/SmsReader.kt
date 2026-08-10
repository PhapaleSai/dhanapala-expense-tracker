package com.phapalesai.dhanapala.data.sms

import android.content.Context
import android.provider.Telephony
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Read-only access to the device SMS inbox. Never writes, edits, deletes, or sends SMS.
 */
class SmsReader(private val context: Context) {

    suspend fun readInbox(limit: Int = 200): List<RawSms> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<RawSms>()
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )
        val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT $limit"

        context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addressIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (cursor.moveToNext()) {
                messages.add(
                    RawSms(
                        id = cursor.getString(idIdx),
                        address = cursor.getString(addressIdx),
                        body = cursor.getString(bodyIdx) ?: "",
                        dateMillis = cursor.getLong(dateIdx)
                    )
                )
            }
        }
        messages
    }
}
