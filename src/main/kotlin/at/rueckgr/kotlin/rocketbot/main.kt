package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.webservice.*

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


