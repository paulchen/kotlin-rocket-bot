package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage

class AboutPlugin : AbstractPlugin() {
    override fun getCommands(): List<String> = listOf("about")

    override fun handle(message: String): List<OutgoingMessage> =
        listOf(OutgoingMessage("This is `kotlin-rocket-bot`. Its sources can be found on GitHub: https://github.com/paulchen/kotlin-rocket-bot"))

    override fun getHelp(command: String): List<String> =
        listOf("`!about` shows information about this bot")

    override fun getProblems() = emptyList<String>()
}
