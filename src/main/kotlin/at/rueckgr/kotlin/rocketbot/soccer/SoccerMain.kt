package at.rueckgr.kotlin.rocketbot.soccer

import com.api_football.api.DefaultApi
import org.openapitools.client.infrastructure.ApiClient

fun main() {
    ApiClient.apiKey.put("x-apisports-key", "TODO")
//    val fixtures = DefaultApi()
//        .getFixtures(713474, null, null, null, null, null, null, null, null, null, null, null, null)
//    println(fixtures)

    val fixtureRounds = DefaultApi()
        .getFixturesRounds(4, 2020, false)
    println(fixtureRounds)
}
