package com.phapalesai.dhanapala.ui.screens.accounts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.domain.AccountShameCommentary
import com.phapalesai.dhanapala.domain.roastLanguageEnum
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountSummary(
    val sender: String,
    val displayName: String,
    val totalSpent: Double,
    val transactionCount: Int,
    val rank: Int,
    val shameCommentary: String?
)

class AccountsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val transactionRepo = app.transactionRepository
    private val nicknameRepo = app.accountNicknameRepository
    private val budgetRepo = app.budgetRepository

    private data class UnrankedAccount(val sender: String, val displayName: String, val totalSpent: Double, val transactionCount: Int)

    val accounts: StateFlow<List<AccountSummary>> = combine(
        transactionRepo.observeAll(),
        nicknameRepo.observeAll(),
        budgetRepo.observeSettings()
    ) { transactions, nicknames, settings ->
        val nicknameMap = nicknames.associate { it.senderPattern to it.displayName }
        transactions
            .filter { !it.sender.isNullOrBlank() }
            .groupBy { it.sender!! }
            .map { (sender, txs) ->
                UnrankedAccount(
                    sender = sender,
                    displayName = nicknameMap[sender] ?: sender,
                    totalSpent = txs.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount },
                    transactionCount = txs.size
                )
            }
            .sortedByDescending { it.totalSpent }
            .mapIndexed { index, account ->
                val rank = index + 1
                AccountSummary(
                    sender = account.sender,
                    displayName = account.displayName,
                    totalSpent = account.totalSpent,
                    transactionCount = account.transactionCount,
                    rank = rank,
                    shameCommentary = if (account.totalSpent > 0) {
                        AccountShameCommentary.forRank(rank, settings.roastLanguageEnum)
                    } else {
                        null
                    }
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setNickname(sender: String, name: String) {
        viewModelScope.launch { nicknameRepo.setNickname(sender, name.trim()) }
    }
}
