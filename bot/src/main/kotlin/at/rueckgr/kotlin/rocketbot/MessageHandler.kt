package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.handler.PluginProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logExceptions
import at.rueckgr.kotlin.rocketbot.util.logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MessageHandler : EventHandler, Logging {
    override fun handleRoomMessage(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        val seriousMode = SeriousModeService().isInSeriousMode(channel.id)
        val messageWithoutQuote = EventHandler.Message(removeQuotes(message.message), message.botMessage)
        if (!messageWithoutQuote.message.startsWith("!")) {
            logger().debug("Message contains no command, applying general plugins")
            return applyGeneralPlugins(channel, user, messageWithoutQuote, seriousMode)
        }

        val command = messageWithoutQuote.message.split(" ")[0].substring(1)
        logger().debug("Message contains command: {}", command)

        val commandPlugins = PluginProvider.getByCommand(command)
        if (commandPlugins.isNotEmpty()) {
            return commandPlugins
                .filter { !message.botMessage || it.handleBotMessages() }
                .filter { it.getChannelTypes().contains(channel.type) }
                .flatMap {
                    if(seriousMode && !it.runInSeriousMode()) {
                        logger().info("Not applying plugin ${it.javaClass.name} as ${channel.id} is in serious mode")
                        emptyList()
                    }
                    else {
                        it.handle(channel, user, messageWithoutQuote)
                    }
                }
        }

        logger().debug("No handler for command {} found, applying general plugins", command)
        return applyGeneralPlugins(channel, user, messageWithoutQuote, seriousMode)
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

    private fun applyGeneralPlugins(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message, seriousMode: Boolean) = PluginProvider.getGeneralPlugins()
            .filter { !message.botMessage || it.handleBotMessages() }
            .filter { it.getChannelTypes().contains(channel.type) }
            .flatMap {
                if(seriousMode && !it.runInSeriousMode()) {
                    logger().info("Not applying plugin ${it.javaClass.simpleName} as ${channel.id} is in serious mode")
                    emptyList()
                }
                else {
                    it.handle(channel, user, message)
                }
        }

    fun removeQuotes(message: String): String {
        return message.replace("""^(\[[^]]*]\([^)]*\)\s*)+""".toRegex(), "").trim()
    }
}
