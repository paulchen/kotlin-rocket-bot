package at.rueckgr.kotlin.rocketbot.plugins

import spock.lang.Specification

class NicePluginTest extends Specification {
    def "extractNumbers"() {
        given:
            def n = new NicePlugin()

        when:
            def result = n.extractNumbers(message)

        then:
            result == expectedResult

        where:
            message       | expectedResult
            ""            | []
            "1"           | [ 1 ]
            "1 2"         | [ 1, 2 ]
            "abc1def2ghi" | [ 1, 2 ]
    }

    def "containsNiceNumbers"() {
        given:
            def n = new NicePlugin()

        when:
            def result = n.containsNiceNumbers(message)

        then:
            result == expectedResult

        where:
            message              | expectedResult
            ""                   | false
            "1"                  | false
            "1 2"                | false
            "abc1def2ghi"        | false
            "abc16def27ghi26jkl" | true
    }
}