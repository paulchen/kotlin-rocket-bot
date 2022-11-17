package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.exception.ConfigurationException
import at.rueckgr.kotlin.rocketbot.handler.PluginProvider
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logExceptions
import kotlinx.coroutines.*
import org.apache.commons.lang3.SystemUtils
import java.io.File
import kotlin.system.exitProcess

fun main() {
    Main().run()
}

class Main: Logging {
    fun run() {
        // TODO store authentication token somewhere

        val config = try {
            ConfigurationProvider.loadConfiguration(findConfigurationFile())
        }
        catch (e: ConfigurationException) {
            println(e.message)
            exitProcess(e.exitCode)
        }

        val general = config.general
        runBlocking {
            launch {
                withContext(Dispatchers.IO) {
                    logExceptions {
                        ConfigurationProvider.checkForConfigurationUpdates()
                    }
                }
            }
            launch {
                logExceptions {
                    Bot(
                        BotConfiguration(general!!.host!!, general.username!!, general.password!!, general.ignoredChannels!!, general.botId!!, 8082),
                        MessageHandler(),
                        UserValidator(),
                        BotHealthChecker()
                    ).start()
                }
            }
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

    private fun findConfigurationFile(): String {
        val filename = "kotlin-rocket-bot.yaml"
        val possibleFilenames = listOf(
            "/config/$filename",
            System.getProperty("user.home") + File.separator + filename
        )

        val configurationFileName = possibleFilenames
            .firstOrNull() { File(it).exists() }
            ?: throw ConfigurationException(4, "No configuration file found")

        if (!SystemUtils.IS_OS_WINDOWS && File(configurationFileName).canWrite()) {
            throw ConfigurationException(5, "Configuration file $configurationFileName is writable")
        }
        return configurationFileName
    }
}

