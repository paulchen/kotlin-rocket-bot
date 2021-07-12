package at.rueckgr.kotlin.rocketbot

import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DateTimeDifferenceCalculatorTest extends Specification {
    def "CalculateTimeDifference"() {
        given:
        DateTimeDifferenceCalculator d = new DateTimeDifferenceCalculator()

            from = LocalDateTime.of(LocalDate.of(2021, 7, 12), LocalTime.of(17, 22, 36))
            to = LocalDateTime.of(LocalDate.of(2022, 9, 16), LocalTime.of(19, 23, 42))

        when:
            result = d.calculateTimeDifference(from, to)

        then:
            result == [
                    (DateTimeDifferenceCalculator.TimeUnit.YEAR): 1,
                    (DateTimeDifferenceCalculator.TimeUnit.MONTH): 2,
                    (DateTimeDifferenceCalculator.TimeUnit.DAY): 4,
                    (DateTimeDifferenceCalculator.TimeUnit.HOUR): 2,
                    (DateTimeDifferenceCalculator.TimeUnit.MINUTE): 1,
                    (DateTimeDifferenceCalculator.TimeUnit.SECOND): 6,
            ]
    }
}
