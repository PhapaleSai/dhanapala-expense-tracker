package com.phapalesai.dhanapala.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phapalesai.dhanapala.domain.RoastLanguage
import com.phapalesai.dhanapala.domain.RoastLevel
import com.phapalesai.dhanapala.domain.roastLanguageEnum
import com.phapalesai.dhanapala.domain.roastLevelEnum
import com.phapalesai.dhanapala.ui.lock.canUseBiometricLock
import android.content.Intent
import java.io.File

private fun shareCsv(context: android.content.Context, csv: String) {
    val file = File(context.cacheDir, "dhanapala-export-${System.currentTimeMillis()}.csv")
    file.writeText(csv)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Export transactions"))
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel(), onViewRawSms: () -> Unit = {}) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var thresholdText by remember(settings.largeExpenseThreshold) {
        mutableStateOf(settings.largeExpenseThreshold.toInt().toString())
    }
    var showResetConfirm by remember { mutableStateOf(false) }
    var nameText by remember(settings.userName) { mutableStateOf(settings.userName) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your name", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Used to greet you on the dashboard.",
                        style = MaterialTheme.typography.labelSmall
                    )
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = { viewModel.updateSettings { it.copy(userName = nameText.trim()) } }) {
                        Text("Save name")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Large expense threshold", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Bhai Mode gets extra dramatic above this amount per transaction.",
                        style = MaterialTheme.typography.labelSmall
                    )
                    OutlinedTextField(
                        value = thresholdText,
                        onValueChange = { thresholdText = it.filter { c -> c.isDigit() } },
                        label = { Text("Threshold (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            thresholdText.toDoubleOrNull()?.let { value ->
                                viewModel.updateSettings { it.copy(largeExpenseThreshold = value) }
                            }
                        }
                    ) { Text("Save threshold") }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SettingToggleRow(
                        label = "Bhai Mode",
                        checked = settings.bhaiModeEnabled,
                        onCheckedChange = { viewModel.updateSettings { s -> s.copy(bhaiModeEnabled = it) } }
                    )
                    SettingToggleRow(
                        label = "Notifications",
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { viewModel.updateSettings { s -> s.copy(notificationsEnabled = it) } }
                    )
                    SettingToggleRow(
                        label = "Daily reminder",
                        checked = settings.dailyReminderEnabled,
                        onCheckedChange = { viewModel.updateSettings { s -> s.copy(dailyReminderEnabled = it) } }
                    )
                    var biometricUnavailableNotice by remember { mutableStateOf(false) }
                    SettingToggleRow(
                        label = "Biometric Lock",
                        checked = settings.biometricLockEnabled,
                        onCheckedChange = { turningOn ->
                            if (turningOn && !canUseBiometricLock(context)) {
                                biometricUnavailableNotice = true
                            } else {
                                viewModel.updateSettings { s -> s.copy(biometricLockEnabled = turningOn) }
                            }
                        }
                    )
                    if (biometricUnavailableNotice) {
                        Text(
                            "No fingerprint/face unlock or screen lock is set up on this device — set one up in your phone's security settings first.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🌶️ Roast Level", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "How savage should Bhai Mode get?",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RoastLevelOption("😇", "Mild", RoastLevel.MILD, settings.roastLevelEnum) {
                            viewModel.updateSettings { s -> s.copy(roastLevel = it.name) }
                        }
                        RoastLevelOption("😏", "Medium", RoastLevel.MEDIUM, settings.roastLevelEnum) {
                            viewModel.updateSettings { s -> s.copy(roastLevel = it.name) }
                        }
                        RoastLevelOption("🔥", "Savage", RoastLevel.SAVAGE, settings.roastLevelEnum) {
                            viewModel.updateSettings { s -> s.copy(roastLevel = it.name) }
                        }
                    }

                    Text("Roast Language", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LanguageOption("English", RoastLanguage.EN, settings.roastLanguageEnum) {
                            viewModel.updateSettings { s -> s.copy(roastLanguage = it.name) }
                        }
                        LanguageOption("Hindi", RoastLanguage.HI, settings.roastLanguageEnum) {
                            viewModel.updateSettings { s -> s.copy(roastLanguage = it.name) }
                        }
                        LanguageOption("Marathi", RoastLanguage.MR, settings.roastLanguageEnum) {
                            viewModel.updateSettings { s -> s.copy(roastLanguage = it.name) }
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SMS", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = viewModel::rescanSms) { Text("Rescan SMS") }
                    OutlinedButton(onClick = onViewRawSms) { Text("View raw SMS inbox") }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Export", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Generated locally as a CSV and handed to the share sheet — never uploaded.",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Button(onClick = { viewModel.exportCsv { csv -> shareCsv(context, csv) } }) {
                        Text("Export Data")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Danger zone", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(onClick = { showResetConfirm = true }) { Text("Reset all data") }
                }
            }

            Text(
                text = "धनपाल — All rights reserved by Sai.",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset all data?") },
            text = { Text("This deletes every transaction stored in Dhanapala. Your original SMS messages are never touched.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetAllData()
                    showResetConfirm = false
                }) { Text("Delete everything") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun RoastLevelOption(
    emoji: String,
    label: String,
    value: RoastLevel,
    selected: RoastLevel,
    onSelect: (RoastLevel) -> Unit
) {
    androidx.compose.material3.FilterChip(
        selected = value == selected,
        onClick = { onSelect(value) },
        label = { Text("$emoji $label") }
    )
}

@Composable
private fun LanguageOption(
    label: String,
    value: RoastLanguage,
    selected: RoastLanguage,
    onSelect: (RoastLanguage) -> Unit
) {
    androidx.compose.material3.FilterChip(
        selected = value == selected,
        onClick = { onSelect(value) },
        label = { Text(label) }
    )
}
