package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.database.FixtureImpl
import spock.lang.Specification

import java.time.LocalDateTime

class MatchTitleServiceTest extends Specification {
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
