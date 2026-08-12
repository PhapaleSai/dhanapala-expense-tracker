package com.phapalesai.dhanapala.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.MainActivity
import com.phapalesai.dhanapala.domain.BudgetCalculator
import com.phapalesai.dhanapala.util.CurrencyFormat
import com.phapalesai.dhanapala.util.DateUtils
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.roundToInt

private val BackgroundColor = Color(0xFF0B0C0F)
private val GreenColor = Color(0xFF00E5A0)
private val AmberColor = Color(0xFFFFC857)
private val ErrorColor = Color(0xFFFF5C5C)
private val MutedColor = Color(0xFFB0B0B0)

/** Green under 60% used, amber 60-90%, red past that — same tiers as the Broke-o-Meter gauge. */
private fun accentColorFor(percentUsed: Double): Color = when {
    percentUsed >= 90 -> ErrorColor
    percentUsed >= 60 -> AmberColor
    else -> GreenColor
}

/** Glanceable remaining-budget + % used, tap to open the app. Read-only, no interactive add-transaction. */
class DhanpalWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as DhanapalaApplication
        val zone = ZoneId.systemDefault()
        val nowMillis = System.currentTimeMillis()
        val budget = app.budgetRepository.getActiveOnce(nowMillis)
        val range = budget?.let { it.startDateMillis to it.endDateMillis }
            ?: DateUtils.monthRangeMillis(YearMonth.now()).let { it.first to it.last }
        val transactions = app.transactionRepository.getBetweenOnce(range.first, range.second)
        val periodEnd = budget?.let { Instant.ofEpochMilli(it.endDateMillis).atZone(zone).toLocalDate() }
        val summary = if (budget != null && periodEnd != null) {
            BudgetCalculator.calculate(budget.amount, transactions, periodEnd = periodEnd)
        } else {
            BudgetCalculator.calculate(0.0, transactions)
        }

        val openAppIntent = Intent(context, MainActivity::class.java)
        val accentColor = accentColorFor(summary.percentUsed)
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(BackgroundColor))
                    .padding(12.dp)
                    .clickable(actionStartActivity(openAppIntent))
            ) {
                Text(
                    "Dhanpal",
                    style = TextStyle(color = ColorProvider(accentColor), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                )
                Text(
                    "Remaining",
                    style = TextStyle(color = ColorProvider(MutedColor), fontSize = 11.sp)
                )
                Text(
                    CurrencyFormat.rupees(summary.remaining),
                    style = TextStyle(
                        color = ColorProvider(if (summary.remaining < 0) ErrorColor else accentColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
                Text(
                    "${summary.percentUsed.roundToInt()}% used",
                    style = TextStyle(color = ColorProvider(MutedColor), fontSize = 11.sp)
                )
            }
        }
    }
}
