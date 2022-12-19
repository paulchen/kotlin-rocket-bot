package at.rueckgr.kotlin.rocketbot.test

import at.rueckgr.kotlin.rocketbot.soccer.DataImportService
import at.rueckgr.kotlin.rocketbot.util.Db

import org.junit.jupiter.api.Test

class WorldCup2022FinalTest {
    @Test
    fun testFixture() {
        val mockServer = FixtureMockServer("/example-football-api-responses/fixture-979139")
        mockServer.start()

        // TODO set up H2 in-memory database
        val database = Db().connection
        var iteration = 0
        while(mockServer.hasMoreResponses()) {
            val (fixture, newEvents, stateChange) = DataImportService().importFixture(database, 979139L)
            // TODO collect events, collect state changes

            iteration++
        }

        // TODO verify events and state changes
    }
}
