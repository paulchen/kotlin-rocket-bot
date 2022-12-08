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
        channel: EventHandler.Channel,
        user: EventHandler.User,
        message: EventHandler.Message
    ): List<OutgoingMessage> {
        if (getChannelIds(getConfiguration()?.tumbleweedChannels)?.contains(channel.id) == true) {
            lastActivities[channel.id] = LocalDateTime.now()
            cancelExecution(channel.id)
            scheduleExecution(channel.id)
        }
        return emptyList()
    }

    private fun cancelExecution(roomId: String) {
        logger().debug("Cancelling execution for room {}", roomId)

        scheduledFutures[roomId]?.cancel(false)
        scheduledFutures.remove(roomId)
    }

    private fun scheduleExecution(roomId: String) {
        val seconds = calculateNextExecution(roomId) ?: return
        if (seconds <= 0) {
            logger().debug("Next execution in channel {} is overdue ({} seconds in the past), immediately posting tumbleweed", roomId, seconds)
            postTumbleweed(roomId)
        }
        else {
            logger().debug("Scheduling next execution in channel {} for {} (in {} seconds)", roomId, nextExecutions[roomId], seconds)

            val scheduledFuture = executorService.schedule({ logExceptions { postTumbleweed(roomId) } }, seconds, TimeUnit.SECONDS)
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
        val secondsFromLastExecution = (0..timeRange).random() + configuration.minimumInactivity
        logger().debug("Next execution {} seconds after last activity (range: {} to {} seconds)",
            secondsFromLastExecution, configuration.minimumInactivity, configuration.maximumInactivity)
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

        logger().debug("Calculated next execution for channel {} at {}", roomId, actualNextExecution)

        return ChronoUnit.SECONDS.between(LocalDateTime.now(), actualNextExecution)
    }

    private fun postTumbleweed(roomId: String) {
        try {
            val configuration = validateConfiguration()
            if (configuration.tumbleweedUrls!!.isNotEmpty()) {
                val lastIndex = configuration.tumbleweedUrls.size - 1
                val index = (0..lastIndex).random()
                val url = configuration.tumbleweedUrls[index]

                logger().debug("Posting tumbleweed {} to channel {}", url, roomId)

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

        getChannelIds(configuration.tumbleweedChannels)
            ?.forEach {
                val lastActivity = fetchLastActivity(it)
                logger().debug("Fetched last activity in room {} from archive: {}", it, lastActivity)
                if (lastActivity != null) {
                    lastActivities[it] = toLocalDateTime(lastActivity)
                    scheduleExecution(it)
                }
            }

        super.init()
    }

    private fun getChannelIds(channelNames: List<String>?) = channelNames?.mapNotNull { Bot.knownChannelNamesToIds[it] }

    private fun getChannelName(channelId: String) = Bot.knownChannelNamesToIds.entries.firstOrNull { it.value == channelId }?.key ?: channelId

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
            .map { "Next execution for channel ${getChannelName(it.key)} is in the past: ${it.value}" }
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
            .map { "last activity in room ${getChannelName(it.key)}" to it.value.toString() }
            .forEach { result[it.first] = it.second }
        nextExecutions
            .map { "next execution for room ${getChannelName(it.key)}" to it.value.toString() }
            .forEach { result[it.first] = it.second }
        return result
    }
}
