package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.EventHandler

abstract class AbstractPlugin {
    abstract fun getCommands(): List<String>

    abstract fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage>

    abstract fun getHelp(command: String): List<String>

    open fun getChannelTypes() = EventHandler.ChannelType.values().toList()

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

    fun handleBotMessages(): Boolean = false

    open fun getAdditionalStatus(): Map<String, String> = emptyMap()
}
