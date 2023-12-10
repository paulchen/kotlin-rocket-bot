
package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.EventHandler
import at.rueckgr.kotlin.rocketbot.util.time.TimeUnit
import spock.lang.Specification

import java.time.LocalDateTime

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

    def testParseTimespecSuccess(input, referenceTime, nextNotification, notifyInterval, notifyUnit) {
        given:
            def plugin = new RemindPlugin()

        when:
            def timespec = plugin.parseTimespec(input, referenceTime)

        then:
            timespec.nextNotification == nextNotification
            timespec.notifyInterval == notifyInterval
            timespec.notifyUnit == notifyUnit

        where:
            input              | referenceTime                          | nextNotification                       | notifyInterval | notifyUnit
            "in 10 min"        | LocalDateTime.of(2023, 12, 10, 17, 22) | LocalDateTime.of(2023, 12, 10, 17, 32) | null           | null
            "in 10min"         | LocalDateTime.of(2023, 12, 10, 17, 22) | LocalDateTime.of(2023, 12, 10, 17, 32) | null           | null
            "every 25 min"     | LocalDateTime.of(2023, 12, 7, 14, 47)  | LocalDateTime.of(2023, 12, 7, 15, 12)  | 25L            | TimeUnit.MINUTE
            "every 25min"      | LocalDateTime.of(2023, 12, 7, 14, 47)  | LocalDateTime.of(2023, 12, 7, 15, 12)  | 25L            | TimeUnit.MINUTE
            "every 25 minutes" | LocalDateTime.of(2023, 12, 7, 14, 47)  | LocalDateTime.of(2023, 12, 7, 15, 12)  | 25L            | TimeUnit.MINUTE
            "at 12:26"         | LocalDateTime.of(2023, 12, 5, 17, 56)  | LocalDateTime.of(2023, 12, 6, 12, 26)  | null           | null
    }

    def testParseTimespecFailure(input) {
        given:
            def plugin = new RemindPlugin()

        when:
            def timespec = plugin.parseTimespec(input, LocalDateTime.now())

        then:
            timespec == null

        where:
            input           |_
            "wtf"           |_
            "in 10 wtf"     |_
            "in xy"         |_
            "in 0h"         |_
            "every 10 foo"  |_
            "every asdf"    |_
            "at xy"         |_
            "at 12345"      |_
            "at 2023-12"    |_
    }
}
