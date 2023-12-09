package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.*
import at.rueckgr.kotlin.rocketbot.database.Reminder
import at.rueckgr.kotlin.rocketbot.database.reminders
import at.rueckgr.kotlin.rocketbot.reminder.ReminderService
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Db
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.time.DateTimeParser
import at.rueckgr.kotlin.rocketbot.util.time.TimeUnit
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.removeIf
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

data class MessageParts(val targetUsername: String, val subject: String, val timespec: String)

data class Timespec(val nextNotification: LocalDateTime, val notifyInterval: Long?, val notifyUnit: TimeUnit?)

class RemindException(message: String): Exception(message)

// TODO add some logging
class RemindPlugin : AbstractPlugin(), Logging {
    override fun getCommands(): List<String> = listOf("remind", "unremind")

    override fun init() {
        ReminderService().scheduleExecution()
    }

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message) =
        try {
            if (message.message.startsWith("!remind")) {
                addReminder(channel, user, message)
            }
            else if (message.message.startsWith("!unremind")) {
                removeReminder( user, message)
            }
            else {
                emptyList()
            }
        }
        catch (e: RemindException) {
            listOf(OutgoingMessage(e.message ?: "Unknown problem occurred"))
        }

    private fun addReminder(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        val (targetUsername, subject, timespecString) = splitRemindMessage(message)
        val notifyeeId = if (targetUsername == "me") {
            user.id
        }
        else {
            val notifyee: UserDetails = ArchiveService().getUserDetails(targetUsername)
                ?: throw RemindException("Unknown user {$targetUsername}")
            notifyee.user.id
        }
        if (notifyeeId != Bot.userId && !isAdmin(user)) {
            throw RemindException("Sorry, you are not allowed to do that.")
        }
        val timespec = parseTimespec(timespecString)
            ?: throw RemindException("Invalid time/interval specification")

        val id = createReminder(channel.id, notifyeeId, subject, timespec)
        return listOf(OutgoingMessage("Will do! _(id: $id)_"))
    }

    private fun isAdmin(user: EventHandler.User) =
        ConfigurationProvider.getConfiguration().plugins?.admin?.admins?.contains(user.id) ?: false

    private fun createReminder(channelId: String, notifyeeId: String, reminderSubject: String, timespec: Timespec): Int {
        val reminder = Reminder {
            notifyer = Bot.userId!!
            notifyee = notifyeeId
            channel = channelId
            subject = reminderSubject
            createdAt = LocalDateTime.now()
            nextNotification = timespec.nextNotification
            notifyInterval = timespec.notifyInterval
            notifyUnit = timespec.notifyUnit
        }
        return Db().connection.reminders.add(reminder)
    }

    fun splitRemindMessage(message: EventHandler.Message): MessageParts {
        // !remind <user> about <subject> (at|in|every)
        val aboutPos = message.message.indexOf(" about ", 8)
        val timespecPos = listOf(" at ", " in ", " every ")
            .maxOfOrNull { message.message.lastIndexOf(it) }!!
        if (aboutPos < 9 || timespecPos == -1) {
            throw RemindException("Invalid format of input")
        }

        val username = message.message.substring(8, aboutPos).trim()
        val subject = message.message.substring(aboutPos + 7, timespecPos).trim()
        val timespec = message.message.substring(timespecPos + 1).trim()
        if (username.isBlank() || subject.isBlank() || timespec.isBlank()) {
            throw RemindException("Invalid format of input")
        }

        return MessageParts(username, subject, timespec)
    }

    // TODO write unit tests
    private fun parseTimespec(timespecString: String): Timespec? =
        if (timespecString.startsWith("at")) {
            parseAtTimespec(timespecString.substring(3))
        }
        else if (timespecString.startsWith("in")) {
            parseInTimespec(timespecString.substring(3))
        }
        else if (timespecString.startsWith("every")) {
            parseEveryTimespec(timespecString.substring(6))
        }
        else {
            null
        }

    private fun parseAtTimespec(timespecString: String): Timespec? {
        try {
            val date = DateTimeParser().parse(timespecString) ?: return null
            return Timespec(date, null, null)
        }
        catch (e: DateTimeParseException) {
            return null
        }
    }

    private fun parseInTimespec(timespecString: String): Timespec? {
        val timespec = parseEveryTimespec(timespecString) ?: return null
        return Timespec(timespec.nextNotification, null, null)
    }

    private fun parseEveryTimespec(timespecString: String): Timespec? {
        val regex = """([0-9]+)\s*([a-z])""".toRegex(RegexOption.IGNORE_CASE)
        val matchResult = regex.matchEntire(timespecString) ?: return null
        val (countString, unitString) = matchResult.destructured
        val count = countString.toLong()
        if (count == 0L) {
            return null
        }
        val unit = parseUnit(unitString)
        val nextExecution = calculateNextExecution(count, unit)

        return Timespec(nextExecution, count, unit)
    }

    private fun calculateNextExecution(count: Long, unit: TimeUnit) =
        unit.plusFunction.invoke(LocalDateTime.now(), count)

    private fun parseUnit(unitString: String) =
        TimeUnit.entries
            .find { it.singular == unitString || it.plural == unitString || it.abbreviation == unitString }
            ?: throw RemindException("Unknown time unit")


    private fun removeReminder(user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        val parts = message.message.split(" ")
        if (parts.size != 2 || parts[1].toLongOrNull() == null) {
            throw RemindException("Invalid input")
        }
        val reminderId = parts[1].toLong()
        val reminder = Db().connection.reminders.find { it.id eq reminderId } ?: throw RemindException("Unknown reminder id")
        if (!isAdmin(user) && reminder.notifyer != user.id && reminder.notifyee != user.id) {
            throw RemindException("Sorry, you are not allowed to do that")
        }
        Db().connection.reminders.removeIf { it.id eq reminderId }
        return listOf(OutgoingMessage("Reminder $reminderId removed"))
    }

    override fun getHelp(command: String) = when(command) {
        "remind" -> listOf("TODO")
        "unremind" -> listOf("TODO")
        else -> emptyList()
    }

    override fun getProblems(): List<String> = emptyList() // TODO
}
