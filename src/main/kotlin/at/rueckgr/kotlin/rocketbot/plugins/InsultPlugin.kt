package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.RoomMessageHandler

class InsultPlugin : AbstractPlugin() {
    private val insults: List<String> = VersionPlugin::class.java.getResource("/insult.dat")!!
        .readText()
        .split("\n")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    override fun getCommands() = listOf("insult")

    override fun handle(channel: RoomMessageHandler.Channel, user: RoomMessageHandler.User, message: RoomMessageHandler.Message): List<OutgoingMessage> {
        val name = stripCommand(message.message) ?: return emptyList()
        val insult = insults.random()
        return listOf(OutgoingMessage("$name: $insult"))
    }

    override fun getHelp(command: String) = listOf(
        "`!insult <name>` issues an insult towards `<name>`"
    )

    override fun getProblems() = emptyList<String>()
}
