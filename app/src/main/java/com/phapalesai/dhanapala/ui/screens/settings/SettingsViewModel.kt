package com.phapalesai.dhanapala.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.AppSettingsEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val budgetRepo = app.budgetRepository
    private val transactionRepo = app.transactionRepository
    private val smsReader = app.smsReader

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
        viewModelScope.launch { transactionRepo.deleteAll() }
    }
}
