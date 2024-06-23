package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.EventHandler

abstract class AbstractPlugin {
    abstract fun getCommands(): List<String>

    abstract fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage>

    open fun handleOwnMessage(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        /* do nothing by default */
        return emptyList()
    }

    abstract fun getHelp(command: String): List<String>

    open fun getChannelTypes() = EventHandler.ChannelType.entries.toList()

    open fun init() { /* do nothing by default */ }

    open fun reinit() { /* do nothing by default */ }

    fun stripCommand(message: String): String? {
        val pos = message.indexOf(" ")
        if (pos < 0) {
            return null
        }
        return message.substring(pos + 1).trim()
    }

    abstract fun getProblems(): List<String>

    open fun handleBotMessages(): Boolean = false

    open fun getAdditionalStatus(): Map<String, String> = emptyMap()

    open fun runInSeriousMode(): Boolean = true
}
