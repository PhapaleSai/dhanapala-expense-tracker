package com.phapalesai.dhanapala.ui.screens.messages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phapalesai.dhanapala.data.sms.RawSms
import com.phapalesai.dhanapala.data.sms.SmsReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    private val smsReader = SmsReader(application)

    private val _messages = MutableStateFlow<List<RawSms>>(emptyList())
    val messages: StateFlow<List<RawSms>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadInbox() {
        viewModelScope.launch {
            _isLoading.value = true
            _messages.value = smsReader.readInbox()
            _isLoading.value = false
        }
    }
}
