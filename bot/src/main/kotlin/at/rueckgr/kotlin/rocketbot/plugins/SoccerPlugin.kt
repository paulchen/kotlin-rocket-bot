package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.EventHandler
import at.rueckgr.kotlin.rocketbot.soccer.DataImportService
import at.rueckgr.kotlin.rocketbot.soccer.MatchInfoService
import at.rueckgr.kotlin.rocketbot.soccer.SoccerProblemService
import at.rueckgr.kotlin.rocketbot.soccer.SoccerUpdateService
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import at.rueckgr.kotlin.rocketbot.util.SoccerPluginMode
import at.rueckgr.kotlin.rocketbot.util.time.DateTimeDifferenceCalculator
import at.rueckgr.kotlin.rocketbot.util.time.TimeUnit
import java.time.LocalDateTime
import java.time.ZonedDateTime

class SoccerPlugin : AbstractPlugin() {
    override fun getCommands() = listOf("em", "wm")

    override fun init() {
        SoccerUpdateService.scheduleImmediateDailyUpdate()
    }

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        val messageText = message.message.lowercase().trim()
        val configuration = ConfigurationProvider.getSoccerConfiguration()
        return when (messageText) {
            "!em" -> when (configuration.mode) {
                SoccerPluginMode.EUROPEAN_CHAMPIONSHIP -> createTournamentStatusMessage()
                else -> createTimeMessage(configuration.nextEuropeanChampionship)
            }
            "!wm" -> when (configuration.mode) {
                SoccerPluginMode.WORLD_CUP -> createTournamentStatusMessage()
                else -> createTimeMessage(configuration.nextWorldCup)
            }
            else -> emptyList()
        }
    }

    private fun createTournamentStatusMessage(): List<OutgoingMessage> {
        val configuration = ConfigurationProvider.getSoccerConfiguration()
        val matchesToShow = configuration.matchesToShow ?: 3
        val (pastMatches, liveMatches, futureMatches) = MatchInfoService().getMatchInfo(matchesToShow, configuration.leagueId!!, configuration.season!!)

        if (pastMatches.isEmpty() && liveMatches.isEmpty() && futureMatches.isEmpty()) {
            return listOf(OutgoingMessage("Keine Spieldaten vorhanden.", configuration.emoji, configuration.username))
        }
        val parts = ArrayList<String>(3)
        parts.add(processMatches(pastMatches, "Vergangenes Spiel", "Vergangene Spiele"))
        parts.add(processMatches(liveMatches, "Laufendes Spiel", "Laufende Spiele"))
        parts.add(processMatches(futureMatches, "Zukünftiges Spiel", "Zukünftige Spiele"))

        val result = parts.filter { it.isNotBlank() }.joinToString("\n\n")

        return listOf(OutgoingMessage(result, configuration.emoji, configuration.username))
    }

    private fun createTimeMessage(date: LocalDateTime?): List<OutgoingMessage> {
        if (date == null) {
            return emptyList()
        }
        val configuration = ConfigurationProvider.getSoccerConfiguration()
        return listOf(OutgoingMessage(DateTimeDifferenceCalculator().formatTimeDifference(LocalDateTime.now(), date, listOf(TimeUnit.WEEK)),
            configuration.emoji, configuration.username))
    }

    private fun processMatches(matches: List<String>, singular: String, plural: String): String {
        val processedMatches = matches.joinToString("\n") { "- $it" }
        return when (matches.size) {
            0 -> ""
            1 -> "*$singular: *\n$processedMatches"
            else -> "*$plural: *\n$processedMatches"
        }
    }

    override fun getHelp(command: String): List<String> {
        val mode = ConfigurationProvider.getSoccerConfiguration().mode
        return when (command) {
            "em" -> when (mode) {
                SoccerPluginMode.EUROPEAN_CHAMPIONSHIP -> listOf("`!em` provides some information about past, current, and future matches within the current UEFA European Championship")
                else -> listOf("`!em` tells the time period until the beginning of the next UEFA European Championship")
            }
            "wm" -> when (mode) {
                SoccerPluginMode.WORLD_CUP -> listOf("`!wm` provides some information about past, current, and future matches within the current FIFA World Cup")
                else -> listOf("`!wm` tells the time period until the beginning of the next FIFA World Cup")
            }
            else -> emptyList()
        }
    }

    override fun getProblems(): List<String> {
        val problems = mutableListOf<String>()
        checkLastUpdate()?.let { problems.add(it) }
        checkLastUpdateFailed()?.let { problems.add(it) }
        checkNextUpdate()?.let { problems.add(it) }

        problems.addAll(SoccerProblemService.problems.values)

        return problems
    }

    private fun checkLastUpdate(): String? {
        val lastUpdate = DataImportService.lastUpdate
        return if (ConfigurationProvider.getSoccerConfiguration().mode == SoccerPluginMode.DORMANT) {
            null
        }
        else if (lastUpdate == null) {
            "Soccer data has never been updated"
        }
        else if (lastUpdate.isBefore(ZonedDateTime.now().minusDays(1))) {
            "Last soccer data update is more than one day ago"
        }
        else {
            null
        }
    }

    private fun checkLastUpdateFailed(): String? =
        when (DataImportService.lastUpdateFailed) {
            true -> "Last update of soccer data failed"
            false -> null
        }

    private fun checkNextUpdate(): String? {
        val nextUpdate = DataImportService.nextUpdate
        return if (nextUpdate == null) {
            "No update of soccer data scheduled"
        }
        else if (nextUpdate.isBefore(ZonedDateTime.now().minusMinutes(5))) {
            "Date of next soccer data update is in the past"
        }
        else {
            null
        }
    }

    override fun getAdditionalStatus(): Map<String, String> {
        val liveFixtures = DataImportService().getLiveFixtures()
        val liveMatches = when (liveFixtures.size) {
            0 -> "0"
            else -> {
                val matches = liveFixtures.joinToString(", ") { "${it.id}: ${it.teamHome}-${it.teamAway}" }
                "${liveFixtures.size} ($matches)"
            }
        }

        return mapOf(
            "liveMatches" to liveMatches,
            "lastUpdate" to (DataImportService.lastUpdate?.toString() ?: "unknown"),
            "nextUpdate" to (DataImportService.nextUpdate?.toString() ?: "unknown"),
            "nextUpdateType" to DataImportService.nextUpdateType.toString()
        )
    }
}
