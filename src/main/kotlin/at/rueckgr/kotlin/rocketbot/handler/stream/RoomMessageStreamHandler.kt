package at.rueckgr.kotlin.rocketbot.handler.stream

import at.rueckgr.kotlin.rocketbot.BotConfiguration
import at.rueckgr.kotlin.rocketbot.handler.PluginProvider
import at.rueckgr.kotlin.rocketbot.webservice.SendMessageMessage
import at.rueckgr.kotlin.rocketbot.webservice.UnsubscribeMessage
import java.util.*

class RoomMessageStreamHandler : AbstractStreamHandler() {
    private val pluginProvider = PluginProvider.instance

    override fun getHandledStream() = "stream-room-messages"

    @Suppress("UNCHECKED_CAST")
    override fun handleStreamMessage(configuration: BotConfiguration, data: Map<String, Any>): List<List<Any>> {
        val fields = data["fields"] as Map<String, Any>
        val args = fields["args"] as List<Map<String, Any>>

        return args.map {
            val message = it["msg"] as String
            val roomId = it["rid"] as String

            if ("t" in it && it["t"] == "ru" && message == configuration.username) {
                listOf(UnsubscribeMessage(id = "subscribe-$roomId"))
            }
            else {
                val userData = it["u"] as Map<String, String>
                val username = userData["username"] ?: ""
                handleUserMessage(configuration.username, roomId, username, message.trim())
            }
        }
    }

    private fun handleUserMessage(ownUsername: String, roomId: String, username: String, message: String): List<SendMessageMessage> {
        if (username == ownUsername) {
            return emptyList()
        }
        if (!message.startsWith("!")) {
            return emptyList()
        }

        val command = message.split(" ")[0].substring(1)
        return pluginProvider
            .getByCommand(command)
            .flatMap { it.handle(message) }
            .map {
                val id = UUID.randomUUID().toString()
                val botTag = mapOf("i" to "paulchen/kotlin-rocketBot")
                SendMessageMessage(id = id, params = listOf(mapOf("_id" to id, "rid" to roomId, "msg" to it, "bot" to botTag)))
            }
    }
}
