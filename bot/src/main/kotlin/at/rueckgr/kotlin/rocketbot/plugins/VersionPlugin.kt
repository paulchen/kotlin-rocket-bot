package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.ArchiveService
import at.rueckgr.kotlin.rocketbot.Bot
import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.EventHandler
import at.rueckgr.kotlin.rocketbot.util.LibraryVersion
import at.rueckgr.kotlin.rocketbot.util.RestApiClient
import at.rueckgr.kotlin.rocketbot.util.VersionHelper
import evalBash
import java.util.*

class VersionPlugin : AbstractPlugin() {
    private val botRevision = VersionHelper.getVersion()
    private val libraryRevision = LibraryVersion.instance.getVersion()

    override fun getCommands(): List<String> = listOf("version")

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        val archiveVersion = ArchiveService().getVersion()
        val rocketchatVersion = RestApiClient(Bot.host).getInstanceVersion() ?: "unknown"
        val dockerVersion = System.getenv("DOCKER_VERSION")?.replace("Docker version ", "") ?: "unknown"
        val kernelVersion = getKernelVersion()

        val builder = StringBuilder()

        builder.append("*Rocket.Chat* version `$rocketchatVersion`\n")
        builder.append("*MongoDB* version `${archiveVersion.mongoDbVersion}`\n")
        builder.append("*Docker* version `$dockerVersion`\n")
        builder.append("*Linux kernel* version `$kernelVersion`\n")
        builder.append("*kotlin-rocket-bot* revision `${botRevision.revision}` ( _${botRevision.commitMessage}_ )\n")
        builder.append("*kotlin-rocket-lib* revision `${libraryRevision.revision}` ( _${libraryRevision.commitMessage}_ )\n")
        builder.append("*rocketchat-archive* revision `${archiveVersion.version.revision}` ( _${archiveVersion.version.commitMessage}_ )")

        return listOf(OutgoingMessage(builder.toString()))
    }

    private fun getKernelVersion(): String {
        if (System.getProperty("os.name").lowercase().startsWith("windows")) {
            return "unknown"
        }
        return "uname -a".evalBash(env = emptyMap()).getOrDefault("unknown")
    }

    override fun getHelp(command: String): List<String> =
        listOf("`!version` outputs the version of kotlin-rocket-bot currently running, among several other relevant components")

    override fun getProblems(): List<String> {
        val archiveRevision = ArchiveService().getVersion()

        return if (archiveRevision.version.revision == "unknown" ||
                archiveRevision.version.commitMessage == "unknown" ||
                archiveRevision.mongoDbVersion == "unknown") {
            listOf("Unable to fetch version information from archive")
        }
        else {
            emptyList()
        }
    }
}
