package com.phapalesai.dhanapala.ui.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phapalesai.dhanapala.ui.categoryColor
import com.phapalesai.dhanapala.ui.categoryEmoji
import com.phapalesai.dhanapala.ui.theme.DhanapalaGold
import com.phapalesai.dhanapala.ui.theme.DhanapalaGreen
import com.phapalesai.dhanapala.ui.theme.DhanapalaRed
import com.phapalesai.dhanapala.util.CurrencyFormat
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Analytics", style = MaterialTheme.typography.headlineMedium)
            Text(
                "This month at a glance",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            KpiGrid(state)

            if (state.byCategory.isNotEmpty()) {
                InsightsCard(state)
            }

            if (state.byDay.isNotEmpty()) {
                SpendingInsightsCard(state)
            }

            if (state.recurringItems.isNotEmpty()) {
                RecurringCard(state)
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Spending by Category", style = MaterialTheme.typography.titleMedium)
                    if (state.byCategory.isEmpty()) {
                        Text("No spending yet this month.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                CategoryDonutChart(state.byCategory, state.spent)
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.byCategory.forEach { entry ->
                                    CategoryLegendRow(entry)
                                }
                            }
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Daily Spending", style = MaterialTheme.typography.titleMedium)
                    if (state.byDay.isEmpty()) {
                        Text("No spending yet this month.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        DailySpendingChart(state.byDay)
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiGrid(state: AnalyticsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                label = "Spent",
                value = CurrencyFormat.rupees(state.spent),
                accent = DhanapalaRed,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = "Remaining",
                value = CurrencyFormat.rupees(state.remaining),
                accent = if (state.remaining < 0) DhanapalaRed else DhanapalaGreen,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                label = "% of Budget Used",
                value = "${state.percentUsed.roundToInt()}%",
                accent = DhanapalaGold,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = "Avg Daily Spend",
                value = CurrencyFormat.rupees(state.avgDailySpend),
                accent = DhanapalaGold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, accent: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

@Composable
private fun InsightsCard(state: AnalyticsUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("📌 Insights", style = MaterialTheme.typography.titleMedium)
            state.topCategory?.let {
                Text(
                    "Top category: ${categoryEmoji(it.category)} ${it.category} — ${CurrencyFormat.rupees(it.amount)} (${it.percentOfSpend.roundToInt()}% of spend)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            state.topDay?.let {
                val formatter = DateTimeFormatter.ofPattern("d MMM")
                Text(
                    "Highest spending day: ${it.date.format(formatter)} — ${CurrencyFormat.rupees(it.amount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                "${state.transactionCount} transactions this month.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SpendingInsightsCard(state: AnalyticsUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📈 Spending Insights", style = MaterialTheme.typography.titleMedium)

            val projectedPercent = if (state.budget > 0) (state.projectedPeriodEndSpend / state.budget * 100.0).roundToInt() else null
            Text(
                buildString {
                    append("Projected period-end spend: ${CurrencyFormat.rupees(state.projectedPeriodEndSpend)}")
                    if (projectedPercent != null) append(" (~$projectedPercent% of budget)")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (projectedPercent != null && projectedPercent > 100) DhanapalaRed else MaterialTheme.colorScheme.onSurface
            )

            if (state.weekdayAvgSpend > 0 || state.weekendAvgSpend > 0) {
                val diffText = when {
                    state.weekdayAvgSpend <= 0.0 -> null
                    state.weekendAvgSpend > state.weekdayAvgSpend -> {
                        val pct = ((state.weekendAvgSpend / state.weekdayAvgSpend - 1.0) * 100.0).roundToInt()
                        "Weekends cost $pct% more than weekdays on average."
                    }
                    state.weekendAvgSpend < state.weekdayAvgSpend -> {
                        val pct = ((1.0 - state.weekendAvgSpend / state.weekdayAvgSpend) * 100.0).roundToInt()
                        "Weekends cost $pct% less than weekdays on average."
                    }
                    else -> "Weekday and weekend spending are about the same."
                }
                Text(
                    "Weekday avg ${CurrencyFormat.rupees(state.weekdayAvgSpend)}/day · Weekend avg ${CurrencyFormat.rupees(state.weekendAvgSpend)}/day",
                    style = MaterialTheme.typography.bodyMedium
                )
                diffText?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RecurringCard(state: AnalyticsUiState) {
    val formatter = remember { DateTimeFormatter.ofPattern("d MMM") }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🔁 Recurring & Subscriptions", style = MaterialTheme.typography.titleMedium)
            Text(
                "Detected from your SMS history -- same amount, roughly the same gap each time.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            state.recurringItems.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("${categoryEmoji(item.category)} ${item.category}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${if (item.isMonthly) "Monthly" else "Weekly"} · next ~${item.nextExpectedDate.format(formatter)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(CurrencyFormat.rupees(item.amount), fontWeight = FontWeight.Bold)
                }
            }
            Text(
                "Estimated monthly total: ${CurrencyFormat.rupees(state.recurringMonthlyTotal)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = DhanapalaGold
            )
        }
    }
}

@Composable
private fun CategoryDonutChart(byCategory: List<CategorySpend>, totalSpent: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.height(150.dp).width(150.dp)) {
            val strokeWidth = 26.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = androidx.compose.ui.geometry.Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)
            var startAngle = -90f
            byCategory.forEach { entry ->
                val sweep = (entry.percentOfSpend / 100.0 * 360.0).toFloat()
                drawArc(
                    color = categoryColor(entry.category),
                    startAngle = startAngle,
                    sweepAngle = sweep.coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total", style = MaterialTheme.typography.labelSmall)
            Text(
                CurrencyFormat.rupees(totalSpent),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CategoryLegendRow(entry: CategorySpend) {
    val color = categoryColor(entry.category)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${categoryEmoji(entry.category)} ${entry.category}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                CurrencyFormat.rupees(entry.amount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "${entry.percentOfSpend.roundToInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun DailySpendingChart(byDay: List<DailySpend>) {
    val maxAmount = byDay.maxOf { it.amount }.coerceAtLeast(1.0)
    val barColor = MaterialTheme.colorScheme.primary
    val peakColor = DhanapalaGold
    val dayFormatter = DateTimeFormatter.ofPattern("d")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        byDay.forEach { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(20.dp)
            ) {
                val heightFraction = (day.amount / maxAmount).toFloat().coerceIn(0.02f, 1f)
                val isPeak = day.amount == maxAmount
                Canvas(
                    modifier = Modifier
                        .width(12.dp)
                        .height((95 * heightFraction).dp)
                ) {
                    val radius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    drawRoundRect(
                        color = if (isPeak) peakColor else barColor,
                        cornerRadius = radius
                    )
                }
                Text(
                    text = day.date.format(dayFormatter),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
