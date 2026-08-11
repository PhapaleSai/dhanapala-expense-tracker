package com.phapalesai.dhanapala.domain

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MoneyJokesTest {

    @Test
    fun `each language returns a non-blank joke`() {
        RoastLanguage.entries.forEach { language ->
            assertTrue(MoneyJokes.random(language).isNotBlank())
        }
    }

    @Test
    fun `same seed returns the same joke every time`() {
        val first = MoneyJokes.random(RoastLanguage.EN, Random(7))
        val second = MoneyJokes.random(RoastLanguage.EN, Random(7))
        assertTrue(first == second)
    }

    @Test
    fun `no stray non-ascii characters in any joke pool`() {
        RoastLanguage.entries.forEach { language ->
            repeat(50) { seed ->
                val joke = MoneyJokes.random(language, Random(seed))
                assertTrue(joke.all { it.code in 0..127 || it == '—' })
            }
        }
    }
}
