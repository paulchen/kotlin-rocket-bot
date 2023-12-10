package at.rueckgr.kotlin.rocketbot.util.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

enum class TimeUnit(val plusFunction: (LocalDateTime, Long) -> LocalDateTime, val singular: String, val plural: String, val abbreviation: String) {
    YEAR(LocalDateTime::plusYears, "year", "years", "y"),
    MONTH(LocalDateTime::plusMonths, "month", "months", "mon"),
    WEEK(LocalDateTime::plusWeeks, "week", "weeks", "w"),
    DAY(LocalDateTime::plusDays, "day", "days", "d"),
    HOUR(LocalDateTime::plusHours, "hour", "hours", "h"),
    MINUTE(LocalDateTime::plusMinutes, "minute", "minutes", "min"),
    SECOND(LocalDateTime::plusSeconds, "second", "seconds", "s")
}

enum class DateTimeFormat(
    val regex: Regex,
    val pattern: String,
    val function: (DateTimeParser, String, String, LocalDateTime) -> LocalDateTime
) {
    DATE_EN("""^[0-9]{4}-[0-9]{2}-[0-9]{2}$""".toRegex(), "yyyy-MM-dd", DateTimeParser::parseDay),
    DATE_DE("""^[0-9]{1,2}\.[0-9]{1,2}\.[0-9]{4}$""".toRegex(), "d.M.yyyy", DateTimeParser::parseDay),
    DATE_DE_WITHOUT_YEAR("""^[0-9]{1,2}\.[0-9]{1,2}\.$""".toRegex(), "d.M.yyyy", DateTimeParser::parseDayWithoutYear),

    TIME_HHMM("""^[0-9]{2}:[0-9]{2}$""".toRegex(), "HH:mm", DateTimeParser::parseTime),
    TIME_HHMMSS("""^[0-9]{2}:[0-9]{2}:[0-9]{2}$""".toRegex(), "HH:mm:ss", DateTimeParser::parseTime),

    DATETIME_EN(
        """^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}$""".toRegex(),
        "yyyy-MM-dd HH:mm",
        DateTimeParser::parseDateTime
    ),
    DATETIME_EN_SECONDS(
        """^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$""".toRegex(),
        "yyyy-MM-dd HH:mm:ss",
        DateTimeParser::parseDateTime
    ),
    DATETIME_DE(
        """^[0-9]{1,2}\.[0-9]{1,2}\.[0-9]{4} [0-9]{2}:[0-9]{2}$""".toRegex(),
        "d.M.yyyy HH:mm",
        DateTimeParser::parseDateTime
    ),
    DATETIME_DE_SECONDS(
        """^[0-9]{1,2}\.[0-9]{1,2}\.[0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}$""".toRegex(),
        "d.M.yyyy HH:mm:ss",
        DateTimeParser::parseDateTime
    ),
    DATETIME_DE_WITHOUT_YEAR(
        """^[0-9]{1,2}\.[0-9]{1,2}\. [0-9]{2}:[0-9]{2}$""".toRegex(),
        "d.M.yyyy HH:mm",
        DateTimeParser::parseDateTimeWithoutYear
    ),
}


class DateTimeParser {
    fun parseDay(pattern: String, dateString: String, @Suppress("UNUSED_PARAMETER") referenceTime: LocalDateTime): LocalDateTime {
        val f = createFormatter(pattern)
        return LocalDateTime.of(LocalDate.parse(dateString, f), LocalTime.MIDNIGHT)
    }

    fun parseDayWithoutYear(pattern: String, dateString: String, referenceTime: LocalDateTime): LocalDateTime {
        val f = createFormatter(pattern)
        val localDateTime = LocalDateTime.of(LocalDate.parse(dateString + referenceTime.year, f), LocalTime.MIDNIGHT)
        if (referenceTime.isAfter(localDateTime)) {
            return localDateTime.plusYears(1)
        }
        return localDateTime
    }

    fun parseTime(pattern: String, dateString: String, referenceTime: LocalDateTime): LocalDateTime {
        val f = createFormatter(pattern)
        val localTime = LocalTime.parse(dateString, f)
        if (referenceTime.toLocalTime().isAfter(localTime)) {
            return LocalDateTime.of(referenceTime.toLocalDate().plusDays(1), localTime)
        }
        return LocalDateTime.of(referenceTime.toLocalDate(), localTime)
    }

    fun parseDateTime(pattern: String, dateString: String, @Suppress("UNUSED_PARAMETER") referenceTime: LocalDateTime): LocalDateTime {
        val f = createFormatter(pattern)
        return LocalDateTime.parse(dateString, f)
    }

    fun parseDateTimeWithoutYear(pattern: String, dateString: String, referenceTime: LocalDateTime): LocalDateTime {
        val f = createFormatter(pattern)
        val localDate = LocalDateTime.parse(dateString.replace(" ", referenceTime.year.toString() + " "), f)
        if (referenceTime.isAfter(localDate)) {
            return localDate.plusYears(1)
        }
        return localDate
    }

    private fun createFormatter(pattern: String) = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.LENIENT)

    fun parse(timespecString: String, referenceTime: LocalDateTime): LocalDateTime? {
        val format = DateTimeFormat.entries
            .find { it.regex.matches(timespecString) }
            ?: return null
        return format.function.invoke(DateTimeParser(), format.pattern, timespecString, referenceTime)
    }
}
