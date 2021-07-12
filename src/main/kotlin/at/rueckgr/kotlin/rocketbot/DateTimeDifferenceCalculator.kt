package at.rueckgr.kotlin.rocketbot

import java.time.LocalDateTime

class DateTimeDifferenceCalculator {
    enum class TimeUnit(val plusFunction: (LocalDateTime, Long) -> LocalDateTime, val singular: String, val plural: String) {
        YEAR(LocalDateTime::plusYears, "year", "years"),
        MONTH(LocalDateTime::plusMonths, "month", "months"),
        DAY(LocalDateTime::plusDays, "day", "days"),
        HOUR(LocalDateTime::plusHours, "hour", "hours"),
        MINUTE(LocalDateTime::plusMinutes, "minute", "minutes"),
        SECOND(LocalDateTime::plusSeconds, "second", "seconds")
    }

    fun formatTimeDifference(from: LocalDateTime, to: LocalDateTime): String {
        val (startDate, endDate, suffix) = when (from.isAfter(to)) {
            false -> Triple(from, to, "from now")
            true -> Triple(to, from, "ago")
        }

        val period = TimeUnit.values().associate { it to 0 }.toMutableMap()
        var localDateTime = startDate
        TimeUnit.values().forEach {
            while(true) {
                val newDate = it.plusFunction.invoke(localDateTime, 1)
                if (newDate.isAfter(endDate)) {
                    break
                }
                period[it] = period[it]!!.plus(1)
                localDateTime = newDate
            }
        }

        val prettyTimeDifference = period
            .filter { it.value > 0 }
            .map {
                val count = it.value
                val label = when (count) {
                    1 -> it.key.singular
                    else -> it.key.plural
                }
                "$count $label"
            }
            .joinToString(", ")

        return "$prettyTimeDifference $suffix"
    }
}
