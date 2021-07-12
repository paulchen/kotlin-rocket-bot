package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.DateTimeDifferenceCalculator
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TimePlugin : AbstractPlugin() {
    enum class Format(
        val regex: Regex,
        val pattern: String,
        val function: (TimePlugin, String, String) -> LocalDateTime
    ) {
        // TODO implement date without year
        DATE_EN("""^[0-9]{4}-[0-9]{2}-[0-9]{2}$""".toRegex(), "yyyy-MM-dd", TimePlugin::parseDay),
        DATE_DE("""^[0-9]{1,2}\.[0-9]{1,2}\.[0-9]{4}$""".toRegex(), "d.M.yyyy", TimePlugin::parseDay),

        TIME_HHMM("""^[0-9]{2}:[0-9]{2}$""".toRegex(), "HH:mm", TimePlugin::parseTime),
        TIME_HHMMSS("""^[0-9]{2}:[0-9]{2}:[0-9]{2}$""".toRegex(), "HH:mm:ss", TimePlugin::parseTime),

        DATETIME_EN("""^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}$""".toRegex(), "yyyy-MM-dd HH:mm", TimePlugin::parseDateTime),
        DATETIME_EN_SECONDS("""^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$""".toRegex(), "yyyy-MM-dd HH:mm:ss", TimePlugin::parseDateTime),
        DATETIME_DE("""^[0-9]{1,2}\.[0-9]{1,2}\.[0-9]{4} [0-9]{2}:[0-9]{2}$""".toRegex(), "d.M.yyyy HH:mm", TimePlugin::parseDateTime),
        DATETIME_DE_SECONDS("""^[0-9]{1,2}\.[0-9]{1,2}\.[0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}$""".toRegex(), "d.M.yyyy HH:mm:ss", TimePlugin::parseDateTime),
    }

    override fun getCommands(): List<String> {
        return listOf("t")
    }

    override fun handle(message: String): List<String> {
        val dateString = message.substring(message.indexOf(" ") + 1)

        try {
            for (format in Format.values()) {
                if (format.regex.matches(dateString)) {
                    val date = format.function.invoke(this, format.pattern, dateString)
                    val d = DateTimeDifferenceCalculator()
                    val timeDifference = d.calculateTimeDifference(LocalDateTime.now(), date)
                    return listOf(d.formatTimeDifference(timeDifference))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO log
        }

        return emptyList()
    }

    private fun parseDay(pattern: String, dateString: String): LocalDateTime {
        val f = DateTimeFormatter.ofPattern(pattern)
        return LocalDateTime.of(LocalDate.parse(dateString, f), LocalTime.MIDNIGHT)
    }

    private fun parseTime(pattern: String, dateString: String): LocalDateTime {
        val f = DateTimeFormatter.ofPattern(pattern)
        val localTime = LocalTime.parse(dateString, f)
        if (LocalTime.now().isAfter(localTime)) {
            return LocalDateTime.of(LocalDate.now().plusDays(1), localTime)
        }
        return LocalDateTime.of(LocalDate.now(), localTime)
    }

    private fun parseDateTime(pattern: String, dateString: String): LocalDateTime {
        val f = DateTimeFormatter.ofPattern(pattern)
        return LocalDateTime.parse(dateString, f)
    }
}
