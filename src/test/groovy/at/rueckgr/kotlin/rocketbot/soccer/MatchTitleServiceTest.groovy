package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.database.FixtureImpl
import at.rueckgr.kotlin.rocketbot.database.VenueImpl
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MatchTitleServiceTest extends Specification {
    private def createFixture(venueName, venueCity, home, away) {
        def venue = new VenueImpl(0L, venueName, venueCity, null, null)
        def date = LocalDateTime.of(LocalDate.of(2022, 11, 20), LocalTime.of(17, 0))

        return new FixtureImpl(0L, 0L, 0, date, "", home, away, "", null, null, null, null, null, null, null, null, null, 0, venue, null, false, false)
    }

    def "FormatMatchTitleShort"() {
        given:
            def fixture = createFixture("Al Bayt Stadium", "Al Khor", "Qatar", "Ecuador")

        when:
            def result = MatchTitleService.INSTANCE.formatMatchTitleShort(fixture)

        then:
            result == ":qa:\u00a0*Katar*\u00a0-\u00a0:ec:\u00a0*Ecuador*"
    }

    def "FormatMatchTitle"() {
        given:
            def fixture = createFixture("Al Bayt Stadium", "Al Khor", "Qatar", "Ecuador")

        when:
            def result = MatchTitleService.INSTANCE.formatMatchTitle(fixture)

        then:
            result == "20.11.2022 17\ufeff:\ufeff00: :qa:\u00a0*Katar*\u00a0-\u00a0:ec:\u00a0*Ecuador* (Al Bayt Stadium, Al Khor)"
    }

    def "FormatGameScore"(htHome, htAway, ftHome, ftAway, etHome, etAway, pHome, pAway, expectedResult) {
        given:
            def fixture = new FixtureImpl(0L, 0L, 0, LocalDateTime.now(), "", "", "", "", null, htHome, htAway, ftHome, ftAway, etHome, etAway, pHome, pAway, 0, null, null, false, false)

        when:
            def result = MatchTitleService.INSTANCE.formatMatchScore(fixture)

        then:
            result == expectedResult?.replaceAll(":", "\ufeff:\ufeff")

        where:
        htHome | htAway | ftHome | ftAway | etHome | etAway | pHome | pAway | expectedResult
        null   | null   | null   | null   | null   | null   | null  | null  | null
        0      | 0      | null   | null   | null   | null   | null  | null  | "0:0"
        2      | 1      | null   | null   | null   | null   | null  | null  | "2:1"
        0      | 0      | 0      | 0      | null   | null   | null  | null  | "0:0"
        0      | 0      | 1      | 0      | null   | null   | null  | null  | "1:0 (0:0)"
        1      | 0      | 1      | 0      | null   | null   | null  | null  | "1:0 (1:0)"
        0      | 0      | 0      | 0      | 0      | 0      | null  | null  | "0:0 n.V. (0:0)"
        0      | 0      | 0      | 0      | 3      | 0      | null  | null  | "3:0 n.V. (0:0)"
        0      | 0      | 1      | 1      | 3      | 1      | null  | null  | "3:1 n.V. (1:1, 0:0)"
        1      | 1      | 1      | 1      | 3      | 1      | null  | null  | "3:1 n.V. (1:1, 1:1)"
        0      | 0      | 0      | 0      | 0      | 0      | 5     | 3     | "0:0 n.V. (0:0), 5:3 i.E."
        0      | 0      | 0      | 0      | 3      | 3      | 5     | 3     | "3:3 n.V. (0:0), 5:3 i.E."
        0      | 0      | 1      | 1      | 3      | 3      | 5     | 3     | "3:3 n.V. (1:1, 0:0), 5:3 i.E."
        1      | 1      | 1      | 1      | 3      | 3      | 5     | 3     | "3:3 n.V. (1:1, 1:1), 5:3 i.E."
    }
}
