package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.exception.ConfigurationException
import at.rueckgr.kotlin.rocketbot.handler.PluginProvider
import at.rueckgr.kotlin.rocketbot.soccer.SoccerUpdateService
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import kotlinx.coroutines.*
import kotlin.system.exitProcess

fun main() {
    // TODO store authentication token somewhere

    val configurationFile = "/config/kotlin-rocket-bot.yaml"

    val config = try {
        ConfigurationProvider.loadConfiguration(configurationFile)
    }
    catch (e: ConfigurationException) {
        println(e.message)
        exitProcess(e.exitCode)
    }

    val general = config.general
    runBlocking {
        launch {
            withContext(Dispatchers.IO) {
                ConfigurationProvider.checkForConfigurationUpdates(configurationFile)
            }
        }
        launch {
            Bot(
                BotConfiguration(general!!.host!!, general.username!!, general.password!!, general.ignoredChannels!!, general.botId!!, 8082),
                Handler(),
                UserValidator(),
                BotHealthChecker()
            ).start()
        }
        launch {
            SoccerUpdateService().scheduleImmediateDailyUpdate()
        }
    }
}

class Handler : RoomMessageHandler, Logging {
    override fun handle(channel: RoomMessageHandler.Channel, user: RoomMessageHandler.User, message: RoomMessageHandler.Message): List<OutgoingMessage> {
        val messageWithoutQuote = RoomMessageHandler.Message(removeQuote(message.message), message.botMessage)
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
                .flatMap { it.handle(channel, user, messageWithoutQuote) }
        }

        logger().debug("No handler for command {} found, applying general plugins", command)
        return applyGeneralPlugins(channel, user, messageWithoutQuote)
    }

    private fun applyGeneralPlugins(channel: RoomMessageHandler.Channel, user: RoomMessageHandler.User, message: RoomMessageHandler.Message) = PluginProvider
            .getGeneralPlugins()
            .filter { !message.botMessage || it.handleBotMessages() }
            .flatMap { it.handle(channel, user, message) }

    private fun removeQuote(message: String): String {
        return message.replace("""^\[[^]]*]\([^)]*\)""".toRegex(), "").trim()
    }
}
