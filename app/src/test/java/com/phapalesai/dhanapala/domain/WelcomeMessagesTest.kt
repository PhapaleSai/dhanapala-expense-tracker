package com.phapalesai.dhanapala.domain

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class WelcomeMessagesTest {

    @Test
    fun `each language returns a non-blank message`() {
        RoastLanguage.entries.forEach { language ->
            assertTrue(WelcomeMessages.random(language).isNotBlank())
        }
    }

    @Test
    fun `same seed returns the same message every time`() {
        val first = WelcomeMessages.random(RoastLanguage.EN, Random(3))
        val second = WelcomeMessages.random(RoastLanguage.EN, Random(3))
        assertTrue(first == second)
    }
}
