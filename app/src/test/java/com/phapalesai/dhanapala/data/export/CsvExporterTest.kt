package com.phapalesai.dhanapala.data.export

import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {

    private fun tx(
        amount: Double,
        type: TransactionType,
        category: String,
        merchant: String? = null,
        description: String? = null
    ) = TransactionEntity(
        amount = amount,
        type = type,
        dateMillis = 1_754_784_000_000, // 2025-08-10 UTC-ish
        sender = "AX-BANK",
        merchant = merchant,
        description = description,
        sourceSmsId = null,
        dedupeHash = "$amount-$type-$category",
        category = category,
        isManual = false,
        createdAt = 0
    )

    @Test
    fun `header row is always present`() {
        val csv = CsvExporter.toCsv(emptyList())
        assertEquals("Date,Type,Amount,Category,Merchant,Description", csv.trim())
    }

    @Test
    fun `one row per transaction with expected fields`() {
        val csv = CsvExporter.toCsv(listOf(tx(450.0, TransactionType.DEBIT, "Food", "Swiggy", "Lunch order")))
        val lines = csv.lines()
        assertEquals(2, lines.size)
        assertTrue(lines[1].contains("DEBIT"))
        assertTrue(lines[1].contains("450.0"))
        assertTrue(lines[1].contains("Food"))
        assertTrue(lines[1].contains("Swiggy"))
    }

    @Test
    fun `fields containing commas are quoted and escaped`() {
        val csv = CsvExporter.toCsv(
            listOf(tx(100.0, TransactionType.DEBIT, "Other", description = "Coffee, tea and snacks"))
        )
        assertTrue(csv.contains("\"Coffee, tea and snacks\""))
    }
}
