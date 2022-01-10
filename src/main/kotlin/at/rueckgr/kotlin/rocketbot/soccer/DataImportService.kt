package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.database.*
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Db
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import com.api_football.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.time.LocalDateTime
import java.time.ZoneId

val Database.fixtures get() = this.sequenceOf(Fixtures)
val Database.venues get() = this.sequenceOf(Venues)

class DataImportService : Logging {
    fun runDailyUpdate() {
        val database = Db().connection

        val existingVenues = findExistingVenues(database)

        val importedFixtures = FootballApiService.instance
            .getAllFixtures()
            .response
            .map { importFixture(database, it); it.fixture.id!! }
            .toList()

        removeUnlistedFixtures(database, importedFixtures)

        processNewVenues(database, existingVenues)
    }

    fun runLiveUpdate(): List<ImportFixtureResult> {
        val database = Db().connection
        return findLiveFixtures(database)
            .map {
                importFixture(database, FootballApiService.instance.getFixture(it.id).response[0])
            }
            .toList()
    }

    private fun findExistingVenues(database: Database): List<Long> = database.venues.map { it.id }.toList()

    private fun findLiveFixtures(database: Database): List<Fixture> {
        val liveStates = FixtureState.getByPeriod(FixtureStatePeriod.LIVE)
        val oneHourAgo = LocalDateTime.now().minusHours(1)
        val inOneHour = LocalDateTime.now().plusHours(1)

        return database.from(Fixtures)
            .joinReferencesAndSelect()
            .where {
                (Fixtures.status inList liveStates) or ((Fixtures.date greater oneHourAgo) and (Fixtures.date less inOneHour)) or (Fixtures.endDate greater oneHourAgo)
            }
            .map { row -> Fixtures.createEntity(row) }
            .toList()
    }

    private fun processNewVenues(database: Database, existingVenues: List<Long>) {
        val venues = database.venues
            .filter { it.id notInList existingVenues }
            .filter { it.id greaterEq 0 }
            .toList()

        logger().debug("Updating venue data of {} venues with ids {}", venues.size, venues)

        venues.forEach { updateVenue(it) }
    }

    private fun updateVenue(entity: Venue) {
        val venue = FootballApiService().getVenue(entity.id)

        entity.name = venue.name
        entity.city = venue.city
        entity.country = venue.country ?: entity.country
        entity.capacity = venue.capacity ?: entity.capacity

        entity.flushChanges()
    }

    private fun importFixture(database: Database, fixtureResponse: FixtureResponseResponse): ImportFixtureResult {
        val id = fixtureResponse.fixture.id!!

        val entity = database.fixtures.find { it.id eq id }
            ?: return ImportFixtureResult(createNewFixture(database, fixtureResponse), emptyList(), null)

        return mapToEntity(database, fixtureResponse, entity)
    }

    private fun createNewFixture(database: Database, fixtureResponse: FixtureResponseResponse): Fixture {
        val entity = Fixture {
            id = fixtureResponse.fixture.id!!
            leagueId = fixtureResponse.league.id!!
            season = fixtureResponse.league.season!!
            date = fixtureResponse.fixture.date!!.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
            round = fixtureResponse.league.round!!
            teamHome = fixtureResponse.teams.home?.name ?: "unknown"
            teamAway = fixtureResponse.teams.away?.name ?: "unknown"
            status = fixtureResponse.fixture.status?.short?.value!!
            goalsHalftimeHome = fixtureResponse.score.halftime?.home
            goalsHalftimeAway = fixtureResponse.score.halftime?.away
            goalsFullftimeHome = fixtureResponse.score.fulltime?.home
            goalsFulltimeAway = fixtureResponse.score.fulltime?.away
            goalsExtratimeHome = fixtureResponse.score.extratime?.home
            goalsExtratimeAway = fixtureResponse.score.extratime?.away
            goalsPenaltyHome = fixtureResponse.score.penalty?.home
            goalsPenaltyAway = fixtureResponse.score.penalty?.away
            eventsProcessed = 0
            venue = getVenue(database, fixtureResponse)
        }
        database.fixtures.add(entity)
        return entity
    }

    private fun mapToEntity(database: Database, fixtureResponse: FixtureResponseResponse, entity: Fixture): ImportFixtureResult {
        entity.leagueId = fixtureResponse.league.id!!
        entity.season = fixtureResponse.league.season!!
        entity.date = fixtureResponse.fixture.date!!.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
        entity.round = fixtureResponse.league.round!!
        entity.teamHome = fixtureResponse.teams.home?.name ?: "unbekannt"
        entity.teamAway = fixtureResponse.teams.away?.name ?: "unbekannt"

        val status = fixtureResponse.fixture.status?.short?.value ?: "TBD"
        val stateChange = if (status != entity.status) {
            processStateChange(status)
        }
        else {
            null
        }
        entity.status = status

        entity.goalsHalftimeHome = fixtureResponse.score.halftime?.home
        entity.goalsHalftimeAway = fixtureResponse.score.halftime?.away
        entity.goalsFullftimeHome = fixtureResponse.score.fulltime?.home
        entity.goalsFulltimeAway = fixtureResponse.score.fulltime?.away
        entity.goalsExtratimeHome = fixtureResponse.score.extratime?.home
        entity.goalsExtratimeAway = fixtureResponse.score.extratime?.away
        entity.goalsPenaltyHome = fixtureResponse.score.penalty?.home
        entity.goalsPenaltyAway = fixtureResponse.score.penalty?.away

        val eventsCount = fixtureResponse.events?.size ?: 0
        val newEvents = if (eventsCount > entity.eventsProcessed) {
            fixtureResponse
                .events!!
                .subList(entity.eventsProcessed, fixtureResponse.events.size)
                .mapNotNull { processEvent(fixtureResponse, entity, it) }
        }
        else {
            emptyList()
        }
        entity.eventsProcessed = eventsCount

        entity.venue = getVenue(database, fixtureResponse)

        entity.flushChanges()

        return ImportFixtureResult(entity, newEvents, stateChange)
    }

    private fun processStateChange(newState: String): String? = FixtureState.getByCode(newState)?.description

    private fun processEvent(fixtureResponse: FixtureResponseResponse, entity: Fixture, event: FixtureResponseEvents): String? {
        return if (event.type == "Goal") {
            val type = when (event.detail) {
                "Normal Goal" -> "Tor"
                "Own Goal" -> "Eigentor"
                "Penalty" -> "Elfmetertreffer"
                "Missed Penalty" -> "Vergebener Elfmeter"
                else -> event.detail
            }
            val team = TeamMapper.instance.mapTeamName(event.team?.name ?: "unbekannt")
            val player = findPlayer(fixtureResponse, event.team?.id, event.player?.id, event.player?.name)
            val score = MatchTitleService.instance.formatMatchScore(entity)

            if (player != null) {
                "$type für $team durch $player; Spielstand: $score"
            }
            else {
                "$type für $team; Spielstand: $score"
            }
        }
        else {
            null
        }
    }

    private fun findPlayer(fixtureResponse: FixtureResponseResponse, teamId: Long?, playerId: Long?, fallbackName: String?): String? {
        playerId ?: return fallbackName
        teamId ?: return fallbackName
        fixtureResponse.players ?: return fallbackName

        return fixtureResponse
            .players
            .first { it.team?.id == teamId }
            .players
            ?.first { it.player?.id == playerId }
            ?.player
            ?.name ?: fallbackName
    }

    private fun removeUnlistedFixtures(database: Database, importedFixtures: List<Long>) {
        val configuration = ConfigurationProvider.instance.getConfiguration()
        val soccerConfiguration = configuration.plugins?.soccer!!

        val leagueId = soccerConfiguration.leagueId!!
        val season = soccerConfiguration.season!!

        val fixturesToBeRemoved = database.fixtures
            .filter { it.season eq season }
            .filter { it.leagueId eq leagueId }
            .filter { it.id notInList importedFixtures }
            .map { it.id }
            .toList()

        logger().debug("Removing unlisted fixtures: {}", fixturesToBeRemoved)

        fixturesToBeRemoved.forEach { fixtureId -> database.fixtures.removeIf { it.id eq fixtureId } }
    }

    private fun getVenue(database: Database, fixtureResponse: FixtureResponseResponse): Venue? {
        val venue = fixtureResponse.fixture.venue ?: return null

        val entity = if (venue.id == null) {
            database.venues.find { (it.name eq venue.name!!) and (it.city eq venue.city!!) } ?: return createNewVenue(database, venue)
        }
        else {
            database.venues.find { it.id eq venue.id } ?: return createNewVenue(database, venue)
        }

        mapToEntity(venue, entity)
        entity.flushChanges()
        return entity
    }

    private fun mapToEntity(venue: FixtureResponseFixtureVenue, entity: Venue) {
        entity.name = venue.name ?: entity.name
        entity.city = venue.city ?: entity.city
    }

    private fun createNewVenue(database: Database, venue: FixtureResponseFixtureVenue): Venue {
        val venueId = venue.id ?: createArtificialVenueId(database)
        val entity = Venue {
            id = venueId
            name = venue.name
            city = venue.city
        }
        database.venues.add(entity)
        return entity
    }

    private fun createArtificialVenueId(database: Database): Long {
        val query = database
            .from(Venues)
            .select(Venues.id)
            .where { Venues.id less 0 }
            .orderBy(Venues.id.asc())
            .limit(0, 1)
        for (row in query) {
            return row.getLong(1) - 1
        }

        return -1L
    }
}

data class ImportFixtureResult(
    val fixture: Fixture,
    val newEvents: List<String>,
    val stateChange: String?
)
