package at.rueckgr.kotlin.rocketbot.handler.stream

import at.rueckgr.kotlin.rocketbot.BotConfiguration

abstract class AbstractStreamHandler {
    abstract fun getHandledStream(): String

    abstract fun handleStreamMessage(configuration: BotConfiguration, data: Map<String, Any>): List<List<Any>>
}
