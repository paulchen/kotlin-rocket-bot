package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.EventHandler
import at.rueckgr.kotlin.rocketbot.util.Logging


class SloganPlugin : AbstractPlugin(), Logging {
    private val insults: List<String> = VersionPlugin::class.java.getResource("/slogan.dat")!!
        .readText()
        .split("\n")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    override fun getCommands() = listOf("slogan")

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        val name = stripCommand(message.message) ?: return emptyList()
        val insult = insults.random().replace("<input>", "*$name*")
        return listOf(OutgoingMessage(insult))
    }

    override fun getHelp(command: String) = listOf(
        "`!slogan <object>` prints a slogan for `<object>`"
    )

    override fun getProblems() = emptyList<String>()
}
