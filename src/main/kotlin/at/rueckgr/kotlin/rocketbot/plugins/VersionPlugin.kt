package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.ArchiveService
import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.util.LibraryVersion
import at.rueckgr.kotlin.rocketbot.util.VersionHelper

class VersionPlugin : AbstractPlugin() {
    private val botRevision = VersionHelper.getVersion()
    private val libraryRevision = LibraryVersion.instance.getVersion()

    override fun getCommands(): List<String> = listOf("version")

    override fun handle(username: String, message: String, botMessage: Boolean): List<OutgoingMessage> {
        val archiveRevision = ArchiveService().getVersion()

        val builder = StringBuilder()

        builder.append("*kotlin-rocket-bot* revision `${botRevision.revision}` ( _${botRevision.commitMessage}_ )\n")
        builder.append("*kotlin-rocket-lib* revision `${libraryRevision.revision}` ( _${libraryRevision.commitMessage}_ )\n")
        builder.append("*rocketchat-archive* revision `${archiveRevision.revision}` ( _${archiveRevision.commitMessage}_ )")

        return listOf(OutgoingMessage(builder.toString()))
    }

    override fun getHelp(command: String): List<String> =
        listOf("`!version` outputs the Git revision of kotlin-rocket-bot currently running")

    override fun getProblems(): List<String> {
        val archiveRevision = ArchiveService().getVersion()

        return if (archiveRevision.revision == "unknown" || archiveRevision.commitMessage == "unknown") {
            listOf("Unable to fetch version information from archive")
        }
        else {
            emptyList()
        }
    }
}
