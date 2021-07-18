package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import at.rueckgr.kotlin.rocketbot.webservice.ConnectMessage
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class Bot : Logging {
    fun launch(host: String, username: String, password: String, ignoredChannels: List<String>) {
        logger().info("Configuration: host={}, username={}, ignoredChannels={}", host, username, ignoredChannels)

        val client = HttpClient(CIO) {
            install(WebSockets)
        }
        runBlocking {
            client.wss(
                method = HttpMethod.Get,
                host = host,
                path = "/websocket"
            ) {
                val messageOutputRoutine = launch { receiveMessages(ignoredChannels, username, password) }
                val userInputRoutine = launch { sendMessage(ConnectMessage()) }

                userInputRoutine.join()
                messageOutputRoutine.join()
            }
        }
    }
    private suspend fun DefaultClientWebSocketSession.sendMessage(message: Any) {
        // TODO implement token refresh

        val jsonMessage = Gson().toJson(message)
        logger().debug("Outgoing message: {}", jsonMessage)
        send(Frame.Text(jsonMessage))
    }

    private suspend fun DefaultClientWebSocketSession.receiveMessages(ignoredChannels: List<String>, username: String, password: String) {
        try {
            for (message in incoming) {
                message as? Frame.Text ?: continue
                val text = message.readText()
                logger().debug("Incoming message: {}", text)

                @Suppress("UNCHECKED_CAST") val data = Gson().fromJson(text, Object::class.java) as Map<String, Any>
                if("msg" !in data) {
                    continue
                }
                val responses: Array<Any> = when (val messageType = data["msg"]) {
                    "connected" -> handleConnectedMessage(data, username, password)
                    "result" -> handleResultMessage(ignoredChannels, data)
                    "ping" -> handlePingMessage(data)
                    "changed" -> handleChangedMessage(ignoredChannels, username, data)
                    else -> {
                        logger().info("Unknown message type \"{}\", ignoring message", messageType)
                        continue
                    }
                }

                responses.forEach {
                    sendMessage(it)
                }
            }
        }
        catch (e: Exception) {
            logger().error("Error while receiving", e)
        }
    }
}

