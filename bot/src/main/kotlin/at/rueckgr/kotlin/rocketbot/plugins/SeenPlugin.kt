package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.*
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.formatUsername
import at.rueckgr.kotlin.rocketbot.util.time.DateTimeDifferenceCalculator
import at.rueckgr.kotlin.rocketbot.util.time.TimeUnit
import at.rueckgr.kotlin.rocketbot.util.toLocalDateTime
import java.time.LocalDateTime




class SeenPlugin : AbstractPlugin() {
    override fun getCommands(): List<String> = listOf("seen")

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        val rawUsername = stripCommand(message.message) ?: return emptyList()
        val seenUsername = if (rawUsername.startsWith("@")) {
            rawUsername.substring(1)
        }
        else {
            rawUsername
        }

        val userDetails: UserDetails? = ArchiveService().getUserByUsername(seenUsername)

        val response = if (userDetails == null) {
            "Sorry, I don't know about *${formatUsername(seenUsername)}*."
        }
        else if (userDetails.user.mostRecentMessage?.timestamp == null) {
            "*${formatUsername(userDetails.user.username)}* has never been active."
        }
        else {
            val localDateTime = toLocalDateTime(userDetails.user.mostRecentMessage.timestamp)
            val timestamp = TimestampFormatter().formatTimestamp(localDateTime)
            val ago = DateTimeDifferenceCalculator().formatTimeDifference(LocalDateTime.now(), localDateTime, listOf(TimeUnit.WEEK))

            val channelId = userDetails.user.mostRecentMessage.rid
            var channelName = Bot.subscriptionService.getChannelNameById(channelId)
            if (channelName == null) {
                channelName = ArchiveService().getChannelInfo(channelId)!!.name
            }

            val generalConfiguration = ConfigurationProvider.getConfiguration().general!!
            val host = generalConfiguration.host

            val messageId = userDetails.user.mostRecentMessage.id
            val messageLink = "https://$host/channel/${channelName}?msg=$messageId"
            val archiveLink = generalConfiguration.archiveLink?.let { "; [archive](${generalConfiguration.archiveLink}/$channelId/$messageId)" } ?: ""

            "*${formatUsername(userDetails.user.username)}* wrote their [last message]($messageLink) at $timestamp ($ago)$archiveLink."
        }
        return listOf(OutgoingMessage(response))
    }

    override fun getHelp(command: String): List<String> =
        listOf("`!seen <nickname>` shows the time when the given user was active for the last time")

    override fun getProblems() = emptyList<String>()
}
