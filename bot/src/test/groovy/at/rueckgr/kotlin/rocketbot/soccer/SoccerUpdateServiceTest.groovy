package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.util.FixtureFactory
import spock.lang.Specification

class SoccerUpdateServiceTest extends Specification {
    def "createMessage"() {
        given:
            def service = new SoccerUpdateService()
            def fixture = FixtureFactory.createFixture("Al Bayt Stadium", "Al Khor", "Qatar", "Ecuador")

        when:
            def result = service.createMessage(fixture, "roomName", "test message", "user")

        then:
            result.roomId == null
            result.roomName == "roomName"
            result.message == ":mega: :flag_qa:\u00a0*Katar*\u00a0-\u00a0:flag_ec:\u00a0*Ecuador*: test message"
            result.emoji == ":soccer:"
            result.username == "user"
    }
}
