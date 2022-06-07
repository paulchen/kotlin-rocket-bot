package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.RoomMessageHandler
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

class AdminPlugin : AbstractPlugin(), Logging {
    override fun getCommands(): List<String> = emptyList()

    override fun handle(channel: RoomMessageHandler.Channel, user: RoomMessageHandler.User, message: RoomMessageHandler.Message): List<OutgoingMessage> {
        if (channel.type != RoomMessageHandler.ChannelType.DIRECT) {
            return emptyList()
        }

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

    private fun getStatus(): String = "" // TODO

    private fun getConfig(): String {
        val tree: JsonNode = ObjectMapper()
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
