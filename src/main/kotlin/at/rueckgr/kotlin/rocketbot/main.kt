package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.webservice.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.digest.DigestUtils

fun main() {
    // TODO store authentication token somewhere
    val host = System.getenv("ROCKETCHAT_HOST") ?: return
    val username = System.getenv("ROCKETCHAT_USERNAME") ?: return
    val password = System.getenv("ROCKETCHAT_PASSWORD") ?: return

    val client = HttpClient(CIO) {
        install(WebSockets)
    }
    runBlocking {
        client.wss(
            method = HttpMethod.Get,
            host = host,
            path = "/websocket"
        ) {
            val messageOutputRoutine = launch { outputMessages() }
            val userInputRoutine = launch { inputMessages(username, password) }

            userInputRoutine.join()
            messageOutputRoutine.join()
        }
    }
}

suspend fun DefaultClientWebSocketSession.inputMessages(username: String, password: String) {
    // TODO remove this
    Thread.sleep(1000)

    sendMessage(ConnectMessage())

    val digest = DigestUtils.sha256Hex(password)
    sendMessage(
        LoginMessage(
            params = arrayOf(
                WebserviceRequestParam(
                    UserData(username),
                    PasswordData(digest, "sha-256")
                )
            )
        )
    )
}

suspend fun DefaultClientWebSocketSession.sendMessage(message: Any) {
    val gson = Gson()

    // TODO implement token refresh

    // TODO use logging framework
    println("OUT: " + gson.toJson(message))
    send(Frame.Text(gson.toJson(message)))
}

suspend fun DefaultClientWebSocketSession.outputMessages() {
    try {
        for (message in incoming) {
            message as? Frame.Text ?: continue
            val text = message.readText()
            println("IN: " + text)

            val jsonObject = JsonParser.parseString(text).asJsonObject
            if(!jsonObject.has("msg")) {
                continue
            }

            val messageType = jsonObject.getAsJsonPrimitive("msg").asString

            // TODO handle more messages
            val response = when (messageType) {
                "result" -> handleMessage(Gson().fromJson(text, ResultMessage::class.java))
                "ping" -> handleMessage(Gson().fromJson(text, PingMessage::class.java))
                else -> {
                    println("Unknown message type \"$messageType\", ignoring message")
                    continue
                }
            }

            if (response is NilMessage) {
                continue
            }

            sendMessage(response)
        }
    }
    catch (e: Exception) {
        println("Error while receiving: " + e.localizedMessage)
    }
}

fun handleMessage(message: ResultMessage): Any {
    // TODO token validity not properly parsed
    println(message.result.token)

    return NilMessage()
}

fun handleMessage(message: PingMessage): Any {
    return PongMessage()
}
