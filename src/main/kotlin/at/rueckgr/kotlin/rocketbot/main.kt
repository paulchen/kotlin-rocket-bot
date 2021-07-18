package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.plugins.AbstractPlugin
import at.rueckgr.kotlin.rocketbot.webservice.*
import org.apache.commons.codec.digest.DigestUtils
import org.reflections.Reflections
import java.util.*

fun main() {
    // TODO store authentication token somewhere
    val host = System.getenv("ROCKETCHAT_HOST") ?: return
    val username = System.getenv("ROCKETCHAT_USERNAME") ?: return
    val password = System.getenv("ROCKETCHAT_PASSWORD") ?: return
    val ignoredChannels = System.getenv("IGNORED_CHANNELS")?.split(",") ?: emptyList()

    Bot(BotConfiguration(host, username, password, ignoredChannels)).launch()

}

@Suppress("UNCHECKED_CAST")
fun handleGetRoomsResult(ignoredChannels: List<String>, data: Map<String, Any>): Array<Any> {
    val rooms: List<Map<String, Any>> = data["result"] as List<Map<String, Any>>
    return rooms.filter {
        !ignoredChannels.contains(it["name"])
    }.map {
        val id = it["_id"]
        SubscribeMessage(id = "subscribe-$id", name = "stream-room-messages", params = arrayOf(id, false))
    }.toTypedArray()
}

@Suppress("UNCHECKED_CAST")
fun handleStreamRoomMessages(configuration: BotConfiguration, data: Map<String, Any>): List<List<Any>> {
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

@Suppress("UNCHECKED_CAST")
fun handleStreamNotifyUser(configuration: BotConfiguration, data: Map<String, Any>): List<List<Any>> {
    val fields = data["fields"] as Map<String, Any>
    val args = fields["args"] as List<Any>

    if (args[0] != "inserted") {
        return emptyList()
    }

    return args.subList(1, args.size).map {
        val details = it as Map<String, String>
        val roomId = details["_id"]

        if (configuration.ignoredChannels.contains(details["fname"])) {
            emptyList()
        }
        else {
            listOf(
                SubscribeMessage(
                    id = "subscribe-$roomId",
                    name = "stream-room-messages",
                    params = arrayOf(roomId, false)
                )
            )
        }
    }
}

fun handleUserMessage(ownUsername: String, roomId: String, username: String, message: String): List<SendMessageMessage> {
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
