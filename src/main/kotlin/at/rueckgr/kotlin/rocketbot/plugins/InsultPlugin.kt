package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage

class InsultPlugin : AbstractPlugin() {
    private val insults: List<String> = VersionPlugin::class.java.getResource("/insult.dat")!!
        .readText()
        .split("\n")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    override fun getCommands() = listOf("insult")

    override fun handle(message: String): List<OutgoingMessage> {
        val name = stripCommand(message) ?: return emptyList()
        val insult = insults.random()
        return listOf(OutgoingMessage("$name: $insult"))
    }

    override fun getHelp(command: String) = listOf(
        "`!insult <name>` issues an insult towards `<name>`"
    )

    override fun getProblems() = emptyList<String>()
}
