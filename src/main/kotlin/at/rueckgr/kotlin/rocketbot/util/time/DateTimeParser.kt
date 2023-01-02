package at.rueckgr.kotlin.rocketbot.util.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

class DateTimeParser {
    fun parseDay(pattern: String, dateString: String): LocalDateTime {
        val f = createFormatter(pattern)
        return LocalDateTime.of(LocalDate.parse(dateString, f), LocalTime.MIDNIGHT)
    }

    fun parseDayWithoutYear(pattern: String, dateString: String) = parseDayWithoutYear(pattern, LocalDateTime.now(), dateString)

    fun parseDayWithoutYear(pattern: String, referenceTime: LocalDateTime, dateString: String): LocalDateTime {
        val f = createFormatter(pattern)
        val localDateTime = LocalDateTime.of(LocalDate.parse(dateString + referenceTime.year, f), LocalTime.MIDNIGHT)
        if (referenceTime.isAfter(localDateTime)) {
            return localDateTime.plusYears(1)
        }
        return localDateTime
    }

    fun parseTime(pattern: String, dateString: String): LocalDateTime = parseTime(pattern, LocalDateTime.now(), dateString)

    fun parseTime(pattern: String, referenceTime: LocalDateTime, dateString: String): LocalDateTime {
        val f = createFormatter(pattern)
        val localTime = LocalTime.parse(dateString, f)
        if (referenceTime.toLocalTime().isAfter(localTime)) {
            return LocalDateTime.of(referenceTime.toLocalDate().plusDays(1), localTime)
        }
        return LocalDateTime.of(referenceTime.toLocalDate(), localTime)
    }

    fun parseDateTime(pattern: String, dateString: String): LocalDateTime {
        val f = createFormatter(pattern)
        return LocalDateTime.parse(dateString, f)
    }

    fun parseDateTimeWithoutYear(pattern: String, dateString: String) = parseDateTimeWithoutYear(pattern, LocalDateTime.now(), dateString)

    fun parseDateTimeWithoutYear(pattern: String, referenceTime: LocalDateTime, dateString: String): LocalDateTime {
        val f = createFormatter(pattern)
        val localDate = LocalDateTime.parse(dateString.replace(" ", referenceTime.year.toString() + " "), f)
        if (referenceTime.isAfter(localDate)) {
            return localDate.plusYears(1)
        }
        return localDate
    }

    private fun createFormatter(pattern: String) = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.LENIENT)
}
