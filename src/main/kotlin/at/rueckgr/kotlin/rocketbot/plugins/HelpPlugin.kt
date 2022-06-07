package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.RoomMessageHandler
import at.rueckgr.kotlin.rocketbot.handler.PluginProvider

class HelpPlugin : AbstractPlugin() {
    override fun getCommands() = listOf("help")

    override fun handle(channel: RoomMessageHandler.Channel, user: RoomMessageHandler.User, message: RoomMessageHandler.Message): List<OutgoingMessage> {
        val parts = message.message.split(" ")
        return when(parts.size) {
            1 -> generalHelp()
            2 -> detailedHelp(parts[1])
            else -> emptyList()
        }.map { OutgoingMessage(it) }
    }

    override fun getHelp(command: String): List<String> = listOf(
            "`!help` lists all available commands",
            "`!help <command>` gives details about <command>"
        )

    private fun generalHelp(): List<String> = listOf(
        "Available commands: "
            + PluginProvider
                .getCommands()
                .sorted()
                .joinToString(", ") { "`$it`" }
            + "\nType `!help <command>` for details about <command>\nI am also happy to serve you via private messages."
    )

    private fun detailedHelp(command: String): List<String> = listOf(
        PluginProvider
            .getByCommand(command)
            .flatMap { it.getHelp(command) }
            .joinToString("\n")
        ).filter { it.isNotBlank() }

    override fun getProblems() = emptyList<String>()
}
