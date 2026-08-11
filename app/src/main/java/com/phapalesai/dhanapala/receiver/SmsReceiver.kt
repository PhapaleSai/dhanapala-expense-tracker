package com.phapalesai.dhanapala.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.phapalesai.dhanapala.DhanapalaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Fires the instant a new SMS lands, so bank/UPI transactions are caught and
 * notified about in real time instead of waiting for the user to open the
 * app. Read-only: never writes, edits, deletes, or sends SMS.
 *
 * Re-reads the inbox via the content provider (rather than building
 * transactions from the broadcast's SmsMessage parts directly) so every
 * inserted row carries its real provider _id — that id feeds the dedupe
 * hash, and a synthesized id here would mismatch the one a later full
 * inbox rescan computes, causing duplicate transactions.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val app = context.applicationContext as DhanapalaApplication
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Some OEMs write the incoming message to the SMS provider a
                // beat after broadcasting SMS_RECEIVED; a short delay avoids
                // reading the inbox before the row actually lands.
                delay(1500)
                val recent = app.smsReader.readInbox(limit = 5)
                val result = app.transactionRepository.scanMessages(recent)
                app.transactionAlertService.notifyForScan(result)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
