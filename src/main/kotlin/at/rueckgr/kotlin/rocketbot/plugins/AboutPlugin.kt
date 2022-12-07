package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.EventHandler

class AboutPlugin : AbstractPlugin() {
    override fun getCommands(): List<String> = listOf("about")

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> =
        listOf(OutgoingMessage("This is `kotlin-rocket-bot`. Its sources can be found on GitHub: https://github.com/paulchen/kotlin-rocket-bot"))

    override fun getHelp(command: String): List<String> =
        listOf("`!about` shows information about this bot")

    override fun getProblems() = emptyList<String>()
}
