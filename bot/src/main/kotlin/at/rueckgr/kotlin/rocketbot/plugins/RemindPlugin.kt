package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.*
import at.rueckgr.kotlin.rocketbot.database.Reminder
import at.rueckgr.kotlin.rocketbot.database.Reminders
import at.rueckgr.kotlin.rocketbot.database.reminders
import at.rueckgr.kotlin.rocketbot.reminder.ReminderService
import at.rueckgr.kotlin.rocketbot.reminder.calculateNextExecution
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Db
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import at.rueckgr.kotlin.rocketbot.util.time.DateTimeParser
import at.rueckgr.kotlin.rocketbot.util.time.TimeUnit
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.removeIf
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

data class MessageParts(val targetUsername: String, val subject: String, val timespec: String)

data class Timespec(val nextNotification: LocalDateTime, val notifyInterval: Long?, val notifyUnit: TimeUnit?)

class RemindException(message: String): Exception(message)

class RemindPlugin : AbstractPlugin(), Logging {
    companion object {
        private val reminderService = ReminderService()
    }

    override fun getCommands(): List<String> = listOf("remind", "unremind")

    override fun init() {
        reminderService.scheduleExecution()
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
            val notifyee: UserDetails = ArchiveService().getUserByUsername(targetUsername)
                ?: throw RemindException("Unknown user $targetUsername")
            if (!notifyee.user.rooms.contains(channel.id)) {
                throw RemindException("User $targetUsername is not in this room")
            }
            notifyee.user.id
        }
        if (notifyeeId != user.id && !isAdmin(user)) {
            throw RemindException("Sorry, you are not allowed to do that.")
        }
        if (notifyeeId == Bot.userId) {
            throw RemindException("Sorry, I won't remind myself about anything.")
        }
        val timespec = parseTimespec(timespecString, LocalDateTime.now())
            ?: throw RemindException("Invalid time/interval specification")

        val id = createReminder(channel.id, user.id, notifyeeId, subject, timespec)
        reminderService.scheduleExecution()
        return listOf(OutgoingMessage("@${user.username} Will do! Use `!unremind $id` to cancel."))
    }

    private fun isAdmin(user: EventHandler.User) =
        ConfigurationProvider.getConfiguration().plugins?.admin?.admins?.contains(user.id) ?: false

    private fun createReminder(channelId: String, notifyerId: String, notifyeeId: String, reminderSubject: String, timespec: Timespec): Long {
        val reminder = Reminder {
            notifyer = notifyerId
            notifyee = notifyeeId
            channel = channelId
            subject = reminderSubject
            createdAt = LocalDateTime.now()
            nextNotification = timespec.nextNotification
            notifyInterval = timespec.notifyInterval
            notifyUnit = timespec.notifyUnit
        }
        logger().info("Creating reminder {}", reminder)
        Db().connection.reminders.add(reminder)
        return reminder.id!!
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

    fun parseTimespec(timespecString: String, referenceTime: LocalDateTime): Timespec? =
        if (timespecString.startsWith("at")) {
            parseAtTimespec(timespecString.substring(3), referenceTime)
        }
        else if (timespecString.startsWith("in")) {
            parseInTimespec(timespecString.substring(3), referenceTime)
        }
        else if (timespecString.startsWith("every")) {
            parseEveryTimespec(timespecString.substring(6), referenceTime)
        }
        else {
            null
        }

    private fun parseAtTimespec(timespecString: String, referenceTime: LocalDateTime): Timespec? {
        try {
            val date = DateTimeParser().parse(timespecString, referenceTime) ?: return null
            return Timespec(date, null, null)
        }
        catch (e: DateTimeParseException) {
            return null
        }
    }

    private fun parseInTimespec(timespecString: String, referenceTime: LocalDateTime): Timespec? {
        val timespec = parseEveryTimespec(timespecString, referenceTime) ?: return null
        return Timespec(timespec.nextNotification, null, null)
    }

    private fun parseEveryTimespec(timespecString: String, referenceTime: LocalDateTime): Timespec? {
        val regex = """([0-9]+)\s*([a-z]+)""".toRegex(RegexOption.IGNORE_CASE)
        val matchResult = regex.matchEntire(timespecString) ?: return null
        val (countString, unitString) = matchResult.destructured
        val count = countString.toLong()
        if (count == 0L) {
            return null
        }
        val unit = parseUnit(unitString) ?: return null
        val nextExecution = calculateNextExecution(count, unit, referenceTime)

        return Timespec(nextExecution, count, unit)
    }

    private fun parseUnit(unitString: String) =
        TimeUnit.entries
            .find { it.singular == unitString || it.plural == unitString || it.abbreviation == unitString }

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
        reminderService.scheduleExecution()
        return listOf(OutgoingMessage("Reminder $reminderId removed"))
    }

    override fun getHelp(command: String) = when(command) {
        "remind" -> listOf(
            "`!remind` adds a reminder. Format: `!remind {<nickname>|me} about <subject> <timespec>`\n" +
            "`<timespec>` can be\n" +
            "- an absolute date/time, e.g. `at 13:37`\n" +
            "- a time period, e.g. `in 42 hours`\n" +
            "- an interval, e.g. `every 30 seconds`\n"
        )
        "unremind" -> listOf("`!unremind` cancels a reminder that was created using `!remind`.")
        else -> emptyList()
    }

    override fun getProblems(): List<String> {
        val problems = mutableListOf<String>()

        val overdueRemindersCount = reminderService.getOverdueReminders().count()
        if (overdueRemindersCount > 0) {
            problems.add("$overdueRemindersCount overdue reminders")
        }

        return problems
    }

    override fun getAdditionalStatus(): Map<String, String> {
        val totalRemindersCount = Db().connection
            .from(Reminders)
            .select()
            .map { Reminders.createEntity(it) }
            .count()
        val overdueRemindersCount = reminderService.getOverdueReminders().count()
        return mapOf(
            "number of reminders" to totalRemindersCount.toString(),
            "overdue reminders" to overdueRemindersCount.toString(),
            "next reminder execution" to (reminderService.getNextSchedulerExecution()?.toString() ?: "not scheduled")
        )
    }
}
