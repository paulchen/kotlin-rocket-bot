package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.util.time.DateTimeDifferenceCalculator
import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.EventHandler
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.time.DateTimeParser
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import at.rueckgr.kotlin.rocketbot.util.time.TimeUnit
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

class TimePlugin : AbstractPlugin(), Logging {
    private val pizzaDate = LocalDateTime.of(LocalDate.of(2016, 11, 19), LocalTime.of(11, 51, 29))
    private val emDate = LocalDateTime.of(LocalDate.of(2024, 6, 14), LocalTime.of(12, 0, 0))
    private val wmDate = LocalDateTime.of(LocalDate.of(2026, 6, 10), LocalTime.of(12, 0, 0))

    override fun getCommands(): List<String> {
        return listOf("t", "em", "wm", "oldyear", "newyear", "pizza")
//        return listOf("t", "oldyear", "newyear", "pizza")
    }

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        val messageText = message.message.lowercase()
        val now = LocalDateTime.now()
        if (messageText.contains(" ")) {
            val dateString = messageText.substring(messageText.indexOf(" ") + 1)

            try {
                val date = DateTimeParser().parse(dateString, now)
                if (date == null) {
                    logger().error("Unable to parse date string $dateString")
                }
                else {
                    return listOf(OutgoingMessage(DateTimeDifferenceCalculator().formatTimeDifference(now, date, listOf(TimeUnit.WEEK))))
                }
            } catch (e: DateTimeParseException) {
                logger().error(e.message, e)
            }
        } else {
            if (messageText == "!pizza") {
                val difference = DateTimeDifferenceCalculator()
                    .formatTimeDifference(now, pizzaDate, listOf(TimeUnit.YEAR, TimeUnit.MONTH, TimeUnit.WEEK))
                    .replace(" ago", "")
                return listOf(OutgoingMessage("enri owes us pizza for $difference"))
            }
            val date: LocalDateTime = when (messageText) {
                "!em" -> emDate
                "!wm" -> wmDate
                "!oldyear" -> getBeginOfCurrentYear()
                "!newyear" -> getBeginOfCurrentYear().plusYears(1)
                else -> return emptyList()
            }

            val (emoji, username) = when (messageText) {
                "!em", "!wm" -> listOf(":soccer:", ConfigurationProvider.getSoccerConfiguration().username)
                else -> listOf(null, null)
            }
            return listOf(OutgoingMessage(DateTimeDifferenceCalculator().formatTimeDifference(now, date, listOf(TimeUnit.WEEK)), emoji, username))
//            return listOf(OutgoingMessage(DateTimeDifferenceCalculator().formatTimeDifference(LocalDateTime.now(), date)))
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
        "em" -> listOf("`!em` tells the time period until the beginning of the next UEFA European Championship")
        "wm" -> listOf("`!wm` tells the time period until the beginning of the next FIFA World Cup")
        "oldyear" -> listOf("`!oldyear` tells the time period since the beginning of the current year")
        "newyear" -> listOf("`!newyear` tells the time period until the beginning of the next year")
        "pizza" -> listOf("`!pizza` tells you for how much time enri has already failed to pay a round of pizza")
        else -> emptyList()
    }

    override fun getProblems() = emptyList<String>()

}
