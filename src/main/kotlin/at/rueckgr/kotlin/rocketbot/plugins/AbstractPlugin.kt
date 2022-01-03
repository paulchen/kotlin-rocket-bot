package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage

abstract class AbstractPlugin {
    abstract fun getCommands(): List<String>

    abstract fun handle(message: String): List<OutgoingMessage>

    abstract fun getHelp(command: String): List<String>

    fun stripCommand(message: String): String? {
        val pos = message.indexOf(" ")
        if (pos < 0) {
            return null
        }
        return message.substring(pos + 1).trim()
    }
}
