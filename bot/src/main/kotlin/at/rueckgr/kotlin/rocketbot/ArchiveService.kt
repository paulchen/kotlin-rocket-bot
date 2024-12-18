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
    val id: String,
    val username: String,
    val mostRecentMessage: Message?,
    val rooms: List<String>
)

@Serializable
data class Message(
    val id: String,
    val rid: String,
    val message: String,
    @Serializable(KZonedDateTimeSerializer::class)
    val timestamp: ZonedDateTime,
    val username: String,
    val attachments: List<Attachment>,
    @Serializable(KZonedDateTimeSerializer::class)
    val editedAt: ZonedDateTime?,
    val editedBy: String?
)

@Serializable
data class Attachment(
    val type: String?,
    val title: String?,
    val titleLink: String?,
    val description: String?,
    val messageLink: String?
)

@Serializable
data class VersionDetails(val version: VersionInfo, val mongoDbVersion: String)

@Serializable
data class ChannelInfo(
    val id: String,
    val name: String,
    @Serializable(KZonedDateTimeSerializer::class)
    val lastActivity: ZonedDateTime?
)

class ArchiveService : Logging {
    fun getUserByUsername(username: String): UserDetails? {
        val encodedUsername = URLEncoder.encode(username, "utf-8")
        return getUserDetails("http://backend:8081/user/$encodedUsername")
    }

    fun getUserById(userId: String): UserDetails? {
        val encodedUserId = URLEncoder.encode(userId, "utf-8")
        return getUserDetails("http://backend:8081/user/id/$encodedUserId")
    }

    private fun getUserDetails(url: String): UserDetails? {
        return runBlocking {
            try {
                val response = getClient().use { it.get(url) }
                if (response.status.value > 299) {
                    logger().info("Status code received from archive backend: {}", response.status.value)
                    null
                }
                else {
                    response.body<UserDetails>()
                }
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

    fun getVersion(): VersionDetails {
        return runBlocking {
            try {
                getClient().use { it.get("http://backend:8081/version").body() }
            }
            catch (e: Exception) {
                logger().error("Exception occurred", e)
                VersionDetails(VersionInfo("unknown", "unknown"), "unknown")
            }
        }
    }

    fun getChannelInfo(channelId: String): ChannelInfo? {
        val encodedId = URLEncoder.encode(channelId, "utf-8")
        return runBlocking {
            try {
                val response = getClient().use { it.get("http://backend:8081/channel/$encodedId") }
                if (response.status.value > 299) {
                    logger().info("Status code received from archive backend: {}", response.status.value)
                    null
                }
                else {
                    response.body<ChannelInfo>()
                }
            }
            catch (e: ClientRequestException) {
                logger().error("Exception occurred", e)
                null
            }
        }
    }
}
