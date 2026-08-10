package com.phapalesai.dhanapala.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
fun HomeScreen(viewModel: HomeViewModel = viewModel(), onAddTransaction: () -> Unit = {}) {
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

            ActionTilesRow(
                hasPermission = hasPermission,
                onScan = { if (hasPermission) viewModel.scanSms() else permissionLauncher.launch(Manifest.permission.READ_SMS) },
                onAddTransaction = onAddTransaction
            )

            if (state.settings.userName.isBlank()) {
                NamePromptCard(onSave = viewModel::setUserName)
            }

            if (!state.hasBudgetSet) {
                SetBudgetCard(onSave = viewModel::setBudget, onSaveCustom = viewModel::setCustomBudget)
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
private fun ActionTilesRow(hasPermission: Boolean, onScan: () -> Unit, onAddTransaction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionTile(
            label = if (hasPermission) "Scan SMS" else "Grant SMS access",
            icon = Icons.Filled.Refresh,
            colors = listOf(Color(0xFF00B383), Color(0xFF00E5A0)),
            onClick = onScan,
            modifier = Modifier.weight(1f)
        )
        ActionTile(
            label = "Add Transaction",
            icon = null,
            colors = listOf(Color(0xFFB8860B), Color(0xFFFFC857)),
            onClick = onAddTransaction,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(colors))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color(0xFF06110C))
            } else {
                Text("+", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF06110C))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF06110C)
            )
        }
    }
}

@Composable
private fun NamePromptCard(onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("👋 What should I call you?", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Your name") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onSave(text) },
                enabled = text.isNotBlank()
            ) { Text("Save") }
        }
    }
}

@Composable
private fun SetBudgetCard(onSave: (Double) -> Unit, onSaveCustom: (LocalDate, LocalDate, Double) -> Unit) {
    var text by remember { mutableStateOf("") }
    var useCustomPeriod by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Set your budget", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Budget (₹)") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Custom date range", style = MaterialTheme.typography.bodyMedium)
                androidx.compose.material3.Switch(checked = useCustomPeriod, onCheckedChange = { useCustomPeriod = it })
            }
            if (useCustomPeriod) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DatePickerField(
                        label = "From",
                        date = startDate,
                        onDateChange = { startDate = it },
                        modifier = Modifier.weight(1f)
                    )
                    DatePickerField(
                        label = "To",
                        date = endDate,
                        onDateChange = { endDate = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    "Defaults to this calendar month.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = {
                    val amount = text.toDoubleOrNull() ?: return@Button
                    if (useCustomPeriod) onSaveCustom(startDate, endDate, amount) else onSave(amount)
                },
                enabled = text.toDoubleOrNull() != null && (!useCustomPeriod || !endDate.isBefore(startDate))
            ) { Text("Save budget") }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val formatter = remember { java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy") }

    androidx.compose.material3.OutlinedButton(onClick = { showPicker = true }, modifier = modifier) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(date.format(formatter), style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (showPicker) {
        val state = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onDateChange(
                            java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
                        )
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            androidx.compose.material3.DatePicker(state = state)
        }
    }
}

@Composable
private fun BudgetCard(state: HomeUiState, onEditBudget: (Double) -> Unit) {
    val summary = state.summary
    var showEditDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    radius = 520f
                )
            )
    ) {
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

            val animatedPercent by animateFloatAsState(
                targetValue = (summary.percentUsed / 100.0).toFloat().coerceIn(0f, 1f),
                animationSpec = tween(700),
                label = "percentUsed"
            )
            val ringColor = if (summary.percentUsed >= 90) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                BudgetProgressRing(percent = animatedPercent, color = ringColor)
            }

            Text(
                text = "${(summary.percentUsed).roundToInt()}% used",
                style = MaterialTheme.typography.labelSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconStatCard(
                    icon = Icons.Filled.AccountBalanceWallet,
                    iconTint = MaterialTheme.colorScheme.primary,
                    label = "Budget",
                    value = CurrencyFormat.rupees(summary.budget),
                    modifier = Modifier.weight(1f)
                )
                IconStatCard(
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    iconTint = MaterialTheme.colorScheme.error,
                    label = "Spent",
                    value = CurrencyFormat.rupees(summary.spent),
                    modifier = Modifier.weight(1f)
                )
                IconStatCard(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    iconTint = com.phapalesai.dhanapala.ui.theme.DhanapalaGold,
                    label = "Credited",
                    value = CurrencyFormat.rupees(summary.credited),
                    modifier = Modifier.weight(1f)
                )
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
private fun BudgetProgressRing(percent: Float, color: Color) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(64.dp)) {
            val strokeWidth = 8.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * percent,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${(percent * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun IconStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(iconTint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
