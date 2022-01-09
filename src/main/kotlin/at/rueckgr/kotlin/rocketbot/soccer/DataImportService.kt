package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.database.Fixture
import at.rueckgr.kotlin.rocketbot.database.Fixtures
import at.rueckgr.kotlin.rocketbot.database.Venue
import at.rueckgr.kotlin.rocketbot.database.Venues
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Db
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import com.api_football.model.FixtureResponseFixtureVenue
import com.api_football.model.FixtureResponseResponse
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.entity.*
import java.time.ZoneId

val Database.fixtures get() = this.sequenceOf(Fixtures)
val Database.venues get() = this.sequenceOf(Venues)

class DataImportService : Logging {
    fun runDailyUpdate() {
        val database = Db().connection

        val importedFixtures = FootballApiService()
            .getAllFixtures()
            .response
            .map { importFixture(database, it); it.fixture.id!! }
            .toList()

        removeUnlistedFixtures(database, importedFixtures)
    }

    private fun importFixture(database: Database, fixtureResponse: FixtureResponseResponse): Fixture {
        val id = fixtureResponse.fixture.id!!

        val entity = database.fixtures.find { it.id eq id } ?: return createNewFixture(database, fixtureResponse)

        mapToEntity(database, fixtureResponse, entity)
        entity.flushChanges()
        return entity
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

    private fun mapToEntity(database: Database, fixtureResponse: FixtureResponseResponse, entity: Fixture) {
        entity.leagueId = fixtureResponse.league.id!!
        entity.season = fixtureResponse.league.season!!
        entity.date = fixtureResponse.fixture.date!!.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
        entity.round = fixtureResponse.league.round!!
        entity.teamHome = fixtureResponse.teams.home?.name ?: "unknown"
        entity.teamAway = fixtureResponse.teams.away?.name ?: "unknown"
        entity.status = fixtureResponse.fixture.status?.short?.value!!
        entity.goalsHalftimeHome = fixtureResponse.score.halftime?.home
        entity.goalsHalftimeAway = fixtureResponse.score.halftime?.away
        entity.goalsFullftimeHome = fixtureResponse.score.fulltime?.home
        entity.goalsFulltimeAway = fixtureResponse.score.fulltime?.away
        entity.goalsExtratimeHome = fixtureResponse.score.extratime?.home
        entity.goalsExtratimeAway = fixtureResponse.score.extratime?.away
        entity.goalsPenaltyHome = fixtureResponse.score.penalty?.home
        entity.goalsPenaltyAway = fixtureResponse.score.penalty?.away
        entity.eventsProcessed = fixtureResponse.events?.size ?: 0
        entity.venue = getVenue(database, fixtureResponse)
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
        val id = venue.id ?: return null

        val entity = database.venues.find { it.id eq id } ?: return createNewVenue(database, venue)

        mapToEntity(venue, entity)
        entity.flushChanges()
        return entity
    }

    private fun mapToEntity(venue: FixtureResponseFixtureVenue, entity: Venue) {
        entity.name = venue.name ?: entity.name
        entity.city = venue.city ?: entity.city
    }

    private fun createNewVenue(database: Database, venue: FixtureResponseFixtureVenue): Venue {
        val entity = Venue {
            id = venue.id!!
            name = venue.name
            city = venue.city
        }
        database.venues.add(entity)
        return entity
    }
}
