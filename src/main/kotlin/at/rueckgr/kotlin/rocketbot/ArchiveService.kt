package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.VersionInfo
import at.rueckgr.kotlin.rocketbot.util.logger
import at.rueckgr.kotlin.rocketbot.util.time.KZonedDateTimeSerializer
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.net.URLEncoder
import java.time.ZonedDateTime

@Serializable
data class UserDetails(val user: User)

@Serializable
data class User(
    val username: String,
    @Serializable(KZonedDateTimeSerializer::class)
    val timestamp: ZonedDateTime?
)

@Serializable
data class VersionDetails(val version: VersionInfo)

class ArchiveService : Logging {
    fun getUserDetails(username: String): UserDetails? {
        val encodedUsername = URLEncoder.encode(username, "utf-8")
        return runBlocking {
            try {
                getClient().get("http://backend:8081/user/$encodedUsername").body<UserDetails>()
            }
            catch (e: ClientRequestException) {
                logger().error("Exception occurred", e)
                null
            }
        }
    }

    private fun getClient() = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    fun getVersion(): VersionInfo {
        return runBlocking {
            try {
                val versionDetails: VersionDetails = getClient().get("http://backend:8081/version").body()
                versionDetails.version
            }
            catch (e: Exception) {
                logger().error("Exception occurred", e)
                VersionInfo("unknown", "unknown")
            }
        }
    }
}
