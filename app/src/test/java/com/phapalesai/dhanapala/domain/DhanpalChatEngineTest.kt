package com.phapalesai.dhanapala.domain

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DhanpalChatEngineTest {

    @Test
    fun `affordable amount gets a can-afford style reply`() {
        val reply = DhanpalChatEngine.respond("can I afford 500 rupees shoes?", remaining = 1000.0, RoastLanguage.EN, Random(1))
        assertTrue(reply.contains("afford") || reply.contains("Go for it") || reply.contains("Technically"))
    }

    @Test
    fun `unaffordable amount gets a cannot-afford style reply`() {
        val reply = DhanpalChatEngine.respond("can I afford 5000 rupees shoes?", remaining = 1000.0, RoastLanguage.EN, Random(1))
        assertTrue(reply.contains("no") || reply.contains("No") || reply.contains("short") || reply.contains("not"))
    }

    @Test
    fun `no amount in the message gets a generic nudge`() {
        val reply = DhanpalChatEngine.respond("hello there", remaining = 1000.0, RoastLanguage.EN, Random(1))
        assertTrue(reply.contains("amount") || reply.contains("number"))
    }

    @Test
    fun `amount with k suffix is parsed as thousands`() {
        // 2k shoes against a 1500 remaining budget should be flagged unaffordable.
        val reply = DhanpalChatEngine.respond("can I afford 2k shoes", remaining = 1500.0, RoastLanguage.EN, Random(1))
        assertTrue(reply.contains("no") || reply.contains("No") || reply.contains("short") || reply.contains("not"))
    }

    @Test
    fun `1 lakh is parsed as one hundred thousand, not one`() {
        // Regression test: "1 lakh" must resolve to 100000, not the literal digit "1".
        val reply = DhanpalChatEngine.respond("can I afford 1 lakh bike", remaining = 50_000.0, RoastLanguage.EN, Random(1))
        assertTrue(reply.contains("no") || reply.contains("No") || reply.contains("short") || reply.contains("not"))
    }

    @Test
    fun `affordable lakh amount is recognized as affordable`() {
        val reply = DhanpalChatEngine.respond("can I afford 1 lakh bike", remaining = 200_000.0, RoastLanguage.EN, Random(1))
        assertTrue(reply.contains("afford") || reply.contains("Go for it") || reply.contains("Technically"))
    }

    @Test
    fun `crore amount is parsed as ten million`() {
        val reply = DhanpalChatEngine.respond("can I afford a 1 crore house", remaining = 500_000.0, RoastLanguage.EN, Random(1))
        assertTrue(reply.contains("no") || reply.contains("No") || reply.contains("short") || reply.contains("not"))
    }
}
