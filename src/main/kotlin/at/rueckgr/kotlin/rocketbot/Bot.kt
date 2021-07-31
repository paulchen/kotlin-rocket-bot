package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.handler.message.AbstractMessageHandler
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import at.rueckgr.kotlin.rocketbot.webservice.ConnectMessage
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.reflections.Reflections


class Bot(private val configuration: BotConfiguration) : Logging {
    fun start() {
        logger().info(
            "Configuration: host={}, username={}, ignoredChannels={}",
            configuration.host, configuration.username, configuration.ignoredChannels
        )

        Webservice().start()
        runBlocking { runWebsocketClient() }
    }

    private suspend fun runWebsocketClient() {
        val client = HttpClient(CIO) {
            install(WebSockets)
        }
        client.wss(
            method = HttpMethod.Get,
            host = configuration.host,
            path = "/websocket"
        ) {
            val messageOutputRoutine = launch { receiveMessages() }
            val userInputRoutine = launch { sendMessage(ConnectMessage()) }

            userInputRoutine.join()
            messageOutputRoutine.join()
        }
    }
    private suspend fun DefaultClientWebSocketSession.sendMessage(message: Any) {
        // TODO implement token refresh

        // TODO use jackson for this
        val jsonMessage = Gson().toJson(message)
        logger().debug("Outgoing message: {}", jsonMessage)
        send(Frame.Text(jsonMessage))
    }

    private suspend fun DefaultClientWebSocketSession.receiveMessages() {
        val handlers = Reflections(AbstractMessageHandler::class.java.packageName)
            .getSubTypesOf(AbstractMessageHandler::class.java)
            .map { it.getDeclaredConstructor().newInstance() }
            .associateBy { it.getHandledMessage() }

        try {
            for (message in incoming) {
                message as? Frame.Text ?: continue
                val text = message.readText()
                logger().debug("Incoming message: {}", text)

                @Suppress("BlockingMethodInNonBlockingContext") val data = ObjectMapper().readTree(text)
                val messageType = data.get("msg")?.textValue() ?: continue
                if(messageType !in handlers) {
                    logger().info("Unknown message type \"{}\", ignoring message", messageType)
                    continue
                }

                try {
                    handlers[messageType]
                        ?.handleMessage(configuration, data, getTimestamp(data))
                        ?.forEach { sendMessage(it) }
                }
                catch (e: Exception) {
                    logger().error(e.message, e)
                }
            }
        }
        catch (e: Exception) {
            logger().error("Error while receiving", e)
        }
    }

    private fun getTimestamp(jsonNode: JsonNode): Long {
        val dateNode = jsonNode.get("fields")
            ?.get("args")
            ?.get(0)
            ?.get("ts")
            ?.get("\$date") ?: return 0L
        if (dateNode.isLong) {
            return dateNode.asLong()
        }
        return 0L
    }
}

