package at.rueckgr.kotlin.rocketbot

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

class ArchiveService {
    fun getUserDetails(username: String): UserDetails? {
        val encodedUsername = URLEncoder.encode(username, "utf-8")
        return runBlocking {
            val client = HttpClient(CIO) {
                install (JsonFeature) {
                    serializer = JacksonSerializer {
                        findAndRegisterModules()
                    }
                }
            }

            try {
                client.get("http://backend:8081/user/$encodedUsername")
            }
            catch (e: ClientRequestException) {
                null
            }
        }
    }
}
