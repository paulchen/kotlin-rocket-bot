package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.Bot
import at.rueckgr.kotlin.rocketbot.WebserviceMessage
import at.rueckgr.kotlin.rocketbot.database.Fixture
import at.rueckgr.kotlin.rocketbot.database.FixtureState
import at.rueckgr.kotlin.rocketbot.database.Fixtures
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.Db
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger
import org.ktorm.dsl.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max

class SoccerUpdateService : Logging {
    private val executorService = Executors.newScheduledThreadPool(1)

    fun runDailyUpdate() {
        val updateResult = try {
            DataImportService().runDailyUpdate()
        }
        catch (e: Throwable) {
            logger().error(e)
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
        val soccerConfiguration = ConfigurationProvider.instance.getSoccerConfiguration()
        val username = soccerConfiguration.username
        val notificationChannels = soccerConfiguration.notificationChannels

        val liveUpdateResult = try {
            DataImportService().runLiveUpdate()
        }
        catch (e: Throwable) {
            logger().error(e)
            emptyList()
        }

        if (liveUpdateResult.isEmpty()) {
            scheduleLiveOrDailyUpdate()
        }
        else {
            scheduleQuickLiveUpdate()
        }

        if(notificationChannels?.isEmpty() != false) {
            return
        }

        liveUpdateResult.forEach {
            if (it.stateChange != null) {
                notificationChannels.forEach { roomName ->
                    Bot.webserviceMessageQueue.add(createMessage(it.fixture, roomName, it.stateChange, username))
                }
            }

            it.newEvents.forEach { event ->
                notificationChannels.forEach { roomName ->
                    Bot.webserviceMessageQueue.add(createMessage(it.fixture, roomName, event, username))
                }
            }
        }
    }

    private fun createMessage(fixture: Fixture, roomName: String, message: String, username: String?): WebserviceMessage {
        val teamHome = TeamMapper.instance.mapTeamName(fixture.teamHome)
        val teamAway = TeamMapper.instance.mapTeamName(fixture.teamAway)

        val formattedMessage = "*$teamHome\u00a0-\u00a0$teamAway*: $message"

        return WebserviceMessage(null, roomName, formattedMessage, ":soccer:", username)
    }

    private fun hasLiveFixtures(updateResult: List<ImportFixtureResult>): Boolean {
        val inOneHour = LocalDateTime.now().plusHours(1)
        val oneHourAgo = LocalDateTime.now().minusHours(1)

        return updateResult
            .map { it.fixture }
            .any {
                (FixtureState.getByCode(it.status) == FixtureState.LIVE)
                    || (it.date.isAfter(oneHourAgo) && it.date.isBefore(inOneHour))
                    || (it.endDate != null && it.endDate!!.isAfter(oneHourAgo))
            }
    }

    private fun scheduleQuickLiveUpdate() = executorService.schedule( { runLiveUpdate() }, 30, TimeUnit.SECONDS)

    private fun scheduleLiveOrDailyUpdate() {
        val nextDailyUpdate = getNextDailyUpdate()
        val nextLiveUpdate = getNextLiveUpdate()

        if (nextLiveUpdate == null || nextDailyUpdate.isBefore(nextLiveUpdate)) {
            executorService.schedule( { runDailyUpdate() }, getSeconds(nextDailyUpdate), TimeUnit.SECONDS)
        }
        else {
            executorService.schedule( { runLiveUpdate() }, getSeconds(nextLiveUpdate), TimeUnit.SECONDS)
        }
    }

    private fun getNextDailyUpdate(): LocalDateTime {
        val today4am = LocalDateTime.now().withHour(4).withMinute(0)
        return if(LocalDateTime.now().isBefore(today4am)) {
            today4am
        }
        else {
            today4am.plusDays(1)
        }
    }

    private fun getNextLiveUpdate(): LocalDateTime? = Db().connection
            .from(Fixtures)
            .select(Fixtures.date)
            .where { Fixtures.date greater LocalDateTime.now() }
            .orderBy(Fixtures.date.asc())
            .limit(1)
            .map { it[Fixtures.date] }
            .firstOrNull()
            ?.minusHours(1)

    private fun getSeconds(time: LocalDateTime) = max(30, ChronoUnit.SECONDS.between(LocalDateTime.now(), time))
}
