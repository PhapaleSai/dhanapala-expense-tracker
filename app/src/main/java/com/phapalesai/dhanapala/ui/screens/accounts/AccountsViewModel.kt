package com.phapalesai.dhanapala.ui.screens.accounts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountSummary(
    val sender: String,
    val displayName: String,
    val totalSpent: Double,
    val transactionCount: Int
)

class AccountsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val transactionRepo = app.transactionRepository
    private val nicknameRepo = app.accountNicknameRepository

    val accounts: StateFlow<List<AccountSummary>> = combine(
        transactionRepo.observeAll(),
        nicknameRepo.observeAll()
    ) { transactions, nicknames ->
        val nicknameMap = nicknames.associate { it.senderPattern to it.displayName }
        transactions
            .filter { !it.sender.isNullOrBlank() }
            .groupBy { it.sender!! }
            .map { (sender, txs) ->
                AccountSummary(
                    sender = sender,
                    displayName = nicknameMap[sender] ?: sender,
                    totalSpent = txs.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount },
                    transactionCount = txs.size
                )
            }
            .sortedByDescending { it.totalSpent }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setNickname(sender: String, name: String) {
        viewModelScope.launch { nicknameRepo.setNickname(sender, name.trim()) }
    }
}
