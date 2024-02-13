package at.rueckgr.kotlin.rocketbot.util

import java.time.LocalTime

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
    val botId: String?,
    val logRequests: Boolean?
)

data class PluginsConfiguration(
    val mutePlugins: List<String>,
    val simpleReply: SimpleReplyPluginConfiguration?,
    val admin: AdminPluginConfiguration?,
    val soccer: SoccerPluginConfiguration?,
    val tumbleweed: TumbleweedPluginConfiguration?,
    val seriousMode: SeriousModePluginConfiguration?
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
    var apiUrl: String?,
    val apiKey: String?,
    val notificationChannels: List<String>?,
    val matchesToShow: Int?,
    val username: String?
)

data class TumbleweedPluginConfiguration(
    val tumbleweedChannels: List<String>?,
    val tumbleweedUrls: List<String>?,
    val minimumInactivity: Long?,
    val maximumInactivity: Long?,
    val dayStart: LocalTime?,
    val dayStartWeekend: LocalTime?,
    val dayEnd: LocalTime?,
    val holidayCountry: String?
)

data class SeriousModePluginConfiguration(
    val duration: Long?
)

data class WebserviceConfiguration(
    val users: List<WebserviceUser>?
)

data class WebserviceUser(
    val username: String?,
    val password: String?
)

data class DatabaseConfiguration(
    val url: String?,
    val username: String?,
    val password: String?
)
