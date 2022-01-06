package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.database.Fixture
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.from
import me.liuwj.ktorm.dsl.select
import org.openapitools.client.infrastructure.ApiClient

fun main() {
    ConfigurationProvider.instance.loadConfiguration("/config/kotlin-rocket-bot.yaml")

    ApiClient.apiKey.put("x-apisports-key", ConfigurationProvider.instance.getConfiguration().plugins?.soccer?.apiKey!!)
//    val fixtures = DefaultApi()
//        .getFixtures(713474, null, null, null, null, null, null, null, null, null, null, null, null)
//    println(fixtures)

//    val fixtureRounds = FootballApi()
//        .getFixturesRounds(4, 2020, false)
//    println(fixtureRounds)

    val databaseConfiguration = ConfigurationProvider.instance.getConfiguration().database
    val database = Database.connect(
        url = "jdbc:postgresql://${databaseConfiguration!!.host}/${databaseConfiguration.database}",
        user = databaseConfiguration.username,
        password = databaseConfiguration.password)

    for (row in database.from(Fixture).select()) {
        println(row[Fixture.id])
        println(row[Fixture.date])
    }
}
