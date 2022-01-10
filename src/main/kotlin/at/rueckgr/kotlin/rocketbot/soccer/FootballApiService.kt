package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import com.api_football.api.FootballApi
import com.api_football.model.FixtureResponse
import com.api_football.model.VenueResponseResponse
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.openapitools.client.infrastructure.ApiClient
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FootballApiService : Logging {
    companion object {
        val instance = FootballApiService()
    }

    init {
        val soccerConfiguration = ConfigurationProvider.instance.getSoccerConfiguration()
        ApiClient.apiKey["x-apisports-key"] = soccerConfiguration.apiKey!!
        ApiClient.builder.addInterceptor(HttpLoggingInterceptor { logger().debug(it) }.setLevel(HttpLoggingInterceptor.Level.BODY))
        ApiClient.builder.addInterceptor(JsonDumpInterceptor())
    }

    fun getAllFixtures(): FixtureResponse {
        val soccerConfiguration = ConfigurationProvider.instance.getSoccerConfiguration()

        logger().debug("Calling getFixtures() with parameters: leagueId={}, season={}", soccerConfiguration.leagueId, soccerConfiguration.season)

        val fixtureResponse = FootballApi()
            .getFixtures(null, null, null, soccerConfiguration.leagueId, null, null, null, soccerConfiguration.season, null, null, null, null, null)

        logger().debug("Data returned by getFixtures(): {}", fixtureResponse)

        return fixtureResponse
    }

    fun getFixture(id: Long): FixtureResponse {
        logger().debug("Calling getFixture() with parameter: id={}", id)

        val fixtureResponse = FootballApi()
            .getFixtures(id, null, null, null, null, null, null, null, null, null, null, null, null)

        logger().debug("Data returned by getFixtures(): {}", fixtureResponse)

        return fixtureResponse
    }

    fun getVenue(id: Long): VenueResponseResponse {
        logger().debug("Calling getVenues() with parameters: id={}", id)

        val venue = FootballApi().getVenues(id, null, null, null, null)

        logger().debug("Data returned by getVenues(): {}", venue)

        return venue.response[0]
    }
}

class JsonDumpInterceptor : Interceptor, Logging {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.body != null) {
            val query = request.url.pathSegments.joinToString("/")
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            val filename = "/cache/soccer/$query-$timestamp.json"

            try {
                File("/cache/soccer/").mkdir()

                val source = response.body!!.source()
                source.request(Long.MAX_VALUE)
                val buffer = source.buffer


                FileWriter(filename).use { it.write(buffer.clone().readString(StandardCharsets.UTF_8)) }
            }
            catch (e: IOException) {
                logger().error("Unable to write HTTP response body to file $filename")
            }
        }
        return response
    }

}
