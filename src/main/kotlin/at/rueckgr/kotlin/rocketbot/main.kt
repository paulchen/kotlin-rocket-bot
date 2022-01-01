package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.handler.PluginProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.UserConfiguration
import at.rueckgr.kotlin.rocketbot.util.logger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

fun main() {
    // TODO store authentication token somewhere

    val configurationFile = "/config/kotlin-rocket-bot.yaml"
    val file = File(configurationFile)
    if (!file.exists()) {
        terminate(1, "Configuration file $configurationFile not found")
    }

    val config = try {
        ObjectMapper(YAMLFactory())
            .findAndRegisterModules()
            .readValue(file, UserConfiguration::class.java)
    }
    catch (e: IOException) {
        terminate(2, "Error reading configuration file $configurationFile: ${e.message}")
        return
    }

    val general = config.general
    if (StringUtils.isBlank(general?.host) ||
            StringUtils.isBlank(general?.username) ||
            StringUtils.isBlank(general?.password) ||
            StringUtils.isBlank(general?.botId)) {
        terminate(3, "Configuration is incomplete")
    }

    Bot(
        BotConfiguration(general!!.host!!, general.username!!, general.password!!, general.ignoredChannels!!, general.botId!!, 8082),
        Handler()
    ).start()
}

fun terminate(exitCode: Int, errorMessage: String) {
    println(errorMessage)
    exitProcess(exitCode)
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
