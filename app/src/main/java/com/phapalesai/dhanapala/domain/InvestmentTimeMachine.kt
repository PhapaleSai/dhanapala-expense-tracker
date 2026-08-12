package com.phapalesai.dhanapala.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.pow

/** Pure compound-growth math for the "what if you'd invested it instead" screen. No real market data — just a what-if. */
object InvestmentTimeMachine {

    const val DEFAULT_ANNUAL_RATE_PERCENT = 12.0

    fun futureValue(
        principal: Double,
        fromDate: LocalDate,
        toDate: LocalDate = LocalDate.now(),
        annualRatePercent: Double = DEFAULT_ANNUAL_RATE_PERCENT
    ): Double {
        val days = ChronoUnit.DAYS.between(fromDate, toDate)
        if (days <= 0) return principal
        val years = days / 365.25
        return principal * (1 + annualRatePercent / 100.0).pow(years)
    }
}
