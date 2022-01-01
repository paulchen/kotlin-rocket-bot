package at.rueckgr.kotlin.rocketbot.util

data class UserConfiguration(
    val general: GeneralConfiguration?,
    val plugins: PluginsConfiguration?
)

data class GeneralConfiguration(
    val host: String?,
    val username: String?,
    val password: String?,
    val ignoredChannels: List<String>?,
    val botId: String?
)

data class PluginsConfiguration(
    val simpleReply: SimpleReplyPluginConfiguration?
)

data class SimpleReplyPluginConfiguration(
    val replies: List<SimpleReply>?
)

data class SimpleReply(
    val regex: String?,
    val reply: String?,
    val probability: Int
)
