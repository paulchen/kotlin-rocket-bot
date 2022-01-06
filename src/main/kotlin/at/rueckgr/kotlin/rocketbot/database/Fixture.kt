package at.rueckgr.kotlin.rocketbot.database

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.datetime
import me.liuwj.ktorm.schema.int
import me.liuwj.ktorm.schema.varchar

object Fixture : Table<Nothing>("fixture") {
    val id = int("id").primaryKey()
    val date = datetime("date")
    val round = varchar("round")
    val teamHome = varchar("team_home")
    val teamAway = varchar("team_away")
    val status = varchar("status")
    val goalsHalftimeHome = int("goals_halftime_home")
    val goalsHalftimeAway = int("goals_halftime_away")
    val goalsFulltimeHome = int("goals_fulltime_home")
    val goalsFulltimeAway = int("goals_fulltime_away")
    val goalsExtratimeHome = int("goals_extratime_home")
    val goalsExtratimeAway = int("goals_extratime_away")
    val goalsPenaltyHome = int("goals_penalty_home")
    val goalsPenaltyAway = int("goals_penalty_away")
    val eventsProcessed = int("events_processed")
}
