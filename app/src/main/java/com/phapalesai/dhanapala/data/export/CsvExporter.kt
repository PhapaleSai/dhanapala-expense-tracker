package com.phapalesai.dhanapala.data.export

import com.phapalesai.dhanapala.data.local.TransactionEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Local-only CSV export — never uploaded anywhere, only handed to Android's share sheet. */
object CsvExporter {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun toCsv(transactions: List<TransactionEntity>): String {
        val header = "Date,Type,Amount,Category,Merchant,Description"
        val rows = transactions.map { tx ->
            val date = Instant.ofEpochMilli(tx.dateMillis).atZone(ZoneId.systemDefault()).format(dateFormatter)
            listOf(
                date,
                tx.type.name,
                tx.amount.toString(),
                tx.category,
                tx.merchant.orEmpty(),
                tx.description.orEmpty()
            ).joinToString(",") { escape(it) }
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    private fun escape(value: String): String =
        if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
}
