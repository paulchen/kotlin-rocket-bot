package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.EventHandler
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger

class SimpleReplyPlugin : AbstractPlugin(), Logging {
    override fun getCommands(): List<String> = emptyList()

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        val replies = getReplies()
        if (replies == null) {
            logger().debug("Plugin configuration missing")
            return emptyList()
        }

        var stopProcessing = false
        return replies.flatMap {
            when(!stopProcessing &&
                    it.regex != null &&
                    it.reply != null &&
                    (it.replyToBots || !message.botMessage) &&
                    activatePlugin(it.probability) &&
                    message.message.matches(it.regex.toRegex())) {
                true -> {
                    stopProcessing = it.stopProcessing
                    listOf(OutgoingMessage(it.reply))
                }
                false -> emptyList()
            }
        }
    }

    private fun getReplies() = ConfigurationProvider.getConfiguration().plugins?.simpleReply?.replies

    private fun activatePlugin(probability: Int): Boolean =(0..99).random() < probability

    override fun getHelp(command: String): List<String> = emptyList()

    override fun getProblems(): List<String> = when (getReplies()) {
        null -> listOf("Configuration of SimpleReplyPlugin missing")
        else -> emptyList()
    }
}
