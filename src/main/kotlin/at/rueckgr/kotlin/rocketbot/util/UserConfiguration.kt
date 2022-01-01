package at.rueckgr.kotlin.rocketbot.util

data class UserConfiguration(
    val general: GeneralConfiguration?
)

data class GeneralConfiguration(
    val host: String?,
    val username: String?,
    val password: String?,
    val ignoredChannels: List<String>?,
    val botId: String?
)
