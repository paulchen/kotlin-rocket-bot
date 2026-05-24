package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.ArchiveService
import at.rueckgr.kotlin.rocketbot.Bot
import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.EventHandler
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.LibraryVersion
import at.rueckgr.kotlin.rocketbot.util.RestApiClient
import at.rueckgr.kotlin.rocketbot.util.VersionHelper
import at.rueckgr.kotlin.rocketbot.util.time.DateTimeDifferenceCalculator
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class VersionPlugin : AbstractPlugin() {
    private val botRevision = VersionHelper.getVersion()
    private val libraryRevision = LibraryVersion.instance.getVersion()

    override fun getCommands(): List<String> = listOf("uptime", "version")

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {

        return when (message.message.lowercase()) {
            "!uptime" -> listOf(OutgoingMessage(getUptime()))
            "!version" -> listOf(OutgoingMessage(getVersions()))
            else -> emptyList()
        }
    }

    private fun getUptime(): String {
        val username = ConfigurationProvider.getConfiguration().general?.username
        val startDate = Bot.statusService.getStatus().startDate ?: return "The start date of *$username* is unknown"

        val timeDifference = DateTimeDifferenceCalculator().formatTimeDifference(startDate, LocalDateTime.now())
        val formattedStartDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(startDate)

        return "*$username* was started at $formattedStartDate ($timeDifference)"
    }

    private fun getVersions(): String {
        val archiveVersion = ArchiveService().getVersion()
        val rocketchatVersion = RestApiClient(Bot.host).getInstanceVersion() ?: "unknown"
        val dockerVersion = System.getenv("DOCKER_VERSION")?.replace("Docker version ", "") ?: "unknown"
        val kernelVersion = System.getenv("LINUX_VERSION") ?: "unknown"
        val mongoImageDigest = System.getenv("MONGO_IMAGE_DIGEST") ?: "unknown"

        val builder = StringBuilder()

        builder.append("*Rocket.Chat* version `$rocketchatVersion`\n")
        builder.append("*MongoDB* version `${archiveVersion.mongoDbVersion}` (Docker image: `$mongoImageDigest`)\n")
        builder.append("*Docker* version `$dockerVersion`\n")
        builder.append("*Linux kernel* version `$kernelVersion`\n")
        builder.append("*kotlin-rocket-bot* revision `${botRevision.revision}` ( _${botRevision.commitMessage}_ )\n")
        builder.append("*kotlin-rocket-lib* revision `${libraryRevision.revision}` ( _${libraryRevision.commitMessage}_ )\n")
        builder.append("*rocketchat-archive* revision `${archiveVersion.version.revision}` ( _${archiveVersion.version.commitMessage}_ )")

        return builder.toString()
    }

    override fun getHelp(command: String): List<String> =
        listOf(
            "`!uptime` reports the bot’s start time and the elapsed time since startup",
            "`!version` outputs the version of kotlin-rocket-bot currently running, among several other relevant components"
        )

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
