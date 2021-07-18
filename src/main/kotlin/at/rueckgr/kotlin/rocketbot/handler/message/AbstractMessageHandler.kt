package at.rueckgr.kotlin.rocketbot.handler.message

import at.rueckgr.kotlin.rocketbot.BotConfiguration

abstract class AbstractMessageHandler {
    abstract fun getHandledMessage(): String

    abstract fun handleMessage(configuration: BotConfiguration, data: Map<String, Any>): Array<Any>
}
