package at.rueckgr.kotlin.rocketbot.util

import at.rueckgr.kotlin.rocketbot.exception.ConfigurationException
import at.rueckgr.kotlin.rocketbot.handler.PluginProvider
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds

object ConfigurationProvider : Logging {
    private var config: UserConfiguration? = null
    private var configurationFile: String? = null

    fun loadConfiguration(configurationFile: String): UserConfiguration {
        this.configurationFile = configurationFile
        this.config = this.reloadConfiguration()
        return this.config!!
    }

    private fun reloadConfiguration(): UserConfiguration {
        val file = File(configurationFile)
        if (!file.exists()) {
            throw ConfigurationException(1, "Configuration file $configurationFile not found")
        }

        val config = try {
            ObjectMapper(YAMLFactory())
                .findAndRegisterModules()
                .readValue(file, UserConfiguration::class.java)
        }
        catch (e: IOException) {
            throw ConfigurationException(2, "Error reading configuration file $configurationFile: ${e.message}")
        }

        val general = config.general
        if (StringUtils.isBlank(general?.host) ||
            StringUtils.isBlank(general?.username) ||
            StringUtils.isBlank(general?.password) ||
            StringUtils.isBlank(general?.botId)) {
            throw ConfigurationException(3, "Configuration is incomplete")
        }


        logger().debug("New configuration: {}", config)

        this.config = config
        return config
    }

    fun checkForConfigurationUpdates() {
        val path = File(File(configurationFile).parent).toPath()
        val watchService = FileSystems.getDefault().newWatchService()

        while (true) {
            val pathKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
            watchService.take()

            logger().info("Configuration file has changed, reloading now")

            Thread.sleep(1000)

            try {
                reloadConfiguration()
                PluginProvider.reinit()
            }
            catch (e: Throwable) {
                logger().error("Exception while trying to reload configuration", e)
            }

            pathKey.cancel()
        }
    }

    fun getConfiguration(): UserConfiguration = this.config!!

    fun getSoccerConfiguration(): SoccerPluginConfiguration = this.config?.plugins?.soccer!!
}
