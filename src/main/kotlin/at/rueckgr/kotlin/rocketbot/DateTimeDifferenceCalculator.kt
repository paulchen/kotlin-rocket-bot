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

    fun calculateTimeDifference(from: LocalDateTime, to: LocalDateTime): Map<TimeUnit, Int> {
        val (inverse, startDate, endDate) = when (from.isAfter(to)) {
            false -> Triple(false, from, to)
            true -> Triple(true, to, from)
        }

        val result = TimeUnit.values().associate { it to 0 }.toMutableMap()
        var localDateTime = startDate
        TimeUnit.values().forEach {
            while(true) {
                val newDate = it.plusFunction.invoke(localDateTime, 1)
                if (newDate.isAfter(endDate)) {
                    break
                }
                result[it] = result[it]!!.plus(1)
                localDateTime = newDate
            }
        }

        return when (inverse) {
            false -> result
            true -> result.mapValues { (_, v) -> -v }
        }
    }

    fun formatTimeDifference(calculatedDifference: Map<TimeUnit, Int>): String {
        val (period, suffix) = when(calculatedDifference.any { (_, v) -> v < 0 }) {
            true -> Pair(calculatedDifference.mapValues { (_, v) -> -v }, "ago")
            false -> Pair(calculatedDifference, "from now")
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
