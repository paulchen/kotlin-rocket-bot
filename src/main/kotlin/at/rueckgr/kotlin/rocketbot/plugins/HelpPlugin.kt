package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.handler.PluginProvider

class HelpPlugin : AbstractPlugin() {
    override fun getCommands() = listOf("help")

    override fun handle(message: String): List<String> {
        val parts = message.split(" ")
        return when(parts.size) {
            1 -> generalHelp()
            2 -> detailedHelp(parts[1])
            else -> emptyList()
        }
    }

    override fun getHelp(command: String): List<String> = listOf(
            "`!help` lists all available commands",
            "`!help <command>` gives details about <command>"
        )

    private fun generalHelp(): List<String> = listOf(
        "Available commands: "
            + PluginProvider.instance
                .getCommands()
                .sorted()
                .joinToString(", ") { "`$it`" }
            + "\nType `!help <command>` for details about <command>"
    )

    private fun detailedHelp(command: String): List<String> = listOf(
        PluginProvider.instance
            .getByCommand(command)
            .flatMap { it.getHelp(command) }
            .joinToString("\n")
        ).filter { it.isNotBlank() }
}
