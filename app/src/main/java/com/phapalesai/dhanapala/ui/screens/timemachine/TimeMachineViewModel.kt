package com.phapalesai.dhanapala.ui.screens.timemachine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TimeMachineViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val transactionRepo = app.transactionRepository

    val pastExpenses: StateFlow<List<TransactionEntity>> = transactionRepo.observeAll()
        .map { list -> list.filter { it.type == TransactionType.DEBIT }.sortedByDescending { it.dateMillis } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
