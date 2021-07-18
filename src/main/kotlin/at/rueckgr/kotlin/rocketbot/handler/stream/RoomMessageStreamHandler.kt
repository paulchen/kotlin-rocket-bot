package at.rueckgr.kotlin.rocketbot.handler.stream

import at.rueckgr.kotlin.rocketbot.BotConfiguration
import at.rueckgr.kotlin.rocketbot.plugins.AbstractPlugin
import at.rueckgr.kotlin.rocketbot.webservice.SendMessageMessage
import at.rueckgr.kotlin.rocketbot.webservice.UnsubscribeMessage
import org.reflections.Reflections
import java.util.*

class RoomMessageStreamHandler : AbstractStreamHandler() {
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
        val flatMap = Reflections(AbstractPlugin::class.java.packageName)
            .getSubTypesOf(AbstractPlugin::class.java)
            .flatMap {
                val plugin = it.getDeclaredConstructor().newInstance()
                if (plugin.getCommands().contains(command)) {
                    plugin.handle(message)
                }
                else {
                    emptyList()
                }
            }
        return flatMap
            .map {
                val id = UUID.randomUUID().toString()
                SendMessageMessage(id = id, params = listOf(mapOf("_id" to id, "rid" to roomId, "msg" to it)))
            }
    }
}
