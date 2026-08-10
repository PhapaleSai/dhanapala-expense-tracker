package com.phapalesai.dhanapala.util

import java.time.LocalTime

object Greeting {
    fun forTime(hour: Int = LocalTime.now().hour): String = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else -> "Good night"
    }
}
