package com.phapalesai.dhanapala

import android.app.Application
import com.phapalesai.dhanapala.data.local.AppDatabase
import com.phapalesai.dhanapala.data.repository.BudgetRepository
import com.phapalesai.dhanapala.data.repository.TransactionRepository
import com.phapalesai.dhanapala.data.sms.SmsReader
import com.phapalesai.dhanapala.notification.DhanapalaNotifier
import com.phapalesai.dhanapala.notification.TransactionAlertService

class DhanapalaApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val budgetRepository by lazy { BudgetRepository(database.budgetDao(), database.appSettingsDao()) }
    val smsReader by lazy { SmsReader(this) }
    val notifier by lazy { DhanapalaNotifier(this) }
    val transactionAlertService by lazy { TransactionAlertService(transactionRepository, budgetRepository, notifier) }
}
