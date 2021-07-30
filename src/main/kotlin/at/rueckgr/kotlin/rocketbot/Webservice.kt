package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.handler.message.PingMessageHandler.Companion.lastPing
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.time.LocalDateTime

class Webservice {
    private val warningSeconds = 60L
    private val criticalSeconds = 120L

    fun start() {
        embeddedServer(Netty, 8080) {
            install(ContentNegotiation) {
                jackson {
                    findAndRegisterModules()
                    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                }
            }
            routing {
                route("/status") {
                    get {
                        call.respond(getStatus())
                    }
                }
            }
        }.start(wait = false)
    }

    private fun getStatus(): Map<String, Any> {
        val status = if (LocalDateTime.now().minusSeconds(criticalSeconds).isAfter(lastPing)) {
            "CRITICAL"
        }
        else if (LocalDateTime.now().minusSeconds(warningSeconds).isAfter(lastPing)) {
            "WARNING"
        }
        else {
            "OK"
        }

        return mapOf(
            "status" to status,
            "lastPing" to lastPing
        )
    }
}
