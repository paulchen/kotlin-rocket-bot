package at.rueckgr.kotlin.rocketbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalDateTime

/*
    id SERIAL NOT NULL,
    notifyer VARCHAR(255) NOT NULL,
    notifyee VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    next_notification TIMESTAMP WITH TIME ZONE NOT NULL,
    notify_interval INT,
    notify_unit VARCHAR(10),
    PRIMARY KEY (id)

 */
interface Reminder : Entity<Reminder> {
    companion object : Entity.Factory<Fixture>()

    var id: Long
    var notifyer: String
    var notifyee: String
    var createdAt: LocalDateTime
    var nextNotification: LocalDateTime
    var notifyInterval: Int?
    var notifyUnit: String?
}

object Reminders : Table<Reminder>("reminder") {
    var id = long("id").primaryKey().bindTo { it.id }
    var notifyer = varchar("notifyer").bindTo { it.notifyer }
    var notifyee = varchar("notifyee").bindTo { it.notifyee }
    var createdAt = datetime("created_at").bindTo { it.createdAt }
    var nextNotification = datetime("next_notification").bindTo { it.nextNotification }
    var notifyInterval = int("notify_interval").bindTo { it.notifyInterval }
    var notifyUnit = varchar("notifyUnit").bindTo { it.notifyUnit }
}
