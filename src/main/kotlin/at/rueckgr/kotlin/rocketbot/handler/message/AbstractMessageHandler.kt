package at.rueckgr.kotlin.rocketbot.handler.message

import at.rueckgr.kotlin.rocketbot.BotConfiguration
import com.fasterxml.jackson.databind.JsonNode

abstract class AbstractMessageHandler {
    abstract fun getHandledMessage(): String

    abstract fun handleMessage(configuration: BotConfiguration, data: JsonNode, timestamp: Long): Array<Any>
}
