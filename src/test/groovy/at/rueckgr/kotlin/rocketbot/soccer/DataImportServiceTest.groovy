package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.util.FixtureFactory
import com.api_football.models.FixtureResponseResponseInner
import com.api_football.models.FixtureResponseResponseInnerEventsInner
import com.api_football.models.FixtureResponseResponseInnerEventsInnerTime
import com.api_football.models.FixtureResponseResponseInnerFixture
import com.api_football.models.FixtureResponseResponseInnerFixturePeriods
import com.api_football.models.FixtureResponseResponseInnerFixtureStatus
import com.api_football.models.FixtureResponseResponseInnerFixtureVenue
import com.api_football.models.FixtureResponseResponseInnerLeague
import com.api_football.models.FixtureResponseResponseInnerScore
import com.api_football.models.FixtureResponseResponseInnerTeams
import com.api_football.models.GoalsShort
import com.api_football.models.Player
import com.api_football.models.TeamShort
import spock.lang.Specification

class DataImportServiceTest extends Specification {
    def "processEvent"() {
        given:
            def service = new DataImportService()
            def fixtureResponse = new FixtureResponseResponseInner(
                    new FixtureResponseResponseInnerFixture(
                            0L, null, null, null, null,
                            new FixtureResponseResponseInnerFixturePeriods(null, null),
                            new FixtureResponseResponseInnerFixtureVenue(null, null, null),
                            new FixtureResponseResponseInnerFixtureStatus(null, null, null)
                    ),
                    new FixtureResponseResponseInnerLeague(null, null, null, null, null, null, null),
                    new FixtureResponseResponseInnerTeams(null, null),
                    new GoalsShort(null, null),
                    new FixtureResponseResponseInnerScore(null, null, null, null),
                    null, null, null, null)
            def fixture = FixtureFactory.createFixture("Al Bayt Stadium", "Al Khor", "Qatar", "Ecuador")
            fixture.goalsHalftimeHome = 0
            fixture.goalsHalftimeAway = 1
            def event = new FixtureResponseResponseInnerEventsInner(
                    new FixtureResponseResponseInnerEventsInnerTime(16, null),
                    new TeamShort(0L, "Ecuador", null, null, null),
                    new Player(0L, "Enner Valencia", null, null, null),
                    null, "Goal", "Penalty", null)

        when:
            def message = service.processEvent(fixtureResponse, fixture, event)

        then:
            message == "Elfmetertreffer in Spielminute 16 für :ec:\u00a0*Ecuador* durch Enner Valencia; Spielstand: 0\ufeff:\ufeff1"
    }
}
