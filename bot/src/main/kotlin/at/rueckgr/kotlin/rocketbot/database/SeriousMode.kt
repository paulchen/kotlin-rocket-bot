package at.rueckgr.kotlin.rocketbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalDateTime

interface SeriousMode : Entity<SeriousMode> {
    companion object : Entity.Factory<SeriousMode>()

    var channelId: String
    var startDate: LocalDateTime
    var endDate: LocalDateTime
}

object SeriousModes : Table<SeriousMode>("serious_mode") {
    var channelId = varchar("channel_id").primaryKey().bindTo { it.channelId }
    var startDate = datetime("start_date").bindTo { it.startDate }
    var endDate = datetime("end_date").bindTo { it.endDate }
}
