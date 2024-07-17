package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.database.SeriousMode
import at.rueckgr.kotlin.rocketbot.database.SeriousModes
import at.rueckgr.kotlin.rocketbot.util.*
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.gt
import org.ktorm.entity.*
import java.time.LocalDateTime

val Database.seriousModes get() = this.sequenceOf(SeriousModes)

class SeriousModeService : Logging {
    fun activateSeriousMode(channel: String): LocalDateTime {
        val duration = ConfigurationProvider.getConfiguration().plugins?.seriousMode?.duration ?: 3600L
        val newStartDate = LocalDateTime.now()
        val newEndDate = LocalDateTime.now().plusSeconds(duration)

        val database = Db().connection
        val existingEntry = database.seriousModes.find { it.channelId eq channel }
        if (existingEntry != null) {
            if (existingEntry.endDate.isBefore(newStartDate)) {
                existingEntry.startDate = newStartDate
            }
            existingEntry.endDate = newEndDate
            existingEntry.flushChanges()
        }
        else {
            val newEntry = SeriousMode {
                channelId = channel
                startDate = newStartDate
                endDate = newEndDate
            }
            database.seriousModes.add(newEntry)
        }

        return newEndDate
    }

    fun deactivateSeriousMode(channel: String): Boolean {
        val database = Db().connection
        val existingEntry = database.seriousModes.find { it.channelId eq channel } ?: return false
        val newEndDate = LocalDateTime.now()
        if (existingEntry.endDate.isBefore(newEndDate)) {
            return false
        }

        existingEntry.endDate = newEndDate
        existingEntry.flushChanges()

        return true
    }

    fun isInSeriousMode(channel: String): Boolean {
        try {
            val database = Db().connection
            val existingEntry = database.seriousModes.find { it.channelId eq channel }
            return existingEntry != null && existingEntry.endDate.isAfter(LocalDateTime.now())
        }
        catch (e: DbException) {
            logger().error("Unable to connect to database, defaulting to serious mode")
            return true
        }
    }

    fun getSeriousModeData() = Db().connection
            .seriousModes
            .filter { it.endDate gt LocalDateTime.now() }
            .toList()
}
