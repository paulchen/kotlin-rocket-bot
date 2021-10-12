package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.ArchiveService
import at.rueckgr.kotlin.rocketbot.DateTimeDifferenceCalculator
import at.rueckgr.kotlin.rocketbot.TimestampFormatter
import at.rueckgr.kotlin.rocketbot.UserDetails
import java.time.LocalDateTime
import java.time.ZoneId




class SeenPlugin : AbstractPlugin() {
    private val ZWNBSP = "\uFEFF"

    override fun getCommands(): List<String> = listOf("seen")

    override fun handle(message: String): List<String> {
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
        return listOf(response)
    }

    override fun getHelp(command: String): List<String> =
        listOf("`!seen <nickname>` shows the time when the given user was active for the last time")

    private fun formatUsername(username: String): String {
        // insert ZWNBSP to avoid highlighting
        return username.substring(0, 1) + ZWNBSP + username.substring(1)
    }
}
