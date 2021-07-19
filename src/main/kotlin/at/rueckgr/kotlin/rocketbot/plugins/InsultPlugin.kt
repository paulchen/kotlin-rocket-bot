package at.rueckgr.kotlin.rocketbot.plugins

class InsultPlugin : AbstractPlugin() {
    private val insults: List<String> = VersionPlugin::class.java.getResource("/insult.dat")!!
        .readText()
        .split("\n")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    override fun getCommands() = listOf("insult")

    override fun handle(message: String): List<String> {
        val pos = message.indexOf(" ")
        if (pos < 0) {
            return emptyList()
        }
        val name = message.substring(pos + 1)
        val insult = insults.random()
        return listOf("$name: $insult")
    }

    override fun getHelp(command: String) = listOf(
        "`!insult <name>` issues an insult towards `<name>`"
    )
}
