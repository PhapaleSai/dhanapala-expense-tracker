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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Spending by Category", style = MaterialTheme.typography.titleMedium)
                    if (state.byCategory.isEmpty()) {
                        Text("No spending yet this month.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        state.byCategory.forEach { entry ->
                            CategoryBar(entry)
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
private fun CategoryBar(entry: CategorySpend) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${categoryEmoji(entry.category)} ${entry.category}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "${CurrencyFormat.rupees(entry.amount)}  ·  ${entry.percentOfSpend.roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((entry.percentOfSpend / 100.0).toFloat().coerceIn(0f, 1f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
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
                    drawRect(color = if (isPeak) peakColor else barColor)
                }
                Text(
                    text = day.date.format(dayFormatter),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
