package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.exception.ConfigurationException
import at.rueckgr.kotlin.rocketbot.handler.PluginProvider
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import kotlin.system.exitProcess

fun main() {
    // TODO store authentication token somewhere

    val config = try {
        ConfigurationProvider.instance.loadConfiguration("/config/kotlin-rocket-bot.yaml")
    }
    catch (e: ConfigurationException) {
        println(e.message)
        exitProcess(e.exitCode)
    }

    val general = config.general
    Bot(
        BotConfiguration(general!!.host!!, general.username!!, general.password!!, general.ignoredChannels!!, general.botId!!, 8082),
        Handler()
    ).start()
}

class Handler : RoomMessageHandler, Logging {
    override fun handle(username: String, message: String): List<String> {
        val messageWithoutQuote = removeQuote(message)
        if (!messageWithoutQuote.startsWith("!")) {
            logger().debug("Message contains no command, applying general plugins")
            return PluginProvider
                .instance
                .getGeneralPlugins()
                .flatMap { it.handle(messageWithoutQuote) }
        }

        val command = messageWithoutQuote.split(" ")[0].substring(1)
        logger().debug("Message contains command: {}", command)
        return PluginProvider
            .instance
            .getByCommand(command)
            .flatMap { it.handle(messageWithoutQuote) }
    }

    private fun removeQuote(message: String): String {
        return message.replace("""^\[[^]]*]\([^)]*\)""".toRegex(), "").trim()
    }
}
