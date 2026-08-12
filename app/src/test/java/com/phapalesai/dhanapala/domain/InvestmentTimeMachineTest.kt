package com.phapalesai.dhanapala.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class InvestmentTimeMachineTest {

    @Test
    fun `same day returns the principal unchanged`() {
        val today = LocalDate.of(2026, 8, 12)
        val value = InvestmentTimeMachine.futureValue(1000.0, fromDate = today, toDate = today)
        assertEquals(1000.0, value, 0.01)
    }

    @Test
    fun `one year at 12 percent grows by roughly 12 percent`() {
        val from = LocalDate.of(2025, 8, 12)
        val to = LocalDate.of(2026, 8, 12)
        val value = InvestmentTimeMachine.futureValue(1000.0, fromDate = from, toDate = to, annualRatePercent = 12.0)
        assertEquals(1120.0, value, 5.0)
    }

    @Test
    fun `higher rate yields a higher future value for the same period`() {
        val from = LocalDate.of(2024, 1, 1)
        val to = LocalDate.of(2026, 1, 1)
        val low = InvestmentTimeMachine.futureValue(500.0, from, to, annualRatePercent = 6.0)
        val high = InvestmentTimeMachine.futureValue(500.0, from, to, annualRatePercent = 15.0)
        assert(high > low)
    }
}
