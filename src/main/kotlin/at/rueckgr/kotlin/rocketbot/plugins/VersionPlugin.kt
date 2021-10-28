package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.ArchiveService
import at.rueckgr.kotlin.rocketbot.util.LibraryVersion

class VersionPlugin : AbstractPlugin() {
    private val botRevision: String = when (val resource = VersionPlugin::class.java.getResource("/git-revision")) {
        null -> "unknown"
        else -> resource.readText().trim()
    }

    private val libraryRevision = LibraryVersion.revision

    override fun getCommands(): List<String> = listOf("version")

    override fun handle(message: String): List<String> {
        val archiveRevision = ArchiveService().getVersion()
        return listOf("*kotlin-rocket-bot* revision `$botRevision`, *kotlin-rocket-lib* revision `$libraryRevision`, *rocketchat-archive* revision `$archiveRevision`")
    }

    override fun getHelp(command: String): List<String> =
        listOf("`!version` outputs the Git revision of kotlin-rocket-bot currently running")
}
