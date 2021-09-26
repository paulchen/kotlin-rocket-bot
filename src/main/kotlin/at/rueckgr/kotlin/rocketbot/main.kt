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

    Bot(BotConfiguration(host, username, password, ignoredChannels, "paulchen/kotlin-rocket-bot", 8080), Handler()).start()

}

class Handler : RoomMessageHandler, Logging {
    override fun handle(username: String, message: String): List<String> {
        if (!message.startsWith("!")) {
            logger().debug("Message contains no command, ignoring")
            return emptyList()
        }

        val command = message.split(" ")[0].substring(1)
        return PluginProvider
            .instance
            .getByCommand(command)
            .flatMap { it.handle(message) }
    }
}
