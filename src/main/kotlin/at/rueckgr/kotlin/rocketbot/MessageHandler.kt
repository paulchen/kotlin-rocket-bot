package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.handler.PluginProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logExceptions
import at.rueckgr.kotlin.rocketbot.util.logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MessageHandler : EventHandler, Logging {
    override fun handleRoomMessage(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        val messageWithoutQuote = EventHandler.Message(removeQuote(message.message), message.botMessage)
        if (!messageWithoutQuote.message.startsWith("!")) {
            logger().debug("Message contains no command, applying general plugins")
            return applyGeneralPlugins(channel, user, messageWithoutQuote)
        }

        val command = messageWithoutQuote.message.split(" ")[0].substring(1)
        logger().debug("Message contains command: {}", command)

        val commandPlugins = PluginProvider.getByCommand(command)
        if (commandPlugins.isNotEmpty()) {
            return commandPlugins
                .filter { !message.botMessage || it.handleBotMessages() }
                .filter { it.getChannelTypes().contains(channel.type) }
                .flatMap { it.handle(channel, user, messageWithoutQuote) }
        }

        logger().debug("No handler for command {} found, applying general plugins", command)
        return applyGeneralPlugins(channel, user, messageWithoutQuote)
    }

    override fun handleOwnMessage(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> = PluginProvider.getAllPlugins()
            .filter { it.getChannelTypes().contains(channel.type) }
            .flatMap { it.handleOwnMessage(channel, user, message) }

    override fun botInitialized() {
        runBlocking {
            PluginProvider
                .getAllPlugins()
                .forEach { plugin ->
                    launch {
                        logExceptions {
                            plugin.init()
                        }
                    }
                }
        }
    }

    private fun applyGeneralPlugins(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message) = PluginProvider.getGeneralPlugins()
            .filter { !message.botMessage || it.handleBotMessages() }
            .filter { it.getChannelTypes().contains(channel.type) }
            .flatMap { it.handle(channel, user, message) }

    private fun removeQuote(message: String): String {
        return message.replace("""^\[[^]]*]\([^)]*\)""".toRegex(), "").trim()
    }
}
