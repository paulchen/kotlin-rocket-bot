package at.rueckgr.kotlin.rocketbot.handler.stream

import at.rueckgr.kotlin.rocketbot.BotConfiguration
import at.rueckgr.kotlin.rocketbot.webservice.SubscribeMessage

class NotifyUserStreamHandler : AbstractStreamHandler() {
    override fun getHandledStream() = "stream-notify-user"

    @Suppress("UNCHECKED_CAST")
    override fun handleStreamMessage(configuration: BotConfiguration, data: Map<String, Any>): List<List<Any>> {
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
        }    }
}
