package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.RoomMessageHandler

abstract class AbstractPlugin {
    abstract fun getCommands(): List<String>

    abstract fun handle(channel: RoomMessageHandler.Channel, user: RoomMessageHandler.User, message: RoomMessageHandler.Message): List<OutgoingMessage>

    abstract fun getHelp(command: String): List<String>

    open fun init() {}

    fun stripCommand(message: String): String? {
        val pos = message.indexOf(" ")
        if (pos < 0) {
            return null
        }
        return message.substring(pos + 1).trim()
    }

    abstract fun getProblems(): List<String>

    fun handleBotMessages(): Boolean = false
}
