package at.rueckgr.kotlin.rocketbot.handler.message

import at.rueckgr.kotlin.rocketbot.BotConfiguration
import at.rueckgr.kotlin.rocketbot.handler.stream.AbstractStreamHandler
import org.reflections.Reflections

class ChangedMessageHandler : AbstractMessageHandler() {
    private val handlers: Map<String, AbstractStreamHandler> =
        Reflections(AbstractStreamHandler::class.java.packageName)
            .getSubTypesOf(AbstractStreamHandler::class.java)
            .map { it.getDeclaredConstructor().newInstance() }
            .associateBy { it.getHandledStream() }

    override fun getHandledMessage() = "changed"

    override fun handleMessage(configuration: BotConfiguration, data: Map<String, Any>): Array<Any> {
        val collection = data["collection"]
        if (collection !in handlers) {
            return emptyArray()
        }

        return handlers[collection]!!
            .handleStreamMessage(configuration, data)
            .flatten()
            .toTypedArray()
    }
}
