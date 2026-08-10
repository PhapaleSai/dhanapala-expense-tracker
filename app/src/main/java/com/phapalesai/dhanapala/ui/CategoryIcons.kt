package com.phapalesai.dhanapala.ui

import com.phapalesai.dhanapala.data.local.Category

fun categoryEmoji(category: String): String = when (category) {
    Category.FOOD -> "🍔"
    Category.FUEL -> "⛽"
    Category.SHOPPING -> "🛍️"
    Category.BILLS -> "🧾"
    Category.ENTERTAINMENT -> "🎬"
    Category.TRAVEL -> "✈️"
    Category.UPI -> "📲"
    Category.CASH_WITHDRAWAL -> "🏧"
    Category.SALARY -> "💰"
    Category.REFUND -> "↩️"
    Category.TRANSFER -> "🔁"
    Category.OTHER -> "💳"
    else -> "❓"
}
