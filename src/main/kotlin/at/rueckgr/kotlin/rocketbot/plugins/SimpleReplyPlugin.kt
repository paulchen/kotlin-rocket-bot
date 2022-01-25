package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger

class SimpleReplyPlugin : AbstractPlugin(), Logging {
    override fun getCommands(): List<String> = emptyList()

    override fun handle(message: String): List<OutgoingMessage> {
        val replies = getReplies()
        if (replies == null) {
            logger().debug("Plugin configuration missing")
            return emptyList()
        }

        return replies.flatMap {
            when(it.regex != null &&
                    it.reply != null &&
                    activatePlugin(it.probability) &&
                    message.matches(it.regex.toRegex())) {
                true -> listOf(OutgoingMessage(it.reply))
                false -> emptyList()
            }
        }
    }

    private fun getReplies() = ConfigurationProvider.instance.getConfiguration().plugins?.simpleReply?.replies

    private fun activatePlugin(probability: Int): Boolean =(0..99).random() < probability

    override fun getHelp(command: String): List<String> = when (getReplies()) {
        null -> listOf("Configuration of SimpleReplyPlugin missing")
        else -> emptyList()
    }

    override fun getProblems(): List<String> = emptyList()
}
