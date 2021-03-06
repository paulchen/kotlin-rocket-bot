package at.rueckgr.kotlin.rocketbot.util

data class UserConfiguration(
    val general: GeneralConfiguration?,
    val plugins: PluginsConfiguration?,
    val webservice: WebserviceConfiguration?,
    val database: DatabaseConfiguration?
)

data class GeneralConfiguration(
    val host: String?,
    val username: String?,
    val password: String?,
    val ignoredChannels: List<String>?,
    val botId: String?
)

data class PluginsConfiguration(
    val simpleReply: SimpleReplyPluginConfiguration?,
    val admin: AdminPluginConfiguration?,
    val soccer: SoccerPluginConfiguration?
)

data class SimpleReplyPluginConfiguration(
    val replies: List<SimpleReply>?
)

data class AdminPluginConfiguration(
    val admins: List<String>
)

data class SimpleReply(
    val regex: String?,
    val reply: String?,
    val probability: Int,
    val replyToBots: Boolean,
    val stopProcessing: Boolean
)

data class SoccerPluginConfiguration(
    val leagueId: Long?,
    val season: Int?,
    val rounds: List<String>?,
    val apiKey: String?,
    val notificationChannels: List<String>?,
    val matchesToShow: Int?,
    val username: String?
)

data class WebserviceConfiguration(
    val users: List<WebserviceUser>?
)

data class WebserviceUser(
    val username: String?,
    val password: String?
)

data class DatabaseConfiguration(
    val host: String?,
    val database: String?,
    val username: String?,
    val password: String?
)
