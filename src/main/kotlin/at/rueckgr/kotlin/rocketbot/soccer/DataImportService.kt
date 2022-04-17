package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.database.*
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Db
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import com.api_football.models.FixtureResponseEvents
import com.api_football.models.FixtureResponseFixtureVenue
import com.api_football.models.FixtureResponseResponse
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Collections
import kotlin.math.max

val Database.fixtures get() = this.sequenceOf(Fixtures)
val Database.venues get() = this.sequenceOf(Venues)

data class Score(val home: Int?, val away: Int?)
data class GoalData(val halftime: Score, val fulltime: Score, val extratime: Score, val penalty: Score)

class DataImportService : Logging {
    companion object {
        var lastUpdate: LocalDateTime? = null
    }

    fun runDailyUpdate(): List<ImportFixtureResult> {
        logger().info("Running daily update")

        val database = Db().connection

        val existingVenues = findExistingVenues(database)

        val result = FootballApiService
            .getAllFixtures()
            .response
            .map { importFixture(database, it); }
            .toList()

        removeUnlistedFixtures(database, result.map { it.fixture.id })

        processNewVenues(database, existingVenues)

        lastUpdate = LocalDateTime.now()

        logger().info("Daily update complete")

        return result
    }

    fun runLiveUpdate(): List<ImportFixtureResult> {
        logger().info("Running live update")

        val database = Db().connection
        val liveFixtures = findLiveFixtures(database)

        logger().info("Matches currently live: {}", liveFixtures.map { it.id })

        val result = liveFixtures
            .map {
                val fixture = FootballApiService.getFixture(it.id)
                if (fixture.response.isEmpty()) {
                    throw ImportException("No data returned by API for fixture ${it.id}")
                }
                importFixture(database, fixture.response[0])
            }
            .toList()

        lastUpdate = LocalDateTime.now()

        logger().info("Live update complete")

        return result
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
            .orderBy(Fixtures.date.asc(), Fixtures.id.asc())
            .map { row -> Fixtures.createEntity(row) }
            .toList()
    }

    private fun processNewVenues(database: Database, existingVenues: List<Long>) {
        val venues = if (existingVenues.isEmpty()) {
            database.venues
                .filter { it.id greaterEq 0 }
                .toList()
        }
        else {
            database.venues
                .filter { it.id notInList existingVenues }
                .filter { it.id greaterEq 0 }
                .toList()
        }

        logger().debug("Updating venue data of {} venues with ids {}", venues.size, venues)

        venues.forEach { updateVenue(it) }
    }

    private fun updateVenue(entity: Venue) {
        val venue = FootballApiService.getVenue(entity.id)

        entity.name = venue.name
        entity.city = venue.city
        entity.country = venue.country ?: entity.country
        entity.capacity = venue.capacity ?: entity.capacity

        entity.flushChanges()

        Thread.sleep(10000)
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
            elapsed = fixtureResponse.fixture.status.elapsed
            goalsHalftimeHome = fixtureResponse.score.halftime?.home
            goalsHalftimeAway = fixtureResponse.score.halftime?.away
            goalsFulltimeHome = fixtureResponse.score.fulltime?.home
            goalsFulltimeAway = fixtureResponse.score.fulltime?.away
            goalsExtratimeHome = fixtureResponse.score.extratime?.home
            goalsExtratimeAway = fixtureResponse.score.extratime?.away
            goalsPenaltyHome = fixtureResponse.score.penalty?.home
            goalsPenaltyAway = fixtureResponse.score.penalty?.away
            eventsProcessed = 0
            venue = getVenue(database, fixtureResponse)
            announced = false
            pendingScoreChange = false
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

        val oldGoalData = createGoalData(entity)

        entity.goalsHalftimeHome = fixtureResponse.score.halftime?.home
        entity.goalsHalftimeAway = fixtureResponse.score.halftime?.away
        if (status == FixtureState.SECOND_HALF.code || status == FixtureState.BREAK_TIME.code) {
            entity.goalsFulltimeHome = fixtureResponse.goals.home
            entity.goalsFulltimeAway = fixtureResponse.goals.away
        }
        else {
            entity.goalsFulltimeHome = fixtureResponse.score.fulltime?.home
            entity.goalsFulltimeAway = fixtureResponse.score.fulltime?.away
        }
        entity.goalsExtratimeHome = add(fixtureResponse.score.fulltime?.home, fixtureResponse.score.extratime?.home)
        entity.goalsExtratimeAway = add(fixtureResponse.score.fulltime?.away, fixtureResponse.score.extratime?.away)
        entity.goalsPenaltyHome = fixtureResponse.score.penalty?.home
        entity.goalsPenaltyAway = fixtureResponse.score.penalty?.away

        val newGoalData = createGoalData(entity)

        logger().debug("Old goal data: {}", oldGoalData)
        logger().debug("New goal data: {}", newGoalData)

        val goalsChanged = oldGoalData != newGoalData
        val goalsReset = areGoalsReset(oldGoalData, newGoalData)

        logger().debug("Goals changed: {}", goalsChanged)
        logger().debug("Goals reset: {}", goalsReset)

        // We need to persist the value of goalsChanged because a Goal event might not be processed
        // at the same time as the score changes; this happens when a Goal event appears without
        // player data. Player data will be updated at a later time and then the Goal event
        // will be processed.
        logger().debug("Old value of pendingScoreChange: {}", entity.pendingScoreChange)
        entity.pendingScoreChange = entity.pendingScoreChange || goalsChanged
        logger().debug("New value of pendingScoreChange: {}", entity.pendingScoreChange)

        val stateChange = if (status != entity.status) {
            if (entity.endDate == null
                    && FixtureState.getByCode(entity.status)?.period != FixtureStatePeriod.PAST
                    && FixtureState.getByCode(status)?.period == FixtureStatePeriod.PAST) {
                entity.endDate = LocalDateTime.now()
            }
            processStateChange(entity.status, status, entity)
        }
        else {
            null
        }
        entity.status = status
        entity.elapsed = if (extraTimeHalfTimeFix(fixtureResponse)) {
            105
        }
        else {
            fixtureResponse.fixture.status?.elapsed ?: entity.elapsed
        }

        logger().debug("Number of events in database: {}, in API response: {}",
            entity.eventsProcessed, fixtureResponse.events?.size ?: 0)
        val eventsCount = max(fixtureResponse.events?.size ?: entity.eventsProcessed, entity.eventsProcessed)
        val newEvents = if (eventsCount > entity.eventsProcessed) {
            val unprocessedEvents = fixtureResponse
                .events!!
                .subList(entity.eventsProcessed, fixtureResponse.events.size)
            logger().debug("Identified {} unprocessed events: {}", unprocessedEvents.size, unprocessedEvents)
            val hasUnprocessableEvents = unprocessedEvents.any {
                val eventProcessable = isEventProcessable(fixtureResponse, entity.pendingScoreChange, it)
                logger().debug("Checked event {} whether it is processable; result: {}", it, eventProcessable)
                !eventProcessable
            }
            if (hasUnprocessableEvents) {
                logger().debug("Not processing any events as there is at least one that is currently not processable")
                emptyList<String>().toMutableList()
            }
            else {
                logger().debug("Processing all unprocessed events")
                unprocessedEvents
                    .mapNotNull { processEvent(fixtureResponse, entity, it) }
                    .toMutableList()
            }
        }
        else {
            emptyList<String>().toMutableList()
        }

        if (newEvents.isNotEmpty()) {
            entity.eventsProcessed = eventsCount
            entity.pendingScoreChange = false
        }

        if (goalsReset) {
            newEvents.add(createGoalsResetEvent(entity))
            entity.pendingScoreChange = false
        }

        entity.venue = getVenue(database, fixtureResponse)

        entity.flushChanges()

        return ImportFixtureResult(entity, Collections.unmodifiableList(newEvents), stateChange)
    }

    // Football API reports the break during with status HT and elapsed time 45
    private fun extraTimeHalfTimeFix(fixtureResponse: FixtureResponseResponse): Boolean =
        (fixtureResponse.score.extratime?.home != null || fixtureResponse.score.extratime?.away != null) &&
                fixtureResponse.fixture.status?.short?.value == FixtureState.HALF_TIME.code

    private fun add(a: Int?, b: Int?): Int? {
        if (a == null || b == null) {
            return null
        }
        return a + b
    }

    private fun areGoalsReset(oldGoalData: GoalData, newGoalData: GoalData): Boolean {
        return ((oldGoalData.halftime.home ?: 0) > (newGoalData.halftime.home ?: 0)) ||
                ((oldGoalData.halftime.away ?: 0) > (newGoalData.halftime.away ?: 0)) ||
                ((oldGoalData.fulltime.home ?: 0) > (newGoalData.fulltime.home ?: 0)) ||
                ((oldGoalData.fulltime.away ?: 0) > (newGoalData.fulltime.away ?: 0)) ||
                ((oldGoalData.extratime.home ?: 0) > (newGoalData.extratime.home ?: 0)) ||
                ((oldGoalData.extratime.away ?: 0) > (newGoalData.extratime.away ?: 0)) ||
                ((oldGoalData.penalty.home ?: 0) > (newGoalData.penalty.home ?: 0)) ||
                ((oldGoalData.penalty.away ?: 0) > (newGoalData.penalty.away ?: 0))
    }

    private fun createGoalData(entity: Fixture): GoalData = GoalData(
        Score(entity.goalsHalftimeHome, entity.goalsHalftimeAway),
        Score(entity.goalsFulltimeHome, entity.goalsFulltimeAway),
        Score(entity.goalsExtratimeHome, entity.goalsExtratimeAway),
        Score(entity.goalsPenaltyHome, entity.goalsPenaltyAway)
    )

    private fun isEventProcessable(fixtureResponse: FixtureResponseResponse, goalsChanged: Boolean, event: FixtureResponseEvents): Boolean {
        if (event.type == "Goal") {
            findPlayer(fixtureResponse, event) ?: return false
            return goalsChanged || event.detail == "Missed Penalty"
        }
        return true
    }

    private fun processStateChange(oldState: String, newState: String, entity: Fixture): String? {
        val transition = findMatchingTransition(oldState, newState) ?: return null
        val description = transition.description ?: return null

        if (transition.appendScore) {
            val score = MatchTitleService.formatMatchScore(entity)
            return "$description; Spielstand: $score"
        }
        return description
    }

    private fun findMatchingTransition(oldState: String, newState: String): FixtureStateTransition? {
        val oldFixtureState = FixtureState.getByCode(oldState)
        val newFixtureState = FixtureState.getByCode(newState)

        return FixtureStateTransition
            .values()
            .firstOrNull {
                (it.oldState == oldFixtureState && it.newState == newFixtureState)
                        || (it.oldState == null && it.newState == newFixtureState)
                        || (it.oldState == oldFixtureState && it.newState == null)
            }
    }

    private fun processEvent(fixtureResponse: FixtureResponseResponse, entity: Fixture, event: FixtureResponseEvents): String? {
        logger().debug("Processing event: {}", event)

        val message = if (event.type == "Goal") {
            val type = when (event.detail) {
                "Normal Goal" -> "Tor"
                "Own Goal" -> "Eigentor"
                "Penalty" -> "Elfmetertreffer"
                "Missed Penalty" -> "Vergebener Elfmeter"
                else -> event.detail
            }
            val time = when (val elapsed = event.time?.elapsed) {
                null -> ""
                else -> " in Spielminute $elapsed"
            }
            val team = TeamMapper.mapTeamName(event.team?.name ?: "unbekannt")
            val player = when (val playerName = findPlayer(fixtureResponse, event)) {
                null -> ""
                else -> " durch $playerName"
            }
            val score = MatchTitleService.formatMatchScore(entity)

            "$type$time für $team$player; Spielstand: $score"
        }
        else {
            null
        }

        logger().debug("Message created from event: {}", message)
        return message
    }

    private fun createGoalsResetEvent(entity: Fixture): String {
        val score = MatchTitleService.formatMatchScore(entity)
        val message = "Spielstand wurde korrigiert; neuer Spielstand: $score"

        logger().debug("Created new message: {}", message)
        return message
    }

    private fun findPlayer(fixtureResponse: FixtureResponseResponse, event: FixtureResponseEvents): String? {
        val fallbackName = event.player?.name
        val playerId = event.player?.id ?: return fallbackName
        fixtureResponse.players ?: return fallbackName

        return fixtureResponse
            .players
            .flatMap { it.players ?: emptyList() }
            .firstOrNull { it.player?.id == playerId }
            ?.player
            ?.name ?: fallbackName
    }

    private fun removeUnlistedFixtures(database: Database, importedFixtures: List<Long>) {
        val configuration = ConfigurationProvider.getConfiguration()
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

    fun setFixturesToAnnounced(fixtures: List<Fixture>) {
        val database = Db().connection

        fixtures.forEach { fixture ->
            val entity = database.fixtures.find { it.id eq fixture.id }
            entity?.announced = true
            entity?.flushChanges()
        }
    }
}

class ImportException(message: String) : Exception(message)

data class ImportFixtureResult(
    val fixture: Fixture,
    val newEvents: List<String>,
    val stateChange: String?
)
