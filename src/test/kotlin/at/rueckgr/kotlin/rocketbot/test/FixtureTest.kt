package at.rueckgr.kotlin.rocketbot.test

import at.rueckgr.kotlin.rocketbot.soccer.DataImportService
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import com.api_football.infrastructure.ApiClient
import org.assertj.core.api.Assertions.assertThat

import org.junit.jupiter.api.Test
import org.ktorm.database.Database
import org.ktorm.support.mysql.MySqlDialect

abstract class FixtureTest(
    private val fixtureDirectory: String,
    private val expectedStateChanges: List<Pair<Int, String>>,
    private val expectedEvents: List<Pair<Int, String>>
) {
    @Test
    fun testFixture() {
        val mockServer = FixtureMockServer(fixtureDirectory)
        val port = mockServer.start()

        ConfigurationProvider.loadConfiguration({}.javaClass.getResource("/kotlin-rocket-bot.yaml").path)
        System.setProperty(ApiClient.baseUrlKey, "http://localhost:$port")

        val database = setUpDatabase()

        var iteration = 0
        val stateChanges = mutableListOf<Pair<Int, String>>()
        val events = mutableListOf<Pair<Int, String>>()
        while(mockServer.hasMoreResponses()) {
            val (_, newEvents, stateChange) = DataImportService().importFixture(database, 979139L)

            if (stateChange != null) {
                stateChanges.add(Pair(iteration, stateChange))
            }
            newEvents.forEach {
                events.add(Pair(iteration, it))
            }

            iteration++
        }

//        assertSame(stateChanges, expectedStateChanges)
        assertSame(events, expectedEvents)
    }

    private fun assertSame(list: List<Pair<Int, String>>, expectedList: List<Pair<Int, String>>) {
        // just for making error tracing easier
        assertThat(list).hasSameSizeAs(expectedList)
        list.zip(expectedList) { item, expectedItem -> assertThat(item).isEqualTo(expectedItem) }
    }

    private fun setUpDatabase(): Database {
        val database = Database.connect(
            url = "jdbc:h2:mem:kotlin-rocket-bot;DB_CLOSE_DELAY=-1",
            dialect = MySqlDialect()
        )

        val sqlStatements = String({}.javaClass.getResourceAsStream("/schema.sql").readAllBytes()).split(";")
        database.useConnection { conn ->
            val statement = conn.createStatement()
            sqlStatements.forEach { sql ->
                statement.execute(sql)
            }
            statement.close()
        }

        return database
    }
}
