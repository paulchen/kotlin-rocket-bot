package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.handler.PluginProvider
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger

fun main() {
    // TODO store authentication token somewhere
    val host = System.getenv("ROCKETCHAT_HOST") ?: return
    val username = System.getenv("ROCKETCHAT_USERNAME") ?: return
    val password = System.getenv("ROCKETCHAT_PASSWORD") ?: return
    val ignoredChannels = System.getenv("IGNORED_CHANNELS")?.split(",") ?: emptyList()

    Bot(BotConfiguration(host, username, password, ignoredChannels, "paulchen/kotlin-rocket-bot", 8082), Handler()).start()

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
