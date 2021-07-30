package at.rueckgr.kotlin.rocketbot

fun main() {
    // TODO store authentication token somewhere
    val host = System.getenv("ROCKETCHAT_HOST") ?: return
    val username = System.getenv("ROCKETCHAT_USERNAME") ?: return
    val password = System.getenv("ROCKETCHAT_PASSWORD") ?: return
    val ignoredChannels = System.getenv("IGNORED_CHANNELS")?.split(",") ?: emptyList()

    Bot(BotConfiguration(host, username, password, ignoredChannels)).start()

}

