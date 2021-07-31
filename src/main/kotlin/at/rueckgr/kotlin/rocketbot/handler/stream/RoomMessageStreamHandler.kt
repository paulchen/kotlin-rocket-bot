package at.rueckgr.kotlin.rocketbot.handler.stream

import at.rueckgr.kotlin.rocketbot.BotConfiguration
import at.rueckgr.kotlin.rocketbot.handler.PluginProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import at.rueckgr.kotlin.rocketbot.webservice.SendMessageMessage
import at.rueckgr.kotlin.rocketbot.webservice.UnsubscribeMessage
import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.lang3.StringUtils
import java.util.*

class RoomMessageStreamHandler : AbstractStreamHandler(), Logging {
    private val pluginProvider = PluginProvider.instance

    override fun getHandledStream() = "stream-room-messages"

    @Suppress("UNCHECKED_CAST")
    override fun handleStreamMessage(configuration: BotConfiguration, data: JsonNode): List<List<Any>> {
        val args = data.get("fields")?.get("args") ?: emptyList()

        return args.map { handleStreamMessageItem(configuration, it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleStreamMessageItem(configuration: BotConfiguration, it: JsonNode): List<Any> {
        val message = it.get("msg").textValue()
        val roomId = it.get("rid").textValue()

        val t = it.get("t") ?: ""
        if (t == "ru" && message == configuration.username) {
            return listOf(UnsubscribeMessage(id = "subscribe-$roomId"))
        }

        val i = it.get("bot")?.get("i")?.textValue() ?: ""
        if (StringUtils.isNotBlank(i)) {
            logger().debug("Message comes from self-declared bot, ignoring")
            return emptyList()
        }

        val username = it.get("u")?.get("username")?.textValue() ?: ""
        return handleUserMessage(configuration.username, roomId, username, message.trim())
    }

    private fun handleUserMessage(ownUsername: String, roomId: String, username: String, message: String): List<SendMessageMessage> {
        if (username == ownUsername) {
            logger().debug("Message comes from myself, ignoring")
            return emptyList()
        }
        if (!message.startsWith("!")) {
            logger().debug("Message contains no command, ignoring")
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
