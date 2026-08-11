package com.phapalesai.dhanapala.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SplitCalculatorTest {

    @Test
    fun `splits evenly when it divides cleanly`() {
        val shares = SplitCalculator.splitEqually(300.0, 3)
        assertEquals(listOf(100.0, 100.0, 100.0), shares)
    }

    @Test
    fun `remainder paisa goes to the first few people so shares sum back to the total`() {
        val shares = SplitCalculator.splitEqually(100.0, 3)
        assertEquals(listOf(33.34, 33.33, 33.33), shares)
        assertEquals(100.0, shares.sum(), 0.001)
    }

    @Test
    fun `zero people returns an empty list`() {
        assertEquals(emptyList<Double>(), SplitCalculator.splitEqually(500.0, 0))
    }
}
