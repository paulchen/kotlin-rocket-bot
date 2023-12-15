package at.rueckgr.kotlin.rocketbot.util.time

import spock.lang.Specification

import java.time.LocalDateTime

class DateTimeParserTest extends Specification {
    def "parseDay"() {
        given:
            def p = new DateTimeParser()

        when:
            def result = p.parseDay(pattern, dateString, LocalDateTime.now())

        then:
            result == expectedResult

        where:
            dateString   | pattern      | expectedResult
            "1.2.2022"   | "d.M.yyyy"   | LocalDateTime.of(2022, 2, 1, 0, 0, 0, 0)
            "2022-02-01" | "yyyy-MM-dd" | LocalDateTime.of(2022, 2, 1, 0, 0, 0, 0)
    }

    def "parseDayWithoutYear"() {
        given:
            def p = new DateTimeParser()

        when:
            def result = p.parseDayWithoutYear(pattern, dateString, referenceTime)

        then:
            result == expectedResult

        where:
            dateString | pattern    | referenceTime                        | expectedResult
            "1.2."     | "d.M.yyyy" | LocalDateTime.of(2022, 1, 27, 0, 16) | LocalDateTime.of(2022, 2, 1, 0, 0, 0, 0)
            "1.1."     | "d.M.yyyy" | LocalDateTime.of(2022, 1, 27, 0, 16) | LocalDateTime.of(2023, 1, 1, 0, 0, 0, 0)
            "27.1."    | "d.M.yyyy" | LocalDateTime.of(2022, 1, 27, 0, 16) | LocalDateTime.of(2023, 1, 27, 0, 0, 0, 0)
            "28.1."    | "d.M.yyyy" | LocalDateTime.of(2022, 1, 27, 0, 16) | LocalDateTime.of(2022, 1, 28, 0, 0, 0, 0)
    }

    def "parseTime"() {
        given:
            def p = new DateTimeParser()

        when:
            def result = p.parseTime(pattern, dateString, referenceTime)

        then:
            result == expectedResult

        where:
            dateString | pattern    | referenceTime                         | expectedResult
            "00:00"    | "HH:mm"    | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 28, 0, 0, 0, 0)
            "09:16"    | "HH:mm"    | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 27, 9, 16, 0, 0)
            "09:16"    | "HH:mm"    | LocalDateTime.of(2022, 1, 27, 10, 16) | LocalDateTime.of(2022, 1, 28, 9, 16, 0, 0)
            "22:22"    | "HH:mm"    | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 27, 22, 22, 0, 0)
            "24:00"    | "HH:mm"    | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 28, 0, 0, 0, 0)
            "26:00"    | "HH:mm"    | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 27, 2, 0, 0, 0)
            "26:00"    | "HH:mm"    | LocalDateTime.of(2022, 1, 27, 2, 16)  | LocalDateTime.of(2022, 1, 28, 2, 0, 0, 0)
            "26:00:16" | "HH:mm:ss" | LocalDateTime.of(2022, 1, 27, 0, 16)  | LocalDateTime.of(2022, 1, 27, 2, 0, 16, 0)
            "26:00:16" | "HH:mm:ss" | LocalDateTime.of(2022, 1, 27, 2, 16)  | LocalDateTime.of(2022, 1, 28, 2, 0, 16, 0)
    }

    def "parseDateTime"() {
        given:
            def p = new DateTimeParser()

        when:
            def result = p.parseDateTime(pattern, dateString, LocalDateTime.now())

        then:
            result == expectedResult

        where:
            dateString            | pattern               | expectedResult
            "2022-02-01 15:22"    | "yyyy-MM-dd HH:mm"    | LocalDateTime.of(2022, 2, 1, 15, 22, 0, 0)
            "2022-02-01 15:22:46" | "yyyy-MM-dd HH:mm:ss" | LocalDateTime.of(2022, 2, 1, 15, 22, 46, 0)
            "2022-02-01 26:22"    | "yyyy-MM-dd HH:mm"    | LocalDateTime.of(2022, 2, 2, 2, 22, 0, 0)
            "1.2.2022 15:22"      | "d.M.yyyy HH:mm"      | LocalDateTime.of(2022, 2, 1, 15, 22, 0, 0)
            "1.2.2022 15:22:46"   | "d.M.yyyy HH:mm:ss"   | LocalDateTime.of(2022, 2, 1, 15, 22, 46, 0)
    }

    def "parseDateTimeWithoutYear"() {
        given:
            def p = new DateTimeParser()

        when:
            def result = p.parseDateTimeWithoutYear(pattern, dateString, referenceTime)

        then:
            result == expectedResult

        where:
            dateString     | pattern          | referenceTime                        | expectedResult
            "1.2. 02:26"   | "d.M.yyyy HH:mm" | LocalDateTime.of(2022, 1, 27, 0, 16) | LocalDateTime.of(2022, 2, 1, 2, 26, 0, 0)
            "1.1. 02:26"   | "d.M.yyyy HH:mm" | LocalDateTime.of(2022, 1, 27, 0, 16) | LocalDateTime.of(2023, 1, 1, 2, 26, 0, 0)
            "31.12. 02:26" | "d.M.yyyy HH:mm" | LocalDateTime.of(2022, 1, 27, 0, 16) | LocalDateTime.of(2022, 12, 31, 2, 26, 0, 0)
            "31.12. 26:26" | "d.M.yyyy HH:mm" | LocalDateTime.of(2022, 1, 27, 0, 16) | LocalDateTime.of(2023, 1, 1, 2, 26, 0, 0)
    }
}
