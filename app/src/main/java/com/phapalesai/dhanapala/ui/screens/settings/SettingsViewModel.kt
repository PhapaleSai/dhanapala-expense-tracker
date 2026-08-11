package com.phapalesai.dhanapala.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.backup.BackupCrypto
import com.phapalesai.dhanapala.data.export.CsvExporter
import com.phapalesai.dhanapala.data.local.AppSettingsEntity
import com.phapalesai.dhanapala.widget.WidgetUpdater
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val budgetRepo = app.budgetRepository
    private val transactionRepo = app.transactionRepository
    private val smsReader = app.smsReader
    private val backupManager = app.backupManager

    val settings: StateFlow<AppSettingsEntity> = budgetRepo.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsEntity())

    fun updateSettings(update: (AppSettingsEntity) -> AppSettingsEntity) {
        viewModelScope.launch {
            budgetRepo.updateSettings(update(settings.value))
        }
    }

    fun rescanSms() {
        viewModelScope.launch {
            val messages = smsReader.readInbox(limit = 1000)
            transactionRepo.scanMessages(messages)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            transactionRepo.deleteAll()
            WidgetUpdater.refresh(getApplication())
        }
    }

    fun exportCsv(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val all = transactionRepo.observeAll().first()
            onReady(CsvExporter.toCsv(all))
        }
    }

    fun exportEncryptedBackup(passphrase: String, onResult: (Result<ByteArray>) -> Unit) {
        viewModelScope.launch {
            val result = runCatching {
                val json = backupManager.buildBackupJson()
                BackupCrypto.encrypt(json.toByteArray(Charsets.UTF_8), passphrase)
            }
            onResult(result)
        }
    }

    fun importEncryptedBackup(encryptedBytes: ByteArray, passphrase: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = runCatching {
                val decrypted = BackupCrypto.decrypt(encryptedBytes, passphrase)
                backupManager.restoreFromJson(String(decrypted, Charsets.UTF_8))
            }
            if (result.isSuccess) WidgetUpdater.refresh(getApplication())
            onResult(result)
        }
    }
}
