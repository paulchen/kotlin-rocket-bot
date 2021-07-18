package at.rueckgr.kotlin.rocketbot.plugins

class AboutPlugin : AbstractPlugin() {
    override fun getCommands(): List<String> = listOf("about")

    override fun handle(message: String): List<String> =
        listOf("This is `kotlin-rocket-bot`. Its sources can be found on GitHub: https://github.com/paulchen/kotlin-rocket-bot")

    override fun getHelp(command: String): List<String> =
        listOf("`!about` shows information about this bot")
}
