package com.phapalesai.dhanapala.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phapalesai.dhanapala.R
import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.ui.categoryEmoji
import com.phapalesai.dhanapala.util.CurrencyFormat
import com.phapalesai.dhanapala.util.Greeting
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var hasPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        viewModel.onSmsPermissionResult(granted)
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val monthName = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                        .uppercase(Locale.getDefault())
                    Text(
                        text = "$monthName ${LocalDate.now().year}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    val greeting = Greeting.forTime()
                    Text(
                        text = if (state.settings.userName.isNotBlank()) {
                            "$greeting, ${state.settings.userName} 👋"
                        } else {
                            "$greeting 👋"
                        },
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                IconButton(
                    onClick = {
                        if (hasPermission) viewModel.scanSms() else permissionLauncher.launch(Manifest.permission.READ_SMS)
                    }
                ) {
                    if (state.isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Filled.Refresh, contentDescription = "Scan SMS", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (!state.hasBudgetSet) {
                SetBudgetCard(onSave = viewModel::setBudget)
            } else {
                BudgetCard(state, onEditBudget = viewModel::setBudget)
            }

            BhaiMeterCard(state.bhaiMessage)

            MoneyTipCard(state.moneyTip)

            ScanSmsCard(
                hasPermission = hasPermission,
                isScanning = state.isScanning,
                lastScanResult = state.lastScanResult,
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.READ_SMS) },
                onScan = viewModel::scanSms
            )

            RecentTransactionsCard(state.recentTransactions)
        }
    }
}

@Composable
private fun SetBudgetCard(onSave: (Double) -> Unit) {
    var text by remember { mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Set this month's budget", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Monthly budget (₹)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { text.toDoubleOrNull()?.let(onSave) },
                enabled = text.toDoubleOrNull() != null
            ) { Text("Save budget") }
        }
    }
}

@Composable
private fun BudgetCard(state: HomeUiState, onEditBudget: (Double) -> Unit) {
    val summary = state.summary
    var showEditDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Remaining", style = MaterialTheme.typography.labelSmall)
                androidx.compose.material3.TextButton(onClick = { showEditDialog = true }) {
                    Text("Edit budget")
                }
            }
            val animatedRemaining by animateFloatAsState(
                targetValue = summary.remaining.toFloat(),
                animationSpec = tween(700),
                label = "remaining"
            )
            Text(
                text = CurrencyFormat.rupees(animatedRemaining.toDouble()),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (summary.remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            val animatedPercent by animateFloatAsState(
                targetValue = (summary.percentUsed / 100.0).toFloat().coerceIn(0f, 1f),
                animationSpec = tween(700),
                label = "percentUsed"
            )
            LinearProgressIndicator(
                progress = { animatedPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = if (summary.percentUsed >= 90) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${(summary.percentUsed).roundToInt()}% used",
                style = MaterialTheme.typography.labelSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LabeledAmount("Budget", CurrencyFormat.rupees(summary.budget))
                LabeledAmount("Spent", CurrencyFormat.rupees(summary.spent))
                LabeledAmount("Credited", CurrencyFormat.rupees(summary.credited))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LabeledAmount(
                    "Recommended daily",
                    "${CurrencyFormat.rupees(summary.recommendedDailySpend)}/day"
                )
                LabeledAmount("Days left", "${summary.daysRemainingInMonth}")
            }

            LabeledAmount("Today's spending", CurrencyFormat.rupees(summary.todaySpending))
        }
    }

    if (showEditDialog) {
        var text by remember { mutableStateOf(summary.budget.toInt().toString()) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Change this month's budget") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Monthly budget (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        text.toDoubleOrNull()?.let(onEditBudget)
                        showEditDialog = false
                    },
                    enabled = text.toDoubleOrNull() != null
                ) { Text("Save") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MoneyTipCard(tip: String?) {
    if (tip == null) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("💡 Money-Saving Tip", style = MaterialTheme.typography.titleMedium)
            Text(text = tip, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun LabeledAmount(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun BhaiMeterCard(message: String?) {
    if (message == null) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("💰 Bhai Meter", style = MaterialTheme.typography.titleMedium)
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ScanSmsCard(
    hasPermission: Boolean,
    isScanning: Boolean,
    lastScanResult: com.phapalesai.dhanapala.data.repository.ScanResult?,
    onRequestPermission: () -> Unit,
    onScan: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Scan SMS for transactions", style = MaterialTheme.typography.titleMedium)
            Text(
                "Read-only. Never edits, deletes, or sends SMS.",
                style = MaterialTheme.typography.labelSmall
            )
            when {
                isScanning -> Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Text("Scanning…")
                }
                !hasPermission -> Button(onClick = onRequestPermission) { Text("Grant SMS access") }
                else -> Button(onClick = onScan) { Text("Scan SMS") }
            }
            lastScanResult?.let {
                Text(
                    "Found ${it.inserted} new, ${it.duplicates} already saved, ${it.scanned} SMS scanned.",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun RecentTransactionsCard(transactions: List<TransactionEntity>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
            if (transactions.isEmpty()) {
                Text("No transactions yet this month.", style = MaterialTheme.typography.bodyMedium)
            } else {
                transactions.forEach { tx ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${categoryEmoji(tx.category)} ${tx.category}")
                        val sign = if (tx.type == TransactionType.DEBIT) "-" else "+"
                        Text(
                            text = "$sign${CurrencyFormat.rupees(tx.amount)}",
                            color = if (tx.type == TransactionType.DEBIT) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }
        }
    }
}
