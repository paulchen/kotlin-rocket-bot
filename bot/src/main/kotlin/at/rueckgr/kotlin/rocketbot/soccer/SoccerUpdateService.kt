package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.Bot
import at.rueckgr.kotlin.rocketbot.WebserviceMessage
import at.rueckgr.kotlin.rocketbot.database.Fixture
import at.rueckgr.kotlin.rocketbot.database.FixtureState
import at.rueckgr.kotlin.rocketbot.database.FixtureStatePeriod
import at.rueckgr.kotlin.rocketbot.database.Fixtures
import at.rueckgr.kotlin.rocketbot.util.*
import org.apache.commons.lang3.StringUtils
import org.ktorm.dsl.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.max

object SoccerUpdateService : Logging {
    private var future: ScheduledFuture<*>? = null
    private val executorService = Executors.newScheduledThreadPool(1)

    fun scheduleImmediateDailyUpdate() {
        scheduleDailyUpdate(1)
    }

    private fun runDailyUpdate() {
        if (ConfigurationProvider.getSoccerConfiguration().mode == SoccerPluginMode.DORMANT) {
            scheduleLiveOrDailyUpdate()
            return
        }

        val updateResult = try {
            DataImportService().runDailyUpdate()
        }
        catch (e: Throwable) {
            logger().error("Exception occurred while running daily update", e)
            DataImportService.lastUpdateFailed = true
            emptyList()
        }

        if (hasLiveFixtures(updateResult)) {
            scheduleQuickLiveUpdate()
        }
        else {
            scheduleLiveOrDailyUpdate()
        }
    }

    private fun runLiveUpdate() {
        val soccerConfiguration = ConfigurationProvider.getSoccerConfiguration()
        val username = soccerConfiguration.username
        val emoji = soccerConfiguration.emoji
        val notificationChannels = soccerConfiguration.notificationChannels

        var error = false
        val liveUpdateResult = try {
            DataImportService().runLiveUpdate()
        }
        catch (e: Throwable) {
            logger().error("Exception occurred while running live update", e)
            DataImportService.lastUpdateFailed = true
            error = true
            emptyList()
        }

        if (!error && liveUpdateResult.isEmpty()) {
            scheduleLiveOrDailyUpdate()
        }
        else {
            scheduleQuickLiveUpdate()
        }

        if(notificationChannels?.isEmpty() != false) {
            return
        }

        announceGames(notificationChannels, emoji, username)
        sendNotifications(notificationChannels, emoji, username, liveUpdateResult)
    }

    private fun announceGames(notificationChannels: List<String>, emoji: String?, username: String?) {
        val filteredResults = DataImportService()
            .findLiveFixtures()
            .filter { !it.announced }
        if (filteredResults.isEmpty()) {
            return
        }

        val matches = filteredResults
            .map { MatchTitleService.formatMatchTitle(it) }
            .joinToString("\n") { "- $it" }
        val message = when (filteredResults
            .map { MatchTitleService.formatMatchTitle(it) }.size) {
            1 -> ":mega: *Demnächst stattfindendes Spiel: *\n$matches"
            else -> ":mega: *Demnächst stattfindende Spiele: *\n$matches"
        }

        notificationChannels.forEach { roomName ->
            enqueueMessage(WebserviceMessage(null, roomName, message, null, emoji, username))
        }

        DataImportService().setFixturesToAnnounced(filteredResults)
    }

    private fun sendNotifications(notificationChannels: List<String>, emoji: String?, username: String?, liveUpdateResult: List<ImportFixtureResult>) {
        liveUpdateResult.forEach {
            it.newEvents.forEach { event ->
                notificationChannels.forEach { roomName ->
                    enqueueMessage(createMessage(it.fixture, roomName, event, emoji, username))
                }
            }

            if (it.stateChange != null) {
                notificationChannels.forEach { roomName ->
                    enqueueMessage(createMessage(it.fixture, roomName, it.stateChange, emoji, username))
                }
            }
        }
    }

    private fun enqueueMessage(webserviceMessage: WebserviceMessage) {
        val (validationResult, validatedMessage) = MessageHelper.instance.validateMessage(webserviceMessage)
        if (StringUtils.isNotEmpty(validationResult)) {
            logger().error(validationResult)
            return
        }
        Bot.webserviceMessageQueue.add(validatedMessage)
    }

    fun createMessage(fixture: Fixture, roomName: String, message: String, emoji: String?, username: String?): WebserviceMessage {
        val matchTitleShort = MatchTitleService.formatMatchTitleShort(fixture)
        val formattedMessage = ":mega: $matchTitleShort: $message"

        return WebserviceMessage(null, roomName, formattedMessage, null, emoji, username)
    }

    private fun hasLiveFixtures(updateResult: List<ImportFixtureResult>): Boolean {
        val inOneHour = LocalDateTime.now().plusHours(1)
        val oneHourAgo = LocalDateTime.now().minusHours(1)
        val oneDayAgo = LocalDateTime.now().minusDays(1)

        return updateResult
            .map { it.fixture }
            .filter { it.date.isAfter(oneDayAgo) }
            .any {
                (FixtureState.getByCode(it.status)?.period == FixtureStatePeriod.LIVE)
                    || (it.date.isAfter(oneHourAgo) && it.date.isBefore(inOneHour))
                    || (it.endDate != null && it.endDate!!.isAfter(oneHourAgo))
            }
    }

    private fun scheduleQuickLiveUpdate() {
        scheduleLiveUpdate(30)
    }

    private fun scheduleLiveOrDailyUpdate() {
        val nextDailyUpdate = getNextDailyUpdate()
        val nextLiveUpdate = try {
            getNextLiveUpdate()
        }
        catch (e: DbException) {
            logger().debug("Unable to access database, checking again in one minute")
            schedule({ scheduleLiveOrDailyUpdate() }, 60)
            return
        }

        logger().debug("Next live update would be at {}", nextLiveUpdate)
        logger().debug("Next daily update would be at {}", nextDailyUpdate)

        if (nextLiveUpdate == null || nextDailyUpdate.isBefore(nextLiveUpdate)) {
            scheduleDailyUpdate(getSeconds(nextDailyUpdate))
        }
        else {
            scheduleLiveUpdate(getSeconds(nextLiveUpdate))
        }
    }

    private fun schedule(function: () -> Unit, seconds: Long) {
        synchronized(this) {
            if (this.future != null) {
                this.future!!.cancel(true)
            }
            this.future = executorService.schedule(function, seconds, TimeUnit.SECONDS)
        }
    }

    private fun scheduleDailyUpdate(seconds: Long) {
        DataImportService.nextUpdate = ZonedDateTime.now().plusSeconds(seconds)
        DataImportService.nextUpdateType = UpdateType.DAILY
        logger().debug("Scheduling next daily update for {} (in {} seconds)", DataImportService.nextUpdate, seconds)
        schedule({ handleExceptions { runDailyUpdate() } }, seconds)
    }

    private fun scheduleLiveUpdate(seconds: Long) {
        DataImportService.nextUpdate = ZonedDateTime.now().plusSeconds(seconds)
        DataImportService.nextUpdateType = UpdateType.LIVE
        logger().debug("Scheduling next live update for {} (in {} seconds)", DataImportService.nextUpdate, seconds)
        schedule({ handleExceptions { runLiveUpdate() } }, seconds)
    }

    private fun getNextDailyUpdate(): ZonedDateTime {
        return if (DataImportService.lastUpdateFailed) {
            // if the last update failed,
            // schedule an update to the next full hour
            ZonedDateTime.now().withMinute(0).plusHours(1)
        }
        else {
            val today4am = ZonedDateTime.now().withHour(4).withMinute(0)
            if (ZonedDateTime.now().isBefore(today4am)) {
                today4am
            }
            else {
                today4am.plusDays(1)
            }
        }
    }

    private fun getNextLiveUpdate(): ZonedDateTime? {
        val nextFixture = Db().connection
            .from(Fixtures)
            .select(Fixtures.date)
            .where { Fixtures.date greater LocalDateTime.now() }
            .orderBy(Fixtures.date.asc())
            .limit(1)
            .map { it[Fixtures.date] }
            .firstOrNull()
            ?.minusHours(1) ?: return null
        return ZonedDateTime.of(nextFixture, ZoneId.systemDefault())
    }

    private fun getSeconds(time: ZonedDateTime) = max(30, ChronoUnit.SECONDS.between(ZonedDateTime.now(), time))
}
