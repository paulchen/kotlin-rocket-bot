package at.rueckgr.kotlin.rocketbot.plugins

class VersionPlugin : AbstractPlugin() {
    private val revision: String = when (val resource = VersionPlugin::class.java.getResource("/git-revision")) {
        null -> "unknown"
        else -> resource.readText().trim()
    }

    override fun getCommands(): List<String> = listOf("version")

    override fun handle(message: String): List<String> = listOf("kotlin-rocket-bot revision `$revision`")
}
