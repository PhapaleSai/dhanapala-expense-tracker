package com.phapalesai.dhanapala.ui.screens.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.domain.DhanpalChatEngine
import com.phapalesai.dhanapala.domain.roastLanguageEnum
import com.phapalesai.dhanapala.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

data class ChatMessage(val text: String, val isUser: Boolean)

@OptIn(ExperimentalCoroutinesApi::class)
class DhanpalChatViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DhanapalaApplication
    private val budgetRepo = app.budgetRepository
    private val transactionRepo = app.transactionRepository
    private val zone = ZoneId.systemDefault()

    private val _messages = MutableStateFlow(
        listOf(ChatMessage("Poochh bhai, kya afford kar sakta hai? 💬", isUser = false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val remaining: StateFlow<Double> = budgetRepo.observeActive(System.currentTimeMillis())
        .flatMapLatest { budget ->
            val range = budget?.let { it.startDateMillis to it.endDateMillis }
                ?: DateUtils.monthRangeMillis(YearMonth.now()).let { it.first to it.last }
            transactionRepo.observeBetween(range.first, range.second).map { transactions ->
                val periodEnd = budget?.let { Instant.ofEpochMilli(it.endDateMillis).atZone(zone).toLocalDate() }
                val spent = transactions
                    .filter { it.type == com.phapalesai.dhanapala.data.local.TransactionType.DEBIT }
                    .sumOf { it.amount }
                (budget?.amount ?: 0.0) - spent
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        _messages.value = _messages.value + ChatMessage(text.trim(), isUser = true)
        viewModelScope.launch {
            val settings = budgetRepo.observeSettings().first()
            val reply = DhanpalChatEngine.respond(text, remaining.value, settings.roastLanguageEnum)
            _messages.value = _messages.value + ChatMessage(reply, isUser = false)
        }
    }
}
