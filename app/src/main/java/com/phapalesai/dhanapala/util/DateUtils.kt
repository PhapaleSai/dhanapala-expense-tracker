package com.phapalesai.dhanapala.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

object DateUtils {
    fun monthRangeMillis(month: YearMonth, zone: ZoneId = ZoneId.systemDefault()): LongRange {
        val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return start..end
    }

    fun dayRangeMillis(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): LongRange {
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return start..end
    }
}
