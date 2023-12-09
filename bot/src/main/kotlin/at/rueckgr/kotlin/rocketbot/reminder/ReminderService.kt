package at.rueckgr.kotlin.rocketbot.reminder

import at.rueckgr.kotlin.rocketbot.database.Reminder
import at.rueckgr.kotlin.rocketbot.database.Reminders
import at.rueckgr.kotlin.rocketbot.util.Db
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.handleExceptions
import at.rueckgr.kotlin.rocketbot.util.logger
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

// TODO add some logging
class ReminderService : Logging {
    private val executorService = Executors.newScheduledThreadPool(1)

    fun scheduleExecution() {
        executorService.schedule( { handleExceptions { executeReminder() } }, 30, TimeUnit.SECONDS)
    }

    fun executeReminder() {
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

    private fun remind(it: Reminder): Boolean {
        // TODO create message and submit
        return true
    }

    private fun updateOrRemove(database: Database, reminder: Reminder) {
        if (reminder.notifyUnit != null && reminder.notifyInterval != null) {
            // TODO calculate next execution and save
        }
        else {
            // TODO delete reminder from database
        }
    }
}
