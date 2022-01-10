package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Db
import org.ktorm.dsl.eq
import org.ktorm.entity.find

fun main() {
    ConfigurationProvider.instance.loadConfiguration("/config/kotlin-rocket-bot.yaml")

//    DataImportService().runDailyUpdate()
    DataImportService().runLiveUpdate()

//    val fixture = Db().connection.fixtures.find { it.id eq 708775 }
//    println(MatchTitleService().formatMatchTitle(fixture!!))

    // TODO stuff yet to be implemented:
    //  - DataImportService.runLiveUpdate() for updating games that will start shortly, are currently live or have just ended
    //    - check for a change of fixture state
    //      - send notifications about certain types of changes
    //    - check for unprocessed events
    //      - send notifications about certain types of events
    //        - especially goals; in case of a goal, provide new score
    //  - put a scheduler in place
    //    - run the daily update everyday (at 3am?) and during bot startup
    //    - run the live update when at least a game is currently live, one hour before a game will start and one hour after a game has ended
    //      - record the timestamp in the Fixture table when a game has actually reached a final state

//    val configuration = ConfigurationProvider.instance.getConfiguration()
//    val soccerConfiguration = configuration.plugins?.soccer!!
//    ApiClient.apiKey.put("x-apisports-key", soccerConfiguration.apiKey!!)
//    val fixtures = FootballApi()
//        .getFixtures(713474, null, null, null, null, null, null, null, null, null, null, null, null)
//    println(fixtures)

//    val fixtureRounds = FootballApi()
//        .getFixturesRounds(soccerConfiguration.leagueId, soccerConfiguration.season, false)
//    println(fixtureRounds)

//    val databaseConfiguration = configuration.database
//    val database = Database.connect(
//        url = "jdbc:postgresql://${databaseConfiguration!!.host}/${databaseConfiguration.database}",
//        user = databaseConfiguration.username,
//        password = databaseConfiguration.password)
//
//    for (row in database.from(Fixture).select()) {
//        println(row[Fixture.id])
//        println(row[Fixture.date])
//    }
}
