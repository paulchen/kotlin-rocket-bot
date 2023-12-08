
package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.EventHandler
import spock.lang.Specification

class RemindPluginTest extends Specification {
    def testSplitRemindMessageSuccess(message, username, subject, timespec) {
        given:
            def messageObject = new EventHandler.Message(message, false)
            def plugin = new RemindPlugin()

        when:
            def result = plugin.splitRemindMessage(messageObject)

        then:
            result.targetUsername == username
            result.subject == subject
            result.timespec == timespec

        where:
            message                                       | username | subject            | timespec
            "!remind abc about def in 10min"              | "abc"    | "def"              | "in 10min"
            "!remind abc about xyz about qwertz in 10min" | "abc"    | "xyz about qwertz" | "in 10min"
            "!remind abc about def in xyz in 10min"       | "abc"    | "def in xyz"       | "in 10min"
            "!remind about about about in in 10min"       | "about"  | "about in"         | "in 10min"
    }

    def testSplitRemindMessageFailure(message) {
        given:
            def messageObject = new EventHandler.Message(message, false)
            def plugin = new RemindPlugin()

        when:
            plugin.splitRemindMessage(messageObject)

        then:
            def e = thrown(RemindException)
            e.message == "Invalid format of input"

        where:
            message                    |_
            "wtf"                      |_
            "!remind abc"              |_
            "!remind abc about x"      |_
            "!remind abc in 10min"     |_
            "about x in 10min"         |_
            "!remind about in"         |_
            "!remind x about in"       |_
            "!remind about x in 10min" |_

    }
}
