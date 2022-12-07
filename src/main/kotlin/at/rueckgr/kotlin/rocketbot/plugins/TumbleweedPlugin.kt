package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.*
import at.rueckgr.kotlin.rocketbot.exception.ConfigurationException
import at.rueckgr.kotlin.rocketbot.util.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class TumbleweedPlugin : AbstractPlugin(), Logging {
    private val nextExecutions = mutableMapOf<String, LocalDateTime>()
    private val lastActivities = mutableMapOf<String, LocalDateTime>()
    private val scheduledFutures = mutableMapOf<String, ScheduledFuture<*>>()
    private val executorService = Executors.newScheduledThreadPool(1)

    override fun getCommands() = emptyList<String>()

    override fun handle(
        channel: RoomMessageHandler.Channel,
        user: RoomMessageHandler.User,
        message: RoomMessageHandler.Message
    ): List<OutgoingMessage> {
        if (getConfiguration()?.tumbleweedChannels?.contains(channel.id) == true) {
            cancelExecution(channel.id)
            scheduleExecution(channel.id)
        }
        return emptyList()
    }

    private fun cancelExecution(roomId: String) {
        scheduledFutures[roomId]?.cancel(false)
        scheduledFutures.remove(roomId)
    }

    private fun scheduleExecution(roomId: String) {
        val seconds = calculateNextExecution(roomId) ?: return
        if (seconds <= 0) {
            postTumbleweed(roomId)
        }
        else {
            val scheduledFuture = executorService.schedule({ postTumbleweed(roomId) }, seconds, TimeUnit.SECONDS)
            scheduledFutures[roomId] = scheduledFuture
        }
    }

    private fun calculateNextExecution(roomId: String): Long? {
        val configuration = try {
            validateConfiguration()
        }
        catch (e: ConfigurationException) {
            return null
        }

        val lastActivity = lastActivities[roomId] ?: return null
        val timeRange = configuration.maximumInactivity!! - configuration.minimumInactivity!!
        val secondsFromLastExecution = (0..timeRange).random()
        val nextExecution = lastActivity.plusSeconds(secondsFromLastExecution)

        val actualNextExecution = if (nextExecution.toLocalTime().isAfter(configuration.dayEnd)) {
            LocalDateTime.of(nextExecution.toLocalDate().plusDays(1), configuration.dayStart)
        }
        else if (nextExecution.toLocalTime().isBefore(configuration.dayStart)) {
            LocalDateTime.of(nextExecution.toLocalDate(), configuration.dayStart)
        }
        else {
            nextExecution
        }
        nextExecutions[roomId] = actualNextExecution

        return ChronoUnit.SECONDS.between(LocalDateTime.now(), actualNextExecution)
    }

    private fun postTumbleweed(roomId: String) {
        try {
            val configuration = validateConfiguration()
            if (configuration.tumbleweedUrls!!.isNotEmpty()) {
                val lastIndex = configuration.tumbleweedUrls.size
                val index = (0..lastIndex).random()
                val url = configuration.tumbleweedUrls[index]
                Bot.webserviceMessageQueue.add(WebserviceMessage(roomId, null, url))
            }
        }
        catch (e: ConfigurationException) {
            /* don't post tumbleweed */
        }

        lastActivities[roomId] = LocalDateTime.now()
        scheduleExecution(roomId)
    }

    override fun getHelp(command: String) = emptyList<String>()

    override fun reinit() {
        HashMap(scheduledFutures).forEach { cancelExecution(it.key) }
        nextExecutions.clear()
        lastActivities.clear()

        init()
    }

    override fun init() {
        val configuration = try {
            validateConfiguration()
        }
        catch (e: ConfigurationException) {
            return
        }

        configuration
            .tumbleweedChannels!!
            .forEach {
                val lastActivity = fetchLastActivity(it)
                if (lastActivity != null) {
                    lastActivities[it] = toLocalDateTime(lastActivity)
                    scheduleExecution(it)
                }
            }

        super.init()
    }

    private fun fetchLastActivity(roomId: String) = ArchiveService().getChannelInfo(roomId)?.lastActivity

    override fun getProblems(): List<String> {
        val problems = mutableListOf<String>()
        try {
            validateConfiguration()
        }
        catch (e: ConfigurationException) {
            problems.add(e.message ?: "Configuration error")
        }

        nextExecutions
            .filter { it.value.isBefore(LocalDateTime.now()) }
            .map { "Next execution for channel ${it.key} is in the past: ${it.value}" }
            .forEach { problems.add(it) }

        return problems
    }

    private fun validateConfiguration(): TumbleweedPluginConfiguration {
        val configuration = getConfiguration()
        if (configuration?.tumbleweedChannels == null ||
            configuration.tumbleweedUrls.isNullOrEmpty() ||
            configuration.minimumInactivity == null ||
            configuration.maximumInactivity == null ||
            configuration.dayStart == null ||
            configuration.dayEnd == null
        ) {
            throw ConfigurationException(0, "Configuration is incomplete")
        }
        if (configuration.minimumInactivity > configuration.maximumInactivity) {
            throw ConfigurationException(0, "Configuration contains invalid range for inactivity")
        }
        if (configuration.dayStart.isAfter(configuration.dayEnd)) {
            throw ConfigurationException(0, "Configuration contains invalid range for day")
        }
        return configuration
    }

    private fun getConfiguration() = ConfigurationProvider.getConfiguration().plugins?.tumbleweed

    override fun getAdditionalStatus(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        lastActivities
            .map { "last activity in room ${it.key}" to it.value.toString() }
            .forEach { result[it.first] = it.second }
        nextExecutions
            .map { "next execution for room ${it.key}" to it.value.toString() }
            .forEach { result[it.first] = it.second }
        return super.getAdditionalStatus()
    }
}

