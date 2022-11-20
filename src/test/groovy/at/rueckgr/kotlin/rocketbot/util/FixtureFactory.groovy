package at.rueckgr.kotlin.rocketbot.util

import at.rueckgr.kotlin.rocketbot.database.FixtureImpl
import at.rueckgr.kotlin.rocketbot.database.VenueImpl

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FixtureFactory {
    static def createFixture(venueName, venueCity, home, away) {
        def venue = new VenueImpl(0L, venueName, venueCity, null, null)
        def date = LocalDateTime.of(LocalDate.of(2022, 11, 20), LocalTime.of(17, 0))

        return new FixtureImpl(0L, 0L, 0, date, "", home, away, "", null, null, null, null, null, null, null, null, null, 0, venue, null, false, false)
    }
}
