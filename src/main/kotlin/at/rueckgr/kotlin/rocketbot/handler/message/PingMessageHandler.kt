package at.rueckgr.kotlin.rocketbot.handler.message

import at.rueckgr.kotlin.rocketbot.BotConfiguration
import at.rueckgr.kotlin.rocketbot.webservice.PongMessage

class PingMessageHandler : AbstractMessageHandler() {
    override fun getHandledMessage() = "ping"

    override fun handleMessage(configuration: BotConfiguration, data: Map<String, Any>): Array<Any> = arrayOf(PongMessage())
}
