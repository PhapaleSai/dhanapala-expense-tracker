package com.phapalesai.dhanapala.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SpendingInsightsTest {

    @Test
    fun `projects month end spend from the average so far`() {
        // Rs 500 spent over 5 days elapsed (Rs 100/day) in a 30-day period -> Rs 3000 projected.
        val periodStart = LocalDate.of(2026, 8, 1)
        val periodEnd = LocalDate.of(2026, 8, 30)
        val today = LocalDate.of(2026, 8, 5)
        val projected = SpendingInsights.projectPeriodEndSpend(500.0, periodStart, periodEnd, today)
        assertEquals(3000.0, projected, 0.01)
    }

    @Test
    fun `clamps today to periodEnd when the period has already finished`() {
        val periodStart = LocalDate.of(2026, 8, 1)
        val periodEnd = LocalDate.of(2026, 8, 10)
        val today = LocalDate.of(2026, 8, 20)
        // 10 days elapsed (clamped), Rs 1000 spent -> Rs 100/day * 10 days = Rs 1000.
        val projected = SpendingInsights.projectPeriodEndSpend(1000.0, periodStart, periodEnd, today)
        assertEquals(1000.0, projected, 0.01)
    }

    @Test
    fun `weekday vs weekend average splits correctly`() {
        // Mon 3 Aug 2026, Tue 4 Aug -> weekdays. Sat 1 Aug, Sun 2 Aug -> weekend.
        val dailySpend = mapOf(
            LocalDate.of(2026, 8, 1) to 200.0, // Saturday
            LocalDate.of(2026, 8, 2) to 400.0, // Sunday
            LocalDate.of(2026, 8, 3) to 100.0, // Monday
            LocalDate.of(2026, 8, 4) to 300.0  // Tuesday
        )
        val result = SpendingInsights.weekdayVsWeekendAverage(dailySpend)
        assertEquals(200.0, result.weekdayAvg, 0.01)
        assertEquals(300.0, result.weekendAvg, 0.01)
    }

    @Test
    fun `empty maps produce zero averages instead of dividing by zero`() {
        val result = SpendingInsights.weekdayVsWeekendAverage(emptyMap())
        assertEquals(0.0, result.weekdayAvg, 0.01)
        assertEquals(0.0, result.weekendAvg, 0.01)
    }
}
