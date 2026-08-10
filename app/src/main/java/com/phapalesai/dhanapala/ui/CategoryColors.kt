package com.phapalesai.dhanapala.ui

import androidx.compose.ui.graphics.Color
import com.phapalesai.dhanapala.data.local.Category

/** Distinct accent color per category, used in charts/bars so each category reads at a glance. */
fun categoryColor(category: String): Color = when (category) {
    Category.FOOD -> Color(0xFFFF8A65)
    Category.FUEL -> Color(0xFFFFC857)
    Category.SHOPPING -> Color(0xFFBA68C8)
    Category.BILLS -> Color(0xFF4FC3F7)
    Category.ENTERTAINMENT -> Color(0xFFF06292)
    Category.TRAVEL -> Color(0xFF4DD0E1)
    Category.UPI -> Color(0xFF00E5A0)
    Category.CASH_WITHDRAWAL -> Color(0xFFA1887F)
    Category.SALARY -> Color(0xFF66BB6A)
    Category.REFUND -> Color(0xFF81C784)
    Category.TRANSFER -> Color(0xFF7986CB)
    Category.OTHER -> Color(0xFF90A4AE)
    else -> Color(0xFF757575)
}
