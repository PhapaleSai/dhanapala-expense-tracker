package com.phapalesai.dhanapala.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import android.net.Uri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onViewRawSms: () -> Unit = {},
    onSplitBill: () -> Unit = {},
    onManageAccounts: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenTimeMachine: () -> Unit = {},
    onOpenSplitGroups: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var thresholdText by remember(settings.largeExpenseThreshold) {
        mutableStateOf(settings.largeExpenseThreshold.toInt().toString())
    }
    var showResetConfirm by remember { mutableStateOf(false) }
    var nameText by remember(settings.userName) { mutableStateOf(settings.userName) }

    var showExportPassphraseDialog by remember { mutableStateOf(false) }
    var showImportPassphraseDialog by remember { mutableStateOf(false) }
    var pendingBackupUri by remember { mutableStateOf<Uri?>(null) }
    var backupStatus by remember { mutableStateOf<String?>(null) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        showExportPassphraseDialog = true
        pendingBackupUri = uri // reused as the export destination while the passphrase dialog is up
    }
    val openBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        pendingBackupUri = uri
        showImportPassphraseDialog = true
    }

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
                    SettingToggleRow(
                        label = "🔊 Voice Roasts",
                        checked = settings.voiceRoastsEnabled,
                        onCheckedChange = { viewModel.updateSettings { s -> s.copy(voiceRoastsEnabled = it) } }
                    )
                    if (settings.voiceRoastsEnabled) {
                        Text(
                            "Reads the Bhai Meter message aloud when you open the app. On-device only.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Text("Tools", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(onClick = onSplitBill, modifier = Modifier.fillMaxWidth()) { Text("🧾 Split a Bill") }
                    OutlinedButton(onClick = onManageAccounts, modifier = Modifier.fillMaxWidth()) { Text("🏦 Manage Accounts") }
                    OutlinedButton(onClick = onOpenChat, modifier = Modifier.fillMaxWidth()) { Text("💬 Chat with Dhanpal") }
                    OutlinedButton(onClick = onOpenTimeMachine, modifier = Modifier.fillMaxWidth()) { Text("⏳ Time Machine") }
                    OutlinedButton(onClick = onOpenSplitGroups, modifier = Modifier.fillMaxWidth()) { Text("🤝 Split Groups") }
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
                    Text("Backup & Restore", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "A full, password-encrypted snapshot of everything -- transactions, budgets, settings. " +
                            "Restoring replaces all current data. Nothing leaves the device, and the password " +
                            "never gets stored anywhere -- forgetting it means the backup can't be decrypted.",
                        style = MaterialTheme.typography.labelSmall
                    )
                    backupStatus?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = {
                            backupStatus = null
                            val filename = "dhanpal-backup-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())}.dhpb"
                            createBackupLauncher.launch(filename)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Backup Data") }
                    OutlinedButton(
                        onClick = {
                            backupStatus = null
                            openBackupLauncher.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Restore from Backup") }
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

    if (showExportPassphraseDialog) {
        PassphraseDialog(
            title = "Set a backup password",
            description = "You'll need this exact password to restore this backup later. It's never saved anywhere.",
            requireConfirmation = true,
            confirmLabel = "Encrypt & Save",
            onDismiss = {
                showExportPassphraseDialog = false
                pendingBackupUri = null
            },
            onConfirm = { passphrase ->
                val uri = pendingBackupUri
                showExportPassphraseDialog = false
                if (uri != null) {
                    viewModel.exportEncryptedBackup(passphrase) { result ->
                        result.onSuccess { bytes ->
                            runCatching {
                                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                            }.onSuccess {
                                backupStatus = "Backup saved."
                            }.onFailure {
                                backupStatus = "Couldn't write the backup file: ${it.message}"
                            }
                        }.onFailure {
                            backupStatus = "Export failed: ${it.message}"
                        }
                    }
                }
                pendingBackupUri = null
            }
        )
    }

    if (showImportPassphraseDialog) {
        PassphraseDialog(
            title = "Restore from backup",
            description = "This replaces every transaction, budget, and setting currently on this device. Enter the password this backup was encrypted with.",
            requireConfirmation = false,
            confirmLabel = "Restore",
            onDismiss = {
                showImportPassphraseDialog = false
                pendingBackupUri = null
            },
            onConfirm = { passphrase ->
                val uri = pendingBackupUri
                showImportPassphraseDialog = false
                if (uri != null) {
                    val bytes = runCatching {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }.getOrNull()
                    if (bytes == null) {
                        backupStatus = "Couldn't read that file."
                    } else {
                        viewModel.importEncryptedBackup(bytes, passphrase) { result ->
                            result.onSuccess {
                                backupStatus = "Restore complete."
                            }.onFailure {
                                backupStatus = "Restore failed -- wrong password, or not a Dhanpal backup file."
                            }
                        }
                    }
                }
                pendingBackupUri = null
            }
        )
    }
}

@Composable
private fun PassphraseDialog(
    title: String,
    description: String,
    requireConfirmation: Boolean,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var passphrase by remember { mutableStateOf("") }
    var confirmPassphrase by remember { mutableStateOf("") }
    val mismatch = requireConfirmation && confirmPassphrase.isNotEmpty() && passphrase != confirmPassphrase
    val canConfirm = passphrase.length >= 4 && (!requireConfirmation || passphrase == confirmPassphrase)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(description, style = MaterialTheme.typography.labelSmall)
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (requireConfirmation) {
                    OutlinedTextField(
                        value = confirmPassphrase,
                        onValueChange = { confirmPassphrase = it },
                        label = { Text("Confirm password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = mismatch,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (mismatch) {
                        Text("Passwords don't match.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(passphrase) }, enabled = canConfirm) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
