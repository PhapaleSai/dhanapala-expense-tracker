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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phapalesai.dhanapala.ui.categoryEmoji
import com.phapalesai.dhanapala.util.CurrencyFormat
import java.time.format.DateTimeFormatter

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

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Monthly budget usage", style = MaterialTheme.typography.titleMedium)
                    Text("Budget: ${CurrencyFormat.rupees(state.budget)}")
                    Text("Spent: ${CurrencyFormat.rupees(state.spent)}")
                    Text("Remaining: ${CurrencyFormat.rupees(state.remaining)}")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Spending by category", style = MaterialTheme.typography.titleMedium)
                    if (state.byCategory.isEmpty()) {
                        Text("No spending yet this month.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        val maxAmount = state.byCategory.maxOf { it.amount }
                        state.byCategory.forEach { entry ->
                            CategoryBar(
                                label = "${categoryEmoji(entry.category)} ${entry.category}",
                                amount = entry.amount,
                                fraction = (entry.amount / maxAmount).toFloat()
                            )
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Daily spending", style = MaterialTheme.typography.titleMedium)
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
private fun CategoryBar(label: String, amount: Double, fraction: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(CurrencyFormat.rupees(amount), style = MaterialTheme.typography.bodyMedium)
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
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
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
    val dayFormatter = DateTimeFormatter.ofPattern("d")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        byDay.forEach { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(20.dp)
            ) {
                val heightFraction = (day.amount / maxAmount).toFloat().coerceIn(0.02f, 1f)
                Canvas(
                    modifier = Modifier
                        .width(12.dp)
                        .height((90 * heightFraction).dp)
                ) {
                    drawRect(color = barColor)
                }
                Text(
                    text = day.date.format(dayFormatter),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
