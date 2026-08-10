package com.phapalesai.dhanapala.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormat {
    private val format: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }

    fun rupees(amount: Double): String = format.format(amount)
}
