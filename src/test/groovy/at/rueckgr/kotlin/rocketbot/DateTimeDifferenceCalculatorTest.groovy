package at.rueckgr.kotlin.rocketbot

import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DateTimeDifferenceCalculatorTest extends Specification {
    def "DateTimeDifferenceCalculator"(year1, month1, day1, hour1, minute1, second1, year2, month2, day2, hour2, minute2, second2, expectedResult) {
        given:
            DateTimeDifferenceCalculator d = new DateTimeDifferenceCalculator()

            def from = LocalDateTime.of(LocalDate.of(year1, month1, day1), LocalTime.of(hour1, minute1, second1))
            def to = LocalDateTime.of(LocalDate.of(year2, month2, day2), LocalTime.of(hour2, minute2, second2))

        when:
            // TODO add test for third parameter of formatTimeDifference()
            def result = d.formatTimeDifference(from, to, [])

        then:
            result == expectedResult

        where:
            year1 | month1 | day1 | hour1 | minute1 | second1 | year2 | month2 | day2 | hour2 | minute2 | second2 | expectedResult
            2021  | 7      | 12   | 20    | 56      | 16      | 2021  | 7      | 12   | 20    | 56      | 17      | "1 second from now"
            2021  | 7      | 12   | 20    | 56      | 16      | 2021  | 7      | 12   | 20    | 56      | 15      | "1 second ago"
            2021  | 7      | 12   | 20    | 56      | 16      | 2021  | 7      | 12   | 20    | 56      | 18      | "2 seconds from now"
            2021  | 7      | 12   | 20    | 56      | 16      | 2021  | 7      | 12   | 20    | 57      | 18      | "1 minute and 2 seconds from now"
            2021  | 7      | 12   | 20    | 56      | 16      | 2022  | 9      | 14   | 22    | 59      | 35      | "1 year, 2 months, 2 days, 2 hours, 3 minutes, and 19 seconds from now"
            2022  | 9      | 14   | 22    | 59      | 35      | 2021  | 7      | 12   | 20    | 56      | 16      | "1 year, 2 months, 2 days, 2 hours, 3 minutes, and 19 seconds ago"
            2021  | 7      | 12   | 20    | 56      | 16      | 2024  | 5      | 8    | 16    | 37      | 4       | "2 years, 9 months, 25 days, 19 hours, 40 minutes, and 48 seconds from now"
    }
}
