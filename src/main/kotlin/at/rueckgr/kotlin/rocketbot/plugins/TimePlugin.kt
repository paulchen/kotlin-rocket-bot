package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.util.time.DateTimeDifferenceCalculator
import at.rueckgr.kotlin.rocketbot.util.time.DateTimeDifferenceCalculator.TimeUnit
import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.time.DateTimeParser
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

class TimePlugin : AbstractPlugin(), Logging {
    enum class Format(
        val regex: Regex,
        val pattern: String,
        val function: (DateTimeParser, String, String) -> LocalDateTime
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

    private val pizzaDate = LocalDateTime.of(LocalDate.of(2016, 11, 19), LocalTime.of(11, 51, 29))
    private val wmDate = LocalDateTime.of(LocalDate.of(2022, 11, 21), LocalTime.of(11, 0, 0))

    override fun getCommands(): List<String> {
        return listOf("t", "wm", "oldyear", "newyear", "pizza")
    }

    override fun handle(message: String): List<OutgoingMessage> {
        if (message.contains(" ")) {
            val dateString = message.substring(message.indexOf(" ") + 1)

            try {
                for (format in Format.values()) {
                    if (format.regex.matches(dateString)) {
                        val date = format.function.invoke(DateTimeParser(), format.pattern, dateString)
                        return listOf(OutgoingMessage(DateTimeDifferenceCalculator().formatTimeDifference(LocalDateTime.now(), date)))
                    }
                }
            } catch (e: DateTimeParseException) {
                logger().error(e.message, e)
            }
        } else {
            if (message == "!pizza") {
                val difference = DateTimeDifferenceCalculator()
                    .formatTimeDifference(LocalDateTime.now(), pizzaDate, listOf(TimeUnit.YEAR, TimeUnit.MONTH))
                    .replace(" ago", "")
                return listOf(OutgoingMessage("enri owes us pizza for $difference"))
            }
            val date: LocalDateTime = when (message) {
                "!wm" -> wmDate
                "!oldyear" -> getBeginOfCurrentYear()
                "!newyear" -> getBeginOfCurrentYear().plusYears(1)
                else -> return emptyList()
            }

            val (emoji, username) = when (message) {
                "!wm" -> listOf(":soccer:", ConfigurationProvider.getSoccerConfiguration().username)
                else -> listOf(null, null)
            }
            return listOf(OutgoingMessage(DateTimeDifferenceCalculator().formatTimeDifference(LocalDateTime.now(), date), emoji, username))
        }

        return emptyList()
    }

    private fun getBeginOfCurrentYear() = LocalDateTime.now()
        .withMonth(1)
        .withDayOfMonth(1)
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)

    override fun getHelp(command: String) = when (command) {
        "t" -> listOf(
            "`!t <time>` outputs the duration since/until <time>. If <time> is an incomplete specification (e.g. missing year), the next occurrence of the given time will be assumed.",
            "Supported formats for <time>: `YYYY-MM-DD`, `YYYY-MM-DD hh:mm`, `YYYY-MM-DD hh:mm:ss`, `DD.MM`, `DD.MM.YYYY`, `DD.MM. hh:mm`, `DD.MM.YYYY hh:mm`, `DD.MM. hh:mm:ss`, `DD.MM.YYY hh:mm:ss`, `hh:mm`, `hh:mm:ss`"
        )
        "wm" -> listOf("`!wm` tells the time period until the beginning of the next FIFA World Cup")
        "oldyear" -> listOf("`!oldyear` tells the time period since the beginning of the current year")
        "newyear" -> listOf("`!newyear` tells the time period until the beginning of the next year")
        "pizza" -> listOf("`!pizza` tells you for how much time enri has already failed to pay a round of pizza")
        else -> emptyList()
    }

    override fun getProblems() = emptyList<String>()

}
