package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger

class SimpleReplyPlugin : AbstractPlugin(), Logging {
    override fun getCommands(): List<String> = emptyList()

    override fun handle(message: String) = handle(message, false)

    override fun handle(message: String, botMessage: Boolean): List<OutgoingMessage> {
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
                    (it.replyToBots || !botMessage) &&
                    activatePlugin(it.probability) &&
                    message.matches(it.regex.toRegex())) {
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

    override fun getHelp(command: String): List<String> = when (getReplies()) {
        null -> listOf("Configuration of SimpleReplyPlugin missing")
        else -> emptyList()
    }

    override fun getProblems(): List<String> = emptyList()
}
