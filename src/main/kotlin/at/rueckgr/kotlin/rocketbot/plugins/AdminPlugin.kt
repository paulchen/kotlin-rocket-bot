package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.Bot
import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.EventHandler
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import java.text.SimpleDateFormat

class AdminPlugin : AbstractPlugin(), Logging {
    override fun getCommands(): List<String> = emptyList()

    override fun getChannelTypes() = listOf(EventHandler.ChannelType.DIRECT)

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        val admins = ConfigurationProvider.getConfiguration().plugins?.admin?.admins ?: emptyList()
        if (!admins.contains(user.id)) {
            return emptyList()
        }

        return when (message.message) {
            "!status" -> listOf(OutgoingMessage(getStatus()))
            "!config" -> listOf(OutgoingMessage(getConfig()))
            else -> emptyList()
        }
    }

    private fun getStatus(): String {
        val status = Bot.statusService.getStatus()
        val json = ObjectMapper()
            .findAndRegisterModules()
            .setDateFormat(SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS"))
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(status)
        return "```\n$json\n```"
    }

    private fun getConfig(): String {
        val tree: JsonNode = ObjectMapper()
            .findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .valueToTree(ConfigurationProvider.getConfiguration())
        filterPasswords(tree)

        val json = tree.toPrettyString()
        return "```\n$json\n```"
    }

    private fun filterPasswords(node: JsonNode) {
        if (node is ObjectNode && node.has("password")) {
            node.put("password", "********")
        }
        node.elements().forEach { filterPasswords(it) }
    }
    override fun getHelp(command: String): List<String> = emptyList()

    override fun getProblems(): List<String> = emptyList()
}
