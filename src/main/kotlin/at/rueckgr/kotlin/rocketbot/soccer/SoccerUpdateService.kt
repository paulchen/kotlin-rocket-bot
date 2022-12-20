package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.Bot
import at.rueckgr.kotlin.rocketbot.WebserviceMessage
import at.rueckgr.kotlin.rocketbot.database.Fixture
import at.rueckgr.kotlin.rocketbot.database.FixtureState
import at.rueckgr.kotlin.rocketbot.database.FixtureStatePeriod
import at.rueckgr.kotlin.rocketbot.database.Fixtures
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Db
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import org.ktorm.dsl.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max

class SoccerUpdateService : Logging {
    private val executorService = Executors.newScheduledThreadPool(1)

    fun scheduleImmediateDailyUpdate() {
        scheduleDailyUpdate(1)
    }

    private fun runDailyUpdate() {
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

        announceGames(notificationChannels, username)
        sendNotifications(notificationChannels, username, liveUpdateResult)
    }

    private fun announceGames(notificationChannels: List<String>, username: String?) {
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
            1 -> ":mega: *Demnächst stattfindendes Spiel:*\n$matches"
            else -> ":mega: *Demnächst stattfindende Spiele:*\n$matches"
        }

        notificationChannels.forEach { roomName ->
            Bot.webserviceMessageQueue.add(WebserviceMessage(Bot.knownChannelNamesToIds[roomName], null, message, ":soccer:", username))
        }

        DataImportService().setFixturesToAnnounced(filteredResults)
    }

    private fun sendNotifications(notificationChannels: List<String>, username: String?, liveUpdateResult: List<ImportFixtureResult>) {
        liveUpdateResult.forEach {
            it.newEvents.forEach { event ->
                notificationChannels.forEach { roomName ->
                    Bot.webserviceMessageQueue.add(createMessage(it.fixture, roomName, event, username))
                }
            }

            if (it.stateChange != null) {
                notificationChannels.forEach { roomName ->
                    Bot.webserviceMessageQueue.add(createMessage(it.fixture, roomName, it.stateChange, username))
                }
            }
        }
    }

    fun createMessage(fixture: Fixture, roomName: String, message: String, username: String?): WebserviceMessage {
        val matchTitleShort = MatchTitleService.formatMatchTitleShort(fixture)
        val formattedMessage = ":mega: $matchTitleShort: $message"

        return WebserviceMessage(Bot.knownChannelNamesToIds[roomName], null, formattedMessage, ":soccer:", username)
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
        val nextLiveUpdate = getNextLiveUpdate()

        logger().debug("Next live update would be at {}", nextLiveUpdate)
        logger().debug("Next daily update would be at {}", nextDailyUpdate)

        if (nextLiveUpdate == null || nextDailyUpdate.isBefore(nextLiveUpdate)) {
            scheduleDailyUpdate(getSeconds(nextDailyUpdate))
        }
        else {
            scheduleLiveUpdate(getSeconds(nextLiveUpdate))
        }
    }

    private fun scheduleDailyUpdate(seconds: Long) {
        DataImportService.nextUpdate = LocalDateTime.now().plusSeconds(seconds)
        DataImportService.nextUpdateType = UpdateType.DAILY
        logger().debug("Scheduling next daily update for {} (in {} seconds)", DataImportService.nextUpdate, seconds)
        executorService.schedule( { handleExceptions { runDailyUpdate() } }, seconds, TimeUnit.SECONDS)
    }

    private fun scheduleLiveUpdate(seconds: Long) {
        DataImportService.nextUpdate = LocalDateTime.now().plusSeconds(seconds)
        DataImportService.nextUpdateType = UpdateType.LIVE
        logger().debug("Scheduling next live update for {} (in {} seconds)", DataImportService.nextUpdate, seconds)
        executorService.schedule( { handleExceptions { runLiveUpdate() } }, seconds, TimeUnit.SECONDS)
    }

    private fun handleExceptions(function: () -> Unit) {
        try {
            function.invoke()
        }
        catch (e: Throwable) {
            logger().error("Exception occurred: ", e)
        }
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
