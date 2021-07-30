package at.rueckgr.kotlin.rocketbot.handler.message

import at.rueckgr.kotlin.rocketbot.BotConfiguration
import at.rueckgr.kotlin.rocketbot.webservice.PongMessage
import java.time.LocalDateTime

class PingMessageHandler : AbstractMessageHandler() {
    companion object {
        var lastPing: LocalDateTime = LocalDateTime.now()
    }

    override fun getHandledMessage() = "ping"

    override fun handleMessage(configuration: BotConfiguration, data: Map<String, Any>): Array<Any> {
        lastPing = LocalDateTime.now()
        return arrayOf(PongMessage())
    }
}
