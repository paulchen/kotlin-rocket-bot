package at.rueckgr.kotlin.rocketbot.util.time

import spock.lang.Specification

import java.time.LocalDateTime

class DateTimeParserTest extends Specification {
    def "parse"() {
        given:
            def p = new DateTimeParser()

        when:
            def result = p.parse(dateString, referenceTime)

        then:
            result == expectedResult

        where:
            dateString            | referenceTime                         | expectedResult
            "1.2.2022"            | LocalDateTime.now()                   | LocalDateTime.of(2022, 2, 1, 0, 0, 0, 0)
            "2022-02-01"          | LocalDateTime.now()                   | LocalDateTime.of(2022, 2, 1, 0, 0, 0, 0)
            "1.2."                | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 2, 1, 0, 0, 0, 0)
            "1.1."                | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2023, 1, 1, 0, 0, 0, 0)
            "27.1."               | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2023, 1, 27, 0, 0, 0, 0)
            "28.1."               | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 28, 0, 0, 0, 0)
            "00:00"               | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 28, 0, 0, 0, 0)
            "09:16"               | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 27, 9, 16, 0, 0)
            "09:16"               | LocalDateTime.of(2022, 1, 27, 10, 16) | LocalDateTime.of(2022, 1, 28, 9, 16, 0, 0)
            "22:22"               | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 27, 22, 22, 0, 0)
            "24:00"               | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 28, 0, 0, 0, 0)
            "26:00"               | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 27, 2, 0, 0, 0)
            "26:00"               | LocalDateTime.of(2022, 1, 27, 2, 16)  | LocalDateTime.of(2022, 1, 28, 2, 0, 0, 0)
            "26:00:16"            | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 27, 2, 0, 16, 0)
            "26:00:16"            | LocalDateTime.of(2022, 1, 27, 2, 16)  | LocalDateTime.of(2022, 1, 28, 2, 0, 16, 0)
            "2022-02-01 15:22"    | LocalDateTime.now()                   | LocalDateTime.of(2022, 2, 1, 15, 22, 0, 0)
            "2022-02-01 15:22:46" | LocalDateTime.now()                   | LocalDateTime.of(2022, 2, 1, 15, 22, 46, 0)
            "2022-02-01 26:22"    | LocalDateTime.now()                   | LocalDateTime.of(2022, 2, 2, 2, 22, 0, 0)
            "1.2.2022 15:22"      | LocalDateTime.now()                   | LocalDateTime.of(2022, 2, 1, 15, 22, 0, 0)
            "1.2.2022 15:22:46"   | LocalDateTime.now()                   | LocalDateTime.of(2022, 2, 1, 15, 22, 46, 0)
            "1.2. 02:26"          | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 2, 1, 2, 26, 0, 0)
            "1.1. 02:26"          | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2023, 1, 1, 2, 26, 0, 0)
            "31.12. 02:26"        | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 12, 31, 2, 26, 0, 0)
            "31.12. 26:26"        | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2023, 1, 1, 2, 26, 0, 0)
    }
}
