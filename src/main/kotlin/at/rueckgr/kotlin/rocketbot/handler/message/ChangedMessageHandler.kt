package at.rueckgr.kotlin.rocketbot.handler.message

import at.rueckgr.kotlin.rocketbot.BotConfiguration
import at.rueckgr.kotlin.rocketbot.handleStreamNotifyUser
import at.rueckgr.kotlin.rocketbot.handleStreamRoomMessages

class ChangedMessageHandler : AbstractMessageHandler() {
    override fun getHandledMessage() = "changed"

    override fun handleMessage(configuration: BotConfiguration, data: Map<String, Any>): Array<Any> {
        return when (data["collection"]) {
            "stream-room-messages" -> handleStreamRoomMessages(configuration, data)
            "stream-notify-user" -> handleStreamNotifyUser(configuration, data)
            else -> return emptyArray()
        }.flatten().toTypedArray()
    }
}
