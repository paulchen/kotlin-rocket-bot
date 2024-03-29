package at.rueckgr.kotlin.rocketbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*
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
    var elapsed: Int?
    var goalsHalftimeHome: Int?
    var goalsHalftimeAway: Int?
    var goalsFulltimeHome: Int?
    var goalsFulltimeAway: Int?
    var goalsExtratimeHome: Int?
    var goalsExtratimeAway: Int?
    var goalsPenaltyHome: Int?
    var goalsPenaltyAway: Int?
    var eventsProcessed: Int
    var venue: Venue?
    var endDate: LocalDateTime?
    var announced: Boolean
    var pendingScoreChange: Boolean
    var firstPenaltyTeam: Long?
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
    var elapsed = int("elapsed").bindTo { it.elapsed }
    var goalsHalftimeHome = int("goals_halftime_home").bindTo { it.goalsHalftimeHome }
    var goalsHalftimeAway = int("goals_halftime_away").bindTo { it.goalsHalftimeAway }
    var goalsFulltimeHome = int("goals_fulltime_home").bindTo { it.goalsFulltimeHome }
    var goalsFulltimeAway = int("goals_fulltime_away").bindTo { it.goalsFulltimeAway }
    var goalsExtratimeHome = int("goals_extratime_home").bindTo { it.goalsExtratimeHome }
    var goalsExtratimeAway = int("goals_extratime_away").bindTo { it.goalsExtratimeAway }
    var goalsPenaltyHome = int("goals_penalty_home").bindTo { it.goalsPenaltyHome }
    var goalsPenaltyAway = int("goals_penalty_away").bindTo { it.goalsPenaltyAway }
    var eventsProcessed = int("events_processed").bindTo { it.eventsProcessed }
    var venueId = int("venue_id").references(Venues) { it.venue }
    var endDate = datetime("end_date").bindTo { it.endDate }
    var announced = boolean("announced").bindTo { it.announced }
    var pendingScoreChange = boolean("pending_score_change").bindTo { it.pendingScoreChange }
    var firstPenaltyTeam = long("first_penalty_team").bindTo { it.firstPenaltyTeam }
}
