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
    override fun handle(username: String, message: String, botMessage: Boolean): List<OutgoingMessage> {
        val messageWithoutQuote = removeQuote(message)
        if (!messageWithoutQuote.startsWith("!")) {
            logger().debug("Message contains no command, applying general plugins")
            return applyGeneralPlugins(username, messageWithoutQuote, botMessage)
        }

        val command = messageWithoutQuote.split(" ")[0].substring(1)
        logger().debug("Message contains command: {}", command)

        val commandPlugins = PluginProvider.getByCommand(command)
        if (commandPlugins.isNotEmpty()) {
            return commandPlugins
                .filter { !botMessage || it.handleBotMessages() }
                .flatMap { it.handle(username, messageWithoutQuote, botMessage) }
        }

        logger().debug("No handler for command {} found, applying general plugins", command)
        return applyGeneralPlugins(username, messageWithoutQuote, botMessage)
    }

    private fun applyGeneralPlugins(username: String, messageWithoutQuote: String, botMessage: Boolean) = PluginProvider
            .getGeneralPlugins()
            .filter { !botMessage || it.handleBotMessages() }
            .flatMap { it.handle(username, messageWithoutQuote, botMessage) }

    private fun removeQuote(message: String): String {
        return message.replace("""^\[[^]]*]\([^)]*\)""".toRegex(), "").trim()
    }
}
