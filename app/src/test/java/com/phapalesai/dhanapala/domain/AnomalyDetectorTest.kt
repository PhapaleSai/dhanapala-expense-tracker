package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnomalyDetectorTest {

    private fun tx(id: Long, amount: Double, category: String = "Food") = TransactionEntity(
        id = id,
        amount = amount,
        type = TransactionType.DEBIT,
        dateMillis = id,
        sender = "TEST-BANK",
        merchant = null,
        description = "test",
        sourceSmsId = "id-$id",
        dedupeHash = "hash-$id",
        category = category,
        isManual = false,
        createdAt = System.currentTimeMillis()
    )

    @Test
    fun `a transaction far above the category average is flagged`() {
        val history = listOf(tx(1, 200.0), tx(2, 210.0), tx(3, 190.0))
        val spike = tx(4, 2000.0)
        val result = AnomalyDetector.detect(listOf(spike), history + spike)
        assertEquals(1, result.size)
        assertEquals(spike.id, result[0].transaction.id)
    }

    @Test
    fun `a normal transaction near the average is not flagged`() {
        val history = listOf(tx(1, 200.0), tx(2, 210.0), tx(3, 190.0))
        val normal = tx(4, 220.0)
        val result = AnomalyDetector.detect(listOf(normal), history + normal)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `a brand-new category is never flagged regardless of amount`() {
        val onlyOne = tx(1, 50_000.0, category = "Travel")
        val result = AnomalyDetector.detect(listOf(onlyOne), listOf(onlyOne))
        assertTrue(result.isEmpty())
    }
}
