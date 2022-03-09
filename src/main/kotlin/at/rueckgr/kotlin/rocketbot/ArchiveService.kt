package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.VersionInfo
import at.rueckgr.kotlin.rocketbot.util.logger
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import java.net.URLEncoder
import java.time.ZonedDateTime

data class UserDetails(val user: User)

data class User(val username: String, val timestamp: ZonedDateTime?)

data class VersionDetails(val version: VersionInfo)

class ArchiveService : Logging {
    fun getUserDetails(username: String): UserDetails? {
        val encodedUsername = URLEncoder.encode(username, "utf-8")
        return runBlocking {
            try {
                getClient().get("http://backend:8081/user/$encodedUsername")
            }
            catch (e: ClientRequestException) {
                logger().error("Exception occurred", e)
                null
            }
        }
    }

    private fun getClient() = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                findAndRegisterModules()
            }
        }
    }

    fun getVersion(): VersionInfo {
        return runBlocking {
            try {
                val versionDetails: VersionDetails = getClient().get("http://backend:8081/version")
                versionDetails.version
            }
            catch (e: Exception) {
                logger().error("Exception occurred", e)
                VersionInfo("unknown", "unknown")
            }
        }
    }
}
