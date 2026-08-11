package com.phapalesai.dhanapala.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.phapalesai.dhanapala.R
import com.phapalesai.dhanapala.util.CurrencyFormat
import kotlin.random.Random

/**
 * Wraps Android's notification APIs. Fires for every parsed debit/credit
 * SMS (caught live via SmsReceiver, or on a manual rescan) plus
 * budget-threshold crossings — see TransactionAlertService for the caller.
 */
class DhanapalaNotifier(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "dhanapala_alerts"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Dhanapala Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun notifyExpense(amount: Double, bhaiMessage: String) {
        show(
            title = "💸 Bhai Alert!",
            text = "${CurrencyFormat.rupees(amount)} spent.\n$bhaiMessage"
        )
    }

    fun notifySalaryCredit(amount: Double, bhaiMessage: String) {
        show(
            title = "🤑 Salary credited",
            text = "${CurrencyFormat.rupees(amount)} credited.\n$bhaiMessage"
        )
    }

    fun notifyMoneyReceived(amount: Double, bhaiMessage: String) {
        show(
            title = "💰 Money received",
            text = "${CurrencyFormat.rupees(amount)} credited.\n$bhaiMessage"
        )
    }

    fun notifyBudgetThreshold(percentUsed: Int, remaining: Double) {
        show(
            title = "⚠️ Budget Alert",
            text = "You've used $percentUsed% of your monthly budget.\n${CurrencyFormat.rupees(remaining)} remaining."
        )
    }

    fun notifyBudgetExceeded() {
        show(
            title = "🚨 BHAI ALERT",
            text = "Monthly budget exceeded.\nAb thoda control kar 😂"
        )
    }

    private fun show(title: String, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()
        androidx.core.app.NotificationManagerCompat.from(context).notify(Random.nextInt(), notification)
    }
}
