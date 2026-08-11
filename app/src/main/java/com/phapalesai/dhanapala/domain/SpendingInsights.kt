package com.phapalesai.dhanapala.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Pure statistical projections over a period's spending -- no persistence, no Android dependency. */
object SpendingInsights {

    /**
     * Projects total period spend assuming the daily average seen so far
     * continues for the rest of the period.
     */
    fun projectPeriodEndSpend(
        spentSoFar: Double,
        periodStart: LocalDate,
        periodEnd: LocalDate,
        today: LocalDate
    ): Double {
        val clampedToday = when {
            today.isAfter(periodEnd) -> periodEnd
            today.isBefore(periodStart) -> periodStart
            else -> today
        }
        val daysElapsed = (ChronoUnit.DAYS.between(periodStart, clampedToday).toInt() + 1).coerceAtLeast(1)
        val totalDays = (ChronoUnit.DAYS.between(periodStart, periodEnd).toInt() + 1).coerceAtLeast(1)
        val dailyAverage = spentSoFar / daysElapsed
        return dailyAverage * totalDays
    }

    /** Average spend per weekday day vs per weekend day, from a date -> total-spent-that-date map. */
    fun weekdayVsWeekendAverage(dailySpend: Map<LocalDate, Double>): WeekdayWeekendAverage {
        val weekday = dailySpend.filterKeys { !it.isWeekend() }
        val weekend = dailySpend.filterKeys { it.isWeekend() }
        return WeekdayWeekendAverage(
            weekdayAvg = weekday.values.average0(),
            weekendAvg = weekend.values.average0()
        )
    }

    private fun LocalDate.isWeekend(): Boolean = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY

    private fun Collection<Double>.average0(): Double = if (isEmpty()) 0.0 else sum() / size

    data class WeekdayWeekendAverage(val weekdayAvg: Double, val weekendAvg: Double)
}
