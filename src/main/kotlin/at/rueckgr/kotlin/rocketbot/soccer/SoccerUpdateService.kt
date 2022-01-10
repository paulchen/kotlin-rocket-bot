package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.Bot
import at.rueckgr.kotlin.rocketbot.WebserviceMessage
import at.rueckgr.kotlin.rocketbot.database.Fixture
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider

class SoccerUpdateService {
    fun runDailyUpdate() {
        DataImportService().runDailyUpdate()
    }

    fun runLiveUpdate() {
        val soccerConfiguration = ConfigurationProvider.instance.getSoccerConfiguration()
        val username = soccerConfiguration.username
        val notificationChannels = soccerConfiguration.notificationChannels

        val liveUpdateResult = DataImportService().runLiveUpdate()
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
}
