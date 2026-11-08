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
            "9:16"                | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 27, 9, 16, 0, 0)
            "09:16"               | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 27, 9, 16, 0, 0)
            "9:16"                | LocalDateTime.of(2022, 1, 27, 10, 16) | LocalDateTime.of(2022, 1, 28, 9, 16, 0, 0)
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
            "1.2. 2:26"           | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 2, 1, 2, 26, 0, 0)
            "1.2. 02:26"          | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 2, 1, 2, 26, 0, 0)
            "1.2. 15:22:46"       | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 2, 1, 15, 22, 46, 0)
            "1.1. 2:26"           | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2023, 1, 1, 2, 26, 0, 0)
            "1.1. 02:26"          | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2023, 1, 1, 2, 26, 0, 0)
            "31.12. 2:26"         | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 12, 31, 2, 26, 0, 0)
            "31.12. 02:26"        | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 12, 31, 2, 26, 0, 0)
            "31.12. 26:26"        | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2023, 1, 1, 2, 26, 0, 0)
    }
    def "parse next February 29 without a year"() {
        expect:
            new DateTimeParser().parse(dateString, referenceTime) == expectedResult

        where:
            dateString          | referenceTime                             | expectedResult
            "29.2."             | LocalDateTime.of(2027, 1, 1, 0, 0)        | LocalDateTime.of(2028, 2, 29, 0, 0)
            "29.02."            | LocalDateTime.of(2027, 3, 1, 0, 0)        | LocalDateTime.of(2028, 2, 29, 0, 0)
            "29.2."             | LocalDateTime.of(2028, 2, 28, 12, 0)      | LocalDateTime.of(2028, 2, 29, 0, 0)
            "29.2."             | LocalDateTime.of(2028, 2, 29, 0, 0)       | LocalDateTime.of(2028, 2, 29, 0, 0)
            "29.2."             | LocalDateTime.of(2028, 2, 29, 0, 1)       | LocalDateTime.of(2032, 2, 29, 0, 0)
            "29.2."             | LocalDateTime.of(2028, 3, 1, 0, 0)        | LocalDateTime.of(2032, 2, 29, 0, 0)
            "29.2."             | LocalDateTime.of(2097, 1, 1, 0, 0)        | LocalDateTime.of(2104, 2, 29, 0, 0)
            "29.2."             | LocalDateTime.of(2099, 1, 1, 0, 0)        | LocalDateTime.of(2104, 2, 29, 0, 0)
            "29.2. 15:22"       | LocalDateTime.of(2027, 1, 1, 0, 0)        | LocalDateTime.of(2028, 2, 29, 15, 22)
            "29.02. 05:22:46"   | LocalDateTime.of(2027, 3, 1, 0, 0)        | LocalDateTime.of(2028, 2, 29, 5, 22, 46)
            "29.2. 15:22"       | LocalDateTime.of(2028, 2, 29, 15, 21)     | LocalDateTime.of(2028, 2, 29, 15, 22)
            "29.2. 15:22"       | LocalDateTime.of(2028, 2, 29, 15, 22)     | LocalDateTime.of(2028, 2, 29, 15, 22)
            "29.2. 15:22"       | LocalDateTime.of(2028, 2, 29, 15, 23)     | LocalDateTime.of(2032, 2, 29, 15, 22)
            "29.2. 15:22:46"    | LocalDateTime.of(2028, 2, 29, 15, 22, 46) | LocalDateTime.of(2028, 2, 29, 15, 22, 46)
            "29.2. 15:22:46"    | LocalDateTime.of(2028, 2, 29, 15, 22, 47) | LocalDateTime.of(2032, 2, 29, 15, 22, 46)
            "29.2. 15:22"       | LocalDateTime.of(2099, 1, 1, 0, 0)        | LocalDateTime.of(2104, 2, 29, 15, 22)
            "29.2. 26:00"       | LocalDateTime.of(2027, 1, 1, 0, 0)        | LocalDateTime.of(2028, 3, 1, 2, 0)
    }

    def "preserve lenient parsing of other dates"() {
        expect:
            new DateTimeParser().parse(dateString, LocalDateTime.of(2026, 11, 8, 12, 0)) == expectedResult

        where:
            dateString           | expectedResult
            "32.12."             | LocalDateTime.of(2027, 1, 1, 0, 0)
            "32.12.2026"         | LocalDateTime.of(2027, 1, 1, 0, 0)
            "32.12. 15:22"       | LocalDateTime.of(2027, 1, 1, 15, 22)
            "31.4."              | LocalDateTime.of(2027, 5, 1, 0, 0)
            "29.2.2027"          | LocalDateTime.of(2027, 3, 1, 0, 0)
            "29.2.2027 15:22:46" | LocalDateTime.of(2027, 3, 1, 15, 22, 46)
    }

}
