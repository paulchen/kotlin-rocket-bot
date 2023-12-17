package at.rueckgr.kotlin.rocketbot

import spock.lang.Specification

class MessageHandlerTest extends Specification {
    def "removeQuotes"(message, expectedResult) {
        given:
            def handler = new MessageHandler()

        when:
            def result = handler.removeQuotes(message)

        then:
            result == expectedResult

        where:
            message                    | expectedResult
            ""                         | ""
            "blah"                     | "blah"
            "[ ](wtf) foo"             | "foo"
            "[ ](foo) [ ](bar) qwertz" | "qwertz"
    }
}
