package at.rueckgr.kotlin.rocketbot.database

import at.rueckgr.kotlin.rocketbot.util.time.DateTimeDifferenceCalculator
import at.rueckgr.kotlin.rocketbot.util.time.TimeUnit
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

val Database.reminders get() = this.sequenceOf(Reminders)

interface Reminder : Entity<Reminder> {
    companion object : Entity.Factory<Reminder>()

    var id: Long?
    var notifyer: String
    var notifyee: String
    var channel: String
    var subject: String
    var createdAt: LocalDateTime
    var nextNotification: LocalDateTime
    var notifyInterval: Long?
    var notifyUnit: TimeUnit?
}

object Reminders : Table<Reminder>("reminder") {
    var id = long("id").primaryKey().bindTo { it.id }
    var notifyer = varchar("notifyer").bindTo { it.notifyer }
    var notifyee = varchar("notifyee").bindTo { it.notifyee }
    var channel = varchar("channel").bindTo { it.channel }
    var subject = varchar("subject").bindTo { it.subject }
    var createdAt = datetime("created_at").bindTo { it.createdAt }
    var nextNotification = datetime("next_notification").bindTo { it.nextNotification }
    var notifyInterval = long("notify_interval").bindTo { it.notifyInterval }
    var notifyUnit = enum<TimeUnit>("notify_unit").bindTo { it.notifyUnit }
}
