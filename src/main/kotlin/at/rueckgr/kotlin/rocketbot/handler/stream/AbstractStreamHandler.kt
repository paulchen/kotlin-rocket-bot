package at.rueckgr.kotlin.rocketbot.handler.stream

import at.rueckgr.kotlin.rocketbot.BotConfiguration
import com.fasterxml.jackson.databind.JsonNode

abstract class AbstractStreamHandler {
    abstract fun getHandledStream(): String

    abstract fun handleStreamMessage(configuration: BotConfiguration, data: JsonNode): List<List<Any>>
}
