package at.rueckgr.kotlin.rocketbot.util.time

import org.apache.commons.lang3.StringUtils
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class DateTimeDifferenceCalculator {
    private val NOW_MARGIN = 5L

    enum class TimeUnit(val plusFunction: (LocalDateTime, Long) -> LocalDateTime, val singular: String, val plural: String) {
        YEAR(LocalDateTime::plusYears, "year", "years"),
        MONTH(LocalDateTime::plusMonths, "month", "months"),
        DAY(LocalDateTime::plusDays, "day", "days"),
        HOUR(LocalDateTime::plusHours, "hour", "hours"),
        MINUTE(LocalDateTime::plusMinutes, "minute", "minutes"),
        SECOND(LocalDateTime::plusSeconds, "second", "seconds")
    }

    fun formatTimeDifference(from: LocalDateTime, to: LocalDateTime, ignoredTimeUnits: List<TimeUnit> = emptyList()): String {
        if (abs(ChronoUnit.SECONDS.between(from, to)) < NOW_MARGIN) {
            return "now"
        }

        val (startDate, endDate, suffix) = when (from.isAfter(to)) {
            false -> Triple(from, to, "from now")
            true -> Triple(to, from, "ago")
        }

        val filteredTimeUnits = TimeUnit.values().filter { !ignoredTimeUnits.contains(it) }
        val period = filteredTimeUnits
            .associateWith { 0 }
            .toMutableMap()
        var localDateTime = startDate
        filteredTimeUnits.forEach {
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

        val evenMorePrettyDifference = when (StringUtils.countMatches(prettyTimeDifference, ",")) {
            0 -> prettyTimeDifference
            1 -> prettyTimeDifference.replace(",", " and")
            else -> replaceLast(prettyTimeDifference, ", ", ", and ")
        }

        return "$evenMorePrettyDifference $suffix"
    }

    private fun replaceLast(text: String, toReplace: String, replaceWith: String): String {
        return when (val pos = text.lastIndexOf(toReplace)) {
            -1 -> text
            else -> text.substring(0, pos) + replaceWith + text.substring(pos + toReplace.length)
        }
    }
}
