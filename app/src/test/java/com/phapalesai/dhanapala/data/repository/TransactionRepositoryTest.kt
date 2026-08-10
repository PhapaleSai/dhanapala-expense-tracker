package com.phapalesai.dhanapala.data.repository

import com.phapalesai.dhanapala.data.sms.RawSms
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionRepositoryTest {

    private fun sms(id: String, body: String, address: String = "AX-BANK", date: Long = 1_700_000_000_000) =
        RawSms(id = id, address = address, body = body, dateMillis = date)

    @Test
    fun `scanning saves debit and credit transactions and skips non-financial sms`() = runBlocking {
        val repo = TransactionRepository(FakeTransactionDao())
        val messages = listOf(
            sms("1", "Rs. 500 debited from A/c XX1234 for UPI transaction."),
            sms("2", "Rs. 2,000 credited to your account as SALARY."),
            sms("3", "Your OTP for login is 123456. Do not share.")
        )

        val result = repo.scanMessages(messages)

        assertEquals(3, result.scanned)
        assertEquals(2, result.inserted)
        assertEquals(0, result.duplicates)
        assertEquals(1, result.notTransactions)
    }

    @Test
    fun `scanning the same sms twice never creates a duplicate transaction`() = runBlocking {
        val repo = TransactionRepository(FakeTransactionDao())
        val messages = listOf(sms("1", "Rs. 500 debited from A/c XX1234 for UPI transaction."))

        val first = repo.scanMessages(messages)
        val second = repo.scanMessages(messages)

        assertEquals(1, first.inserted)
        assertEquals(0, first.duplicates)
        assertEquals(0, second.inserted)
        assertEquals(1, second.duplicates)
    }

    @Test
    fun `rescanning an inbox that grew only inserts the new messages`() = runBlocking {
        val repo = TransactionRepository(FakeTransactionDao())
        val original = listOf(sms("1", "Rs. 500 debited from A/c XX1234 for UPI transaction."))
        repo.scanMessages(original)

        val grownInbox = original + sms("2", "Rs. 300 debited from A/c XX1234 for POS purchase.")
        val result = repo.scanMessages(grownInbox)

        assertEquals(1, result.inserted)
        assertEquals(1, result.duplicates)
    }

    @Test
    fun `rescanning recategorizes existing transactions with the current keyword dictionary`() = runBlocking {
        val dao = FakeTransactionDao()
        val repo = TransactionRepository(dao)
        val staleFoodOrder = com.phapalesai.dhanapala.data.local.TransactionEntity(
            amount = 250.0,
            type = com.phapalesai.dhanapala.data.local.TransactionType.DEBIT,
            dateMillis = 1_700_000_000_000,
            sender = "AX-BANK",
            merchant = null,
            description = "Rs. 250 debited for SWIGGY order via UPI.",
            sourceSmsId = "99",
            dedupeHash = "stale-1",
            category = "Anonymous Expenses",
            isManual = false,
            createdAt = 0
        )
        dao.insert(staleFoodOrder)

        repo.scanMessages(emptyList())

        val recategorized = dao.getAll().first { it.dedupeHash == "stale-1" }
        assertEquals("Food", recategorized.category)
    }

    @Test
    fun `manual entries are never recategorized`() = runBlocking {
        val dao = FakeTransactionDao()
        val repo = TransactionRepository(dao)
        val manualEntry = com.phapalesai.dhanapala.data.local.TransactionEntity(
            amount = 100.0,
            type = com.phapalesai.dhanapala.data.local.TransactionType.DEBIT,
            dateMillis = 1_700_000_000_000,
            sender = null,
            merchant = null,
            description = "swiggy cash order",
            sourceSmsId = null,
            dedupeHash = "manual-1",
            category = "Entertainment",
            isManual = true,
            createdAt = 0
        )
        dao.insert(manualEntry)

        repo.scanMessages(emptyList())

        val stillManual = dao.getAll().first { it.dedupeHash == "manual-1" }
        assertEquals("Entertainment", stillManual.category)
    }
}
