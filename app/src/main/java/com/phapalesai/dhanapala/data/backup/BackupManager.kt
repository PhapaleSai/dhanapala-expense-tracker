package com.phapalesai.dhanapala.data.backup

import com.phapalesai.dhanapala.data.local.AccountNicknameEntity
import com.phapalesai.dhanapala.data.local.AppSettingsEntity
import com.phapalesai.dhanapala.data.local.BudgetEntity
import com.phapalesai.dhanapala.data.local.CategoryBudgetEntity
import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.data.repository.AccountNicknameRepository
import com.phapalesai.dhanapala.data.repository.BudgetRepository
import com.phapalesai.dhanapala.data.repository.CategoryBudgetRepository
import com.phapalesai.dhanapala.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private const val BACKUP_FORMAT_VERSION = 1

/**
 * Snapshots every table to JSON (for encryption+export) and restores from
 * that same JSON (full replace, not a merge -- the caller is expected to
 * confirm this with the user first, same as "Reset all data").
 */
class BackupManager(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
    private val categoryBudgetRepo: CategoryBudgetRepository,
    private val accountNicknameRepo: AccountNicknameRepository
) {
    suspend fun buildBackupJson(): String {
        val root = JSONObject()
        root.put("version", BACKUP_FORMAT_VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        root.put("transactions", JSONArray(transactionRepo.getAllOnce().map { it.toJson() }))
        root.put("budgets", JSONArray(budgetRepo.getAllBudgetsOnce().map { it.toJson() }))
        root.put("categoryBudgets", JSONArray(categoryBudgetRepo.getAllOnce().map { it.toJson() }))
        root.put("accountNicknames", JSONArray(accountNicknameRepo.getAllOnce().map { it.toJson() }))
        root.put("settings", budgetRepo.observeSettings().first().toJson())

        return root.toString()
    }

    suspend fun restoreFromJson(json: String) {
        val root = JSONObject(json)

        val transactions = root.getJSONArray("transactions").let { array ->
            (0 until array.length()).map { transactionFromJson(array.getJSONObject(it)) }
        }
        val budgets = root.getJSONArray("budgets").let { array ->
            (0 until array.length()).map { budgetFromJson(array.getJSONObject(it)) }
        }
        val categoryBudgets = root.getJSONArray("categoryBudgets").let { array ->
            (0 until array.length()).map { categoryBudgetFromJson(array.getJSONObject(it)) }
        }
        val accountNicknames = root.getJSONArray("accountNicknames").let { array ->
            (0 until array.length()).map { accountNicknameFromJson(array.getJSONObject(it)) }
        }

        transactionRepo.replaceAll(transactions)
        budgetRepo.replaceAllBudgets(budgets)
        categoryBudgetRepo.replaceAll(categoryBudgets)
        accountNicknameRepo.replaceAll(accountNicknames)
        if (root.has("settings")) {
            budgetRepo.updateSettings(settingsFromJson(root.getJSONObject("settings")))
        }
    }

    private fun TransactionEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("amount", amount)
        put("type", type.name)
        put("dateMillis", dateMillis)
        put("sender", sender ?: JSONObject.NULL)
        put("merchant", merchant ?: JSONObject.NULL)
        put("description", description ?: JSONObject.NULL)
        put("sourceSmsId", sourceSmsId ?: JSONObject.NULL)
        put("dedupeHash", dedupeHash)
        put("category", category)
        put("isManual", isManual)
        put("createdAt", createdAt)
        put("tags", tags ?: JSONObject.NULL)
        put("receiptPhotoPath", receiptPhotoPath ?: JSONObject.NULL)
    }

    private fun transactionFromJson(o: JSONObject) = TransactionEntity(
        id = o.getLong("id"),
        amount = o.getDouble("amount"),
        type = TransactionType.valueOf(o.getString("type")),
        dateMillis = o.getLong("dateMillis"),
        sender = o.optStringOrNull("sender"),
        merchant = o.optStringOrNull("merchant"),
        description = o.optStringOrNull("description"),
        sourceSmsId = o.optStringOrNull("sourceSmsId"),
        dedupeHash = o.getString("dedupeHash"),
        category = o.getString("category"),
        isManual = o.getBoolean("isManual"),
        createdAt = o.getLong("createdAt"),
        tags = o.optStringOrNull("tags"),
        receiptPhotoPath = o.optStringOrNull("receiptPhotoPath")
    )

    private fun BudgetEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("startDateMillis", startDateMillis)
        put("endDateMillis", endDateMillis)
        put("amount", amount)
        put("notified80", notified80)
        put("notifiedExceeded", notifiedExceeded)
    }

    private fun budgetFromJson(o: JSONObject) = BudgetEntity(
        id = o.getLong("id"),
        startDateMillis = o.getLong("startDateMillis"),
        endDateMillis = o.getLong("endDateMillis"),
        amount = o.getDouble("amount"),
        notified80 = o.getBoolean("notified80"),
        notifiedExceeded = o.getBoolean("notifiedExceeded")
    )

    private fun CategoryBudgetEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("budgetId", budgetId)
        put("category", category)
        put("amount", amount)
        put("notifiedExceeded", notifiedExceeded)
    }

    private fun categoryBudgetFromJson(o: JSONObject) = CategoryBudgetEntity(
        id = o.getLong("id"),
        budgetId = o.getLong("budgetId"),
        category = o.getString("category"),
        amount = o.getDouble("amount"),
        notifiedExceeded = o.getBoolean("notifiedExceeded")
    )

    private fun AccountNicknameEntity.toJson() = JSONObject().apply {
        put("senderPattern", senderPattern)
        put("displayName", displayName)
    }

    private fun accountNicknameFromJson(o: JSONObject) = AccountNicknameEntity(
        senderPattern = o.getString("senderPattern"),
        displayName = o.getString("displayName")
    )

    private fun AppSettingsEntity.toJson() = JSONObject().apply {
        put("largeExpenseThreshold", largeExpenseThreshold)
        put("bhaiModeEnabled", bhaiModeEnabled)
        put("notificationsEnabled", notificationsEnabled)
        put("dailyReminderEnabled", dailyReminderEnabled)
        put("roastLevel", roastLevel)
        put("roastLanguage", roastLanguage)
        put("userName", userName)
        put("biometricLockEnabled", biometricLockEnabled)
    }

    private fun settingsFromJson(o: JSONObject) = AppSettingsEntity(
        largeExpenseThreshold = o.getDouble("largeExpenseThreshold"),
        bhaiModeEnabled = o.getBoolean("bhaiModeEnabled"),
        notificationsEnabled = o.getBoolean("notificationsEnabled"),
        dailyReminderEnabled = o.getBoolean("dailyReminderEnabled"),
        roastLevel = o.getString("roastLevel"),
        roastLanguage = o.getString("roastLanguage"),
        userName = o.getString("userName"),
        biometricLockEnabled = o.optBoolean("biometricLockEnabled", false)
    )

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)
}
