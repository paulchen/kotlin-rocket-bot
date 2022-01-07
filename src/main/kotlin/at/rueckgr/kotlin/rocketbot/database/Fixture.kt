package at.rueckgr.kotlin.rocketbot.database

import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.schema.*
import java.time.LocalDateTime

interface Fixture : Entity<Fixture> {
    companion object : Entity.Factory<Fixture>()

    var id: Long
    var leagueId: Long
    var season: Int
    var date: LocalDateTime
    var round: String
    var teamHome: String
    var teamAway: String
    var status: String
    var goalsHalftimeHome: Int?
    var goalsHalftimeAway: Int?
    var goalsFullftimeHome: Int?
    var goalsFulltimeAway: Int?
    var goalsExtratimeHome: Int?
    var goalsExtratimeAway: Int?
    var goalsPenaltyHome: Int?
    var goalsPenaltyAway: Int?
    var eventsProcessed: Int
}

object Fixtures : Table<Fixture>("fixture") {
    var id = long("id").primaryKey().bindTo { it.id }
    var leagueId = long("league_id").bindTo { it.leagueId }
    var season = int("season").bindTo { it.season }
    var date = datetime("date").bindTo { it.date }
    var round = varchar("round").bindTo { it.round }
    var teamHome = varchar("team_home").bindTo { it.teamHome }
    var teamAway = varchar("team_away").bindTo { it.teamAway }
    var status = varchar("status").bindTo { it.status }
    var goalsHalftimeHome = int("goals_halftime_home").bindTo { it.goalsHalftimeHome }
    var goalsHalftimeAway = int("goals_halftime_away").bindTo { it.goalsHalftimeAway }
    var goalsFulltimeHome = int("goals_fulltime_home").bindTo { it.goalsFullftimeHome }
    var goalsFulltimeAway = int("goals_fulltime_away").bindTo { it.goalsFulltimeAway }
    var goalsExtratimeHome = int("goals_extratime_home").bindTo { it.goalsExtratimeHome }
    var goalsExtratimeAway = int("goals_extratime_away").bindTo { it.goalsExtratimeAway }
    var goalsPenaltyHome = int("goals_penalty_home").bindTo { it.goalsPenaltyHome }
    var goalsPenaltyAway = int("goals_penalty_away").bindTo { it.goalsPenaltyAway }
    var eventsProcessed = int("events_processed").bindTo { it.eventsProcessed }
}
