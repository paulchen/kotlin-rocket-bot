package at.rueckgr.kotlin.rocketbot.plugins

class NicePlugin : AbstractPlugin() {
    override fun getCommands(): List<String> = emptyList()

    override fun handle(message: String): List<String> =
        when(activatePlugin() && message.matches("""(.*[^0-9a-z_.,\-]|^)69([^0-9.,a-z_\-].*|$)""".toRegex())) {
            true -> listOf("_nice_")
            false -> emptyList()
        }

    private fun activatePlugin(): Boolean =(0..9).random() < 5

    override fun getHelp(command: String): List<String> = emptyList()
}
