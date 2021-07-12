package at.rueckgr.kotlin.rocketbot

import java.time.LocalDateTime

class DateTimeDifferenceCalculator {
    enum class TimeUnit(val plusFunction: (LocalDateTime, Long) -> LocalDateTime, val singular: String, val plural: String) {
        YEAR(LocalDateTime::plusDays, "year", "years"),
        MONTH(LocalDateTime::plusMonths, "month", "months"),
        DAY(LocalDateTime::plusDays, "day", "days"),
        HOUR(LocalDateTime::plusHours, "hour", "hours"),
        MINUTE(LocalDateTime::plusMinutes, "minute", "minutes"),
        SECOND(LocalDateTime::plusSeconds, "second", "seconds")
    }

    fun calculateTimeDifference(from: LocalDateTime, to: LocalDateTime): Map<TimeUnit, Int> {
        if(from.isAfter(to)) {
            // TODO
            return emptyMap()
        }

        val result = TimeUnit.values().associate { it to 0 }.toMutableMap()
        var localDateTime = from
        TimeUnit.values().forEach {
            while(true) {
                val newDate = it.plusFunction.invoke(localDateTime, 1)
                if (newDate.isAfter(to)) {
                    break
                }
                result[it] = result[it]!!.plus(1)
                localDateTime = newDate
            }
        }
        return result
    }

    fun createPrettyTimeDifference(calculatedDifference: Map<TimeUnit, Int>): String {
        return calculatedDifference
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
    }
}
