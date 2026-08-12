package com.phapalesai.dhanapala.ui.screens.timemachine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.domain.InvestmentTimeMachine
import com.phapalesai.dhanapala.ui.categoryEmoji
import com.phapalesai.dhanapala.ui.theme.DhanapalaGold
import com.phapalesai.dhanapala.util.CurrencyFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun TimeMachineScreen(viewModel: TimeMachineViewModel = viewModel()) {
    val expenses by viewModel.pastExpenses.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<TransactionEntity?>(null) }
    var annualRate by remember { mutableFloatStateOf(InvestmentTimeMachine.DEFAULT_ANNUAL_RATE_PERCENT.toFloat()) }

    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("⏳ Time Machine", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Pick a past expense and see what it could've been worth if you'd invested it instead. Purely hypothetical — not investment advice.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val current = selected
            if (current == null) {
                if (expenses.isEmpty()) {
                    Text("No expenses yet to time-travel with.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(expenses) { tx -> ExpenseRow(tx, onClick = { selected = tx }) }
                    }
                }
            } else {
                TimeMachineResult(
                    transaction = current,
                    annualRate = annualRate,
                    onRateChange = { annualRate = it },
                    onBack = { selected = null }
                )
            }
        }
    }
}

@Composable
private fun ExpenseRow(tx: TransactionEntity, onClick: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy") }
    val date = remember(tx.dateMillis) { Instant.ofEpochMilli(tx.dateMillis).atZone(ZoneId.systemDefault()).toLocalDate() }
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("${categoryEmoji(tx.category)} ${tx.category}", style = MaterialTheme.typography.bodyMedium)
                Text(date.format(formatter), style = MaterialTheme.typography.labelSmall)
            }
            Text(CurrencyFormat.rupees(tx.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TimeMachineResult(
    transaction: TransactionEntity,
    annualRate: Float,
    onRateChange: (Float) -> Unit,
    onBack: () -> Unit
) {
    val fromDate = remember(transaction.dateMillis) {
        Instant.ofEpochMilli(transaction.dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val futureValue = remember(transaction.amount, fromDate, annualRate) {
        InvestmentTimeMachine.futureValue(transaction.amount, fromDate, annualRatePercent = annualRate.toDouble())
    }
    val multiplier = if (transaction.amount > 0) futureValue / transaction.amount else 1.0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("← Pick a different expense", modifier = Modifier.clickable(onClick = onBack), style = MaterialTheme.typography.labelMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "${categoryEmoji(transaction.category)} ${CurrencyFormat.rupees(transaction.amount)} on ${transaction.category}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Assumed annual return: ${annualRate.roundToInt()}%", style = MaterialTheme.typography.bodyMedium)
                Slider(value = annualRate, onValueChange = onRateChange, valueRange = 4f..20f, steps = 15)
                Text(
                    "Could be worth today:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    CurrencyFormat.rupees(futureValue),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = DhanapalaGold
                )
                Text(
                    "That's ${String.format("%.1f", multiplier)}x what you spent — gone, into ${transaction.category.lowercase()}.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
