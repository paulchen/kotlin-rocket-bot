package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.EventHandler
import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.SeriousModeService
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger

class SeriousModePlugin : AbstractPlugin(), Logging {
    override fun getCommands() = listOf("srs", "unsrs")

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        when(message.message) {
            "!srs" -> activateSeriousMode(channel.id)
            "!unsrs" -> deactivateSeriousMode(channel.id)
        }

        return emptyList()
    }

    private fun activateSeriousMode(channel: String) {
        val endDate = SeriousModeService().activateSeriousMode(channel)

        logger().debug("Serious mode for $channel active until $endDate")
    }

    private fun deactivateSeriousMode(channel: String) {
        if (SeriousModeService().deactivateSeriousMode(channel)) {
            logger().debug("Serious mode for $channel deactivated")
        }
        else {
            logger().debug("Serious mode for $channel was not active")
        }
    }

    override fun getHelp(command: String) = listOf<String>(
//        "`!srs` activates _serious mode_ for a certain period of time",
//        "`!unsrs` deactivates _serious mode_"
    )

    override fun getChannelTypes() = listOf(EventHandler.ChannelType.CHANNEL)

    override fun getProblems() = emptyList<String>()

    override fun getAdditionalStatus() = SeriousModeService()
        .getSeriousModeData()
        .flatMap {
            listOf(
                "startDate for ${it.channelId}" to it.startDate.toString(),
                "endDate for ${it.channelId}" to it.endDate.toString()
            )
        }
        .toMap()
}
