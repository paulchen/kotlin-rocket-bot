package at.rueckgr.kotlin.rocketbot.util

import at.rueckgr.kotlin.rocketbot.exception.ConfigurationException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.IOException

class ConfigurationProvider {
    private var config: UserConfiguration? = null

    companion object {
        val instance = ConfigurationProvider()
    }

    fun loadConfiguration(configurationFile: String): UserConfiguration {
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

        this.config = config
        return config
    }
}
