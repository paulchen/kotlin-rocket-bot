package at.rueckgr.kotlin.rocketbot.handler.message

import at.rueckgr.kotlin.rocketbot.BotConfiguration
import at.rueckgr.kotlin.rocketbot.webservice.RoomsGetMessage
import at.rueckgr.kotlin.rocketbot.webservice.SubscribeMessage

class ResultMessageHandler : AbstractMessageHandler() {
    override fun getHandledMessage() = "result"

    override fun handleMessage(configuration: BotConfiguration, data: Map<String, Any>) = when (data["id"]) {
        "login-initial" -> {
            val userId = (data["result"] as Map<*, *>)["id"]
            arrayOf(
                RoomsGetMessage(id = "get-rooms-initial"),
                SubscribeMessage(id = "subscribe-stream-notify-user", name = "stream-notify-user", params = arrayOf("$userId/rooms-changed", false))
            )
        }
        "get-rooms-initial" -> handleGetRoomsResult(configuration.ignoredChannels, data)
        else -> emptyArray()
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleGetRoomsResult(ignoredChannels: List<String>, data: Map<String, Any>): Array<Any> {
        val rooms: List<Map<String, Any>> = data["result"] as List<Map<String, Any>>
        return rooms.filter {
            !ignoredChannels.contains(it["name"])
        }.map {
            val id = it["_id"]
            SubscribeMessage(id = "subscribe-$id", name = "stream-room-messages", params = arrayOf(id, false))
        }.toTypedArray()
    }}
