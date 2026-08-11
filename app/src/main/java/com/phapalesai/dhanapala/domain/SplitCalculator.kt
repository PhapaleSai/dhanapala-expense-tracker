package com.phapalesai.dhanapala.domain

/** Pure bill-splitting math -- no persistence, no Android dependency. */
object SplitCalculator {

    /**
     * Splits [totalAmount] evenly among [peopleCount] people to the paisa,
     * assigning the leftover paisa (from rounding) to the first few people
     * so the shares always sum back to exactly [totalAmount].
     */
    fun splitEqually(totalAmount: Double, peopleCount: Int): List<Double> {
        if (peopleCount <= 0) return emptyList()
        val totalPaise = Math.round(totalAmount * 100)
        val basePaise = totalPaise / peopleCount
        val remainder = (totalPaise - basePaise * peopleCount).toInt()
        return (0 until peopleCount).map { index ->
            val paise = basePaise + if (index < remainder) 1 else 0
            paise / 100.0
        }
    }
}
