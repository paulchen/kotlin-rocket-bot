package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.ArchiveService
import at.rueckgr.kotlin.rocketbot.DateTimeDifferenceCalculator
import at.rueckgr.kotlin.rocketbot.TimestampFormatter
import at.rueckgr.kotlin.rocketbot.UserDetails
import java.time.LocalDateTime
import java.time.ZoneId




class SeenPlugin : AbstractPlugin() {
    override fun getCommands(): List<String> = listOf("seen")

    override fun handle(message: String): List<String> {
        // TODO create utility class for this
        val pos = message.indexOf(" ")
        if (pos < 0) {
            return emptyList()
        }
        val username = message.substring(pos + 1)

        val userDetails: UserDetails? = ArchiveService().getUserDetails(username)

        val response = if (userDetails == null) {
            "Sorry, I don't know about *$username*."
        }
        else if (userDetails.user.timestamp == null) {
            "*${userDetails.user.username}* has never been active."
        }
        else {
            val localDateTime = LocalDateTime.ofInstant(userDetails.user.timestamp.toInstant(), ZoneId.of("Europe/Vienna"))
            val timestamp = TimestampFormatter().formatTimestamp(localDateTime)
            val ago = DateTimeDifferenceCalculator().formatTimeDifference(LocalDateTime.now(), localDateTime)
            "*${userDetails.user.username}* wrote their last message at $timestamp ($ago)."
        }
        return listOf(response)
    }

    override fun getHelp(command: String): List<String> =
        listOf("`!seen <nickname>` shows the time when the given user was active for the last time")
}
