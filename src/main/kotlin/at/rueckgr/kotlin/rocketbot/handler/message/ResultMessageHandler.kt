package at.rueckgr.kotlin.rocketbot.handler.message

import at.rueckgr.kotlin.rocketbot.BotConfiguration
import at.rueckgr.kotlin.rocketbot.webservice.RoomsGetMessage
import at.rueckgr.kotlin.rocketbot.webservice.SubscribeMessage
import com.fasterxml.jackson.databind.JsonNode

class ResultMessageHandler : AbstractMessageHandler() {
    override fun getHandledMessage() = "result"

    override fun handleMessage(configuration: BotConfiguration, data: JsonNode, timestamp: Long) = when (data.get("id")?.textValue()) {
        "login-initial" -> {
            val userId = data.get("result").get("id")
            arrayOf(
                RoomsGetMessage(id = "get-rooms-initial"),
                SubscribeMessage(id = "subscribe-stream-notify-user", name = "stream-notify-user", params = arrayOf("$userId/rooms-changed", false))
            )
        }
        "get-rooms-initial" -> handleGetRoomsResult(configuration.ignoredChannels, data)
        else -> emptyArray()
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleGetRoomsResult(ignoredChannels: List<String>, data: JsonNode): Array<Any> {
        val rooms = data.get("result")
        return rooms
            .filter { it.get("t").textValue() == "c" }
            .filter { !ignoredChannels.contains(it.get("name").textValue()) }
            .map {
                val id = it.get("_id").textValue()
                SubscribeMessage(id = "subscribe-$id", name = "stream-room-messages", params = arrayOf(id, false))
            }
            .toTypedArray()
    }
}
