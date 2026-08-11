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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import com.phapalesai.dhanapala.ui.theme.DhanapalaGold
import com.phapalesai.dhanapala.util.CurrencyFormat
import com.phapalesai.dhanapala.util.Greeting
import kotlinx.coroutines.delay
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
    var hasReceiveSmsPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }
    // RECEIVE_SMS is requested alongside READ_SMS so the live SmsReceiver can
    // fire and notify the moment a transaction SMS arrives, not just when
    // the user next opens the app.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.READ_SMS] == true
        hasPermission = granted
        hasReceiveSmsPermission = results[Manifest.permission.RECEIVE_SMS] == true
        viewModel.onSmsPermissionResult(granted)
    }
    val requestSmsPermissions = {
        permissionLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS))
    }

    // Covers users upgrading from a version that only ever asked for
    // READ_SMS: they'd otherwise never be prompted for RECEIVE_SMS and the
    // live receiver would silently never fire.
    LaunchedEffect(hasPermission, hasReceiveSmsPermission) {
        if (hasPermission && !hasReceiveSmsPermission) {
            requestSmsPermissions()
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EnterAnimated(delayMillis = 0) {
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
                            style = MaterialTheme.typography.headlineMedium.copy(
                                brush = Brush.linearGradient(
                                    listOf(MaterialTheme.colorScheme.primary, DhanapalaGold)
                                )
                            ),
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (state.welcomeMessage != null) {
                            Text(
                                text = state.welcomeMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    IconButton(
                        onClick = {
                            if (hasPermission) viewModel.scanSms() else requestSmsPermissions()
                        }
                    ) {
                        if (state.isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = "Scan SMS", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            EnterAnimated(delayMillis = 40) {
                ActionTilesRow(
                    hasPermission = hasPermission,
                    onScan = { if (hasPermission) viewModel.scanSms() else requestSmsPermissions() },
                    onAddTransaction = onAddTransaction
                )
            }

            if (state.settings.userName.isBlank()) {
                EnterAnimated(delayMillis = 80) { NamePromptCard(onSave = viewModel::setUserName) }
            }

            EnterAnimated(delayMillis = 120) {
                if (!state.hasBudgetSet) {
                    SetBudgetCard(onSave = viewModel::setBudget, onSaveCustom = viewModel::setCustomBudget)
                } else {
                    BudgetCard(state, onEditBudget = viewModel::editActiveBudgetPeriod)
                }
            }

            EnterAnimated(delayMillis = 180) { MoneyJokeCard(state.moneyJoke) }

            EnterAnimated(delayMillis = 220) { BhaiMeterCard(state.bhaiMessage) }

            EnterAnimated(delayMillis = 260) { MoneyTipCard(state.moneyTip) }

            EnterAnimated(delayMillis = 300) {
                ScanSmsCard(
                    hasPermission = hasPermission,
                    isScanning = state.isScanning,
                    lastScanResult = state.lastScanResult,
                    onRequestPermission = { requestSmsPermissions() },
                    onScan = viewModel::scanSms
                )
            }

            EnterAnimated(delayMillis = 340) { RecentTransactionsCard(state.recentTransactions) }
        }
        }
    }
}

/** Staggered fade + slide-up entrance for home cards, once per composition. */
@Composable
private fun EnterAnimated(delayMillis: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 5 }
    ) {
        content()
    }
}

/** Gradient halo + hairline border, the premium "glow" treatment used across Home cards. */
@Composable
private fun GlowCard(
    modifier: Modifier = Modifier,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.16f), Color.Transparent),
                    radius = 420f
                )
            )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(listOf(glowColor.copy(alpha = 0.45f), Color.Transparent))
            )
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                content()
            }
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, animationSpec = tween(120), label = "tileScale")

    Box(
        modifier = modifier
            .scale(scale)
            .height(84.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(colors))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
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
            // DatePicker's selectedDateMillis is always UTC midnight of the shown date,
            // regardless of device timezone — must match here or the wrong day shows selected.
            initialSelectedDateMillis = date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
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
private fun BudgetCard(state: HomeUiState, onEditBudget: (LocalDate, LocalDate, Double) -> Unit) {
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
        var startDate by remember { mutableStateOf(state.periodStart ?: LocalDate.now()) }
        var endDate by remember {
            mutableStateOf(state.periodEnd ?: LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()))
        }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Change budget") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Budget (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        text.toDoubleOrNull()?.let { onEditBudget(startDate, endDate, it) }
                        showEditDialog = false
                    },
                    enabled = text.toDoubleOrNull() != null && !endDate.isBefore(startDate)
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
    GlowCard(glowColor = DhanapalaGold) {
        Text("💡 Money-Saving Tip", style = MaterialTheme.typography.titleMedium)
        Text(text = tip, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MoneyJokeCard(joke: String?) {
    if (joke == null) return
    GlowCard(glowColor = Color(0xFFFF8A65)) {
        Text("😂 Money Joke", style = MaterialTheme.typography.titleMedium)
        Text(text = joke, style = MaterialTheme.typography.bodyMedium)
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
    GlowCard {
        Text("💰 Bhai Meter", style = MaterialTheme.typography.titleMedium)
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
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
                transactions.forEachIndexed { index, tx ->
                    EnterAnimated(delayMillis = 380 + index * 40) {
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
}
