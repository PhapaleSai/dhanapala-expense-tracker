package com.phapalesai.dhanapala.data.parser

import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.data.sms.RawSms
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmsTransactionParserTest {

    private val parser = SmsTransactionParser()

    private fun sms(id: String = "1", body: String, address: String = "AX-BANK") =
        RawSms(id = id, address = address, body = body, dateMillis = 1_700_000_000_000)

    @Test
    fun `debited amount with rupee symbol`() {
        val result = parser.parse(sms(body = "Rs. 450.00 debited from A/c XX1234 for UPI transaction."))
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals(450.0, result?.amount)
    }

    @Test
    fun `debited amount with comma separated thousands`() {
        val result = parser.parse(sms(body = "Rs. 1,250 debited from your account."))
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals(1250.0, result?.amount)
    }

    @Test
    fun `credited amount with Rs dot`() {
        val result = parser.parse(sms(body = "Rs. 500 credited to your account."))
        assertEquals(TransactionType.CREDIT, result?.type)
        assertEquals(500.0, result?.amount)
    }

    @Test
    fun `credited amount with INR and comma decimal`() {
        val result = parser.parse(sms(body = "INR 2,500.50 credited to your account XXXX1234."))
        assertEquals(TransactionType.CREDIT, result?.type)
        assertEquals(2500.50, result?.amount)
    }

    @Test
    fun `upi debit is classified as debit not credit just because it mentions UPI`() {
        val result = parser.parse(sms(body = "Rs 200.00 debited via UPI Ref No 123456789."))
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals(200.0, result?.amount)
    }

    @Test
    fun `atm withdrawal`() {
        val result = parser.parse(sms(body = "Rs 2,000 withdrawn from ATM using Card XX1234."))
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals(2000.0, result?.amount)
    }

    @Test
    fun `salary credit`() {
        val result = parser.parse(sms(body = "Rs 45,000.00 credited to A/c XX1234 as SALARY for AUG 2026."))
        assertEquals(TransactionType.CREDIT, result?.type)
        assertEquals(45000.0, result?.amount)
    }

    @Test
    fun `refund is a credit`() {
        val result = parser.parse(sms(body = "Rs 350 refunded to your account for order #123."))
        assertEquals(TransactionType.CREDIT, result?.type)
        assertEquals(350.0, result?.amount)
    }

    @Test
    fun `credit via upi still classified as credit`() {
        val result = parser.parse(sms(body = "Rs 500 credited to your a/c via UPI from John Doe."))
        assertEquals(TransactionType.CREDIT, result?.type)
    }

    @Test
    fun `non-financial SMS is not a transaction`() {
        val result = parser.parse(sms(body = "Your OTP for login is 123456. Do not share this with anyone."))
        assertNull(result)
    }

    @Test
    fun `ordinary message mentioning payment without transaction keywords is not classified`() {
        val result = parser.parse(sms(body = "Reminder: your payment of Rs 500 is due on 15th Aug."))
        assertNull(result)
    }

    @Test
    fun `duplicate sms produces the same dedupe hash`() {
        val a = parser.parse(sms(id = "42", body = "Rs. 500 debited from A/c XX1234 for UPI transaction."))
        val b = parser.parse(sms(id = "42", body = "Rs. 500 debited from A/c XX1234 for UPI transaction."))
        assertEquals(a?.dedupeHash, b?.dedupeHash)
    }

    @Test
    fun `different sms produce different dedupe hashes`() {
        val a = parser.parse(sms(id = "1", body = "Rs. 500 debited from A/c XX1234 for UPI transaction."))
        val b = parser.parse(sms(id = "2", body = "Rs. 500 debited from A/c XX9999 for UPI transaction."))
        assert(a?.dedupeHash != b?.dedupeHash)
    }

    @Test
    fun `real JSBL bank format with no space after Rs and opaque VPA parses as debit`() {
        val result = parser.parse(
            sms(
                body = "A/c no. XX9476 is debited for Rs.5.00 on 09-08-2026 22:10:24 and CR to VPA " +
                    "q345368832@ybl (UPI Ref no 127660747053) If not done by you, call 02024404521/22/23. JSBLPune"
            )
        )
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals(5.0, result?.amount)
        // A random-generated VPA (no brand name) has no signal to categorize
        // beyond the generic UPI bucket — CategoryGuesser is tested separately.
    }
}
