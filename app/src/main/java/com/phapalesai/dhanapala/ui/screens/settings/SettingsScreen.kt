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
