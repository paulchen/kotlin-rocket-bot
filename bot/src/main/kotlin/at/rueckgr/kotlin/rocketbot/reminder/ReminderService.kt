package at.rueckgr.kotlin.rocketbot.reminder

import at.rueckgr.kotlin.rocketbot.ArchiveService
import at.rueckgr.kotlin.rocketbot.Bot
import at.rueckgr.kotlin.rocketbot.WebserviceMessage
import at.rueckgr.kotlin.rocketbot.database.Reminder
import at.rueckgr.kotlin.rocketbot.database.Reminders
import at.rueckgr.kotlin.rocketbot.database.reminders
import at.rueckgr.kotlin.rocketbot.util.Db
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.handleExceptions
import at.rueckgr.kotlin.rocketbot.util.logger
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.removeIf
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ReminderService : Logging {
    private val executorService = Executors.newScheduledThreadPool(1)
    private var schedule: ScheduledFuture<*>? = null

    fun scheduleExecution() {
        synchronized(this) {
            schedule?.cancel(false)
            schedule = executorService.schedule({ handleExceptions { executeReminder() } }, 30, TimeUnit.SECONDS)
        }
    }

    private fun executeReminder() {
        logger().info("Checking for due reminders")
        try {
            val database = Db().connection
            database
                .from(Reminders)
                .select()
                .where { Reminders.nextNotification lte LocalDateTime.now() }
                .map { Reminders.createEntity(it) }
                .forEach {
                    if (remind(it)) {
                        updateOrRemove(database, it)
                    }
                }
        }
        catch (e: Throwable) {
            logger().error(e.message, e)
        }
        scheduleExecution()
    }

    private fun remind(reminder: Reminder): Boolean {
        val notifyer = if(reminder.notifyer == reminder.notifyee) {
            "You"
        }
        else {
            '@' + (ArchiveService().getUserById(reminder.notifyer)?.user?.username ?: return false)
        }
        val notifyee = ArchiveService().getUserById(reminder.notifyee)?.user?.username ?: return false
        val nextNotification = if(reminder.notifyUnit == null || reminder.notifyInterval == null) {
            ""
        }
        else {
            val next = calculateNextExecution(reminder.notifyInterval!!, reminder.notifyUnit!!, LocalDateTime.now())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            "; next notification after $next, use `!unremind ${reminder.id}` to cancel"
        }
        val message = "@$notifyee $notifyer told me to remind you about ${reminder.subject}${nextNotification}"
        Bot.webserviceMessageQueue.add(WebserviceMessage(reminder.channel, null, message))
        return true
    }

    private fun updateOrRemove(database: Database, reminder: Reminder) {
        if (reminder.notifyUnit != null && reminder.notifyInterval != null) {
            val nextExecution = calculateNextExecution(reminder.notifyInterval!!, reminder.notifyUnit!!, LocalDateTime.now())
            logger().info("Calculated next execution for reminder ${reminder.id} at $nextExecution")
            reminder.nextNotification = nextExecution
            reminder.flushChanges()
        }
        else {
            database.reminders.removeIf { it.id eq reminder.id!! }
        }
    }
}
