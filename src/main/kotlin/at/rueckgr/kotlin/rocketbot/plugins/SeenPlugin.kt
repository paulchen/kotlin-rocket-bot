package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.*
import at.rueckgr.kotlin.rocketbot.util.formatUsername
import java.time.LocalDateTime
import java.time.ZoneId




class SeenPlugin : AbstractPlugin() {
    override fun getCommands(): List<String> = listOf("seen")

    override fun handle(message: String): List<OutgoingMessage> {
        val rawUsername = stripCommand(message) ?: return emptyList()
        val username = if (rawUsername.startsWith("@")) {
            rawUsername.substring(1)
        }
        else {
            rawUsername
        }

        val userDetails: UserDetails? = ArchiveService().getUserDetails(username)

        val response = if (userDetails == null) {
            "Sorry, I don't know about *${formatUsername(username)}*."
        }
        else if (userDetails.user.timestamp == null) {
            "*${formatUsername(userDetails.user.username)}* has never been active."
        }
        else {
            val localDateTime = LocalDateTime.ofInstant(userDetails.user.timestamp.toInstant(), ZoneId.systemDefault())
            val timestamp = TimestampFormatter().formatTimestamp(localDateTime)
            val ago = DateTimeDifferenceCalculator().formatTimeDifference(LocalDateTime.now(), localDateTime)
            "*${formatUsername(userDetails.user.username)}* wrote their last message at $timestamp ($ago)."
        }
        return listOf(OutgoingMessage(response))
    }

    override fun getHelp(command: String): List<String> =
        listOf("`!seen <nickname>` shows the time when the given user was active for the last time")
}
