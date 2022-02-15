package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.soccer.DataImportService
import at.rueckgr.kotlin.rocketbot.soccer.MatchInfoService
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import java.time.LocalDateTime

class SoccerPlugin : AbstractPlugin() {
    override fun getCommands() = listOf("cl")

    override fun handle(message: String): List<OutgoingMessage> {
        val configuration = ConfigurationProvider.instance.getSoccerConfiguration()
        val matchesToShow = configuration.matchesToShow ?: 3
        val (pastMatches, liveMatches, futureMatches) = MatchInfoService().getMatchInfo(matchesToShow)

        if (pastMatches.isEmpty() && liveMatches.isEmpty() && futureMatches.isEmpty()) {
            return listOf(OutgoingMessage("Keine Spieldaten vorhanden.", ":soccer:", configuration.username))
        }
        val parts = ArrayList<String>(3)
        parts.add(processMatches(pastMatches, "Vergangenes Spiel", "Vergangene Spiele"))
        parts.add(processMatches(liveMatches, "Laufendes Spiel", "Laufende Spiele"))
        parts.add(processMatches(futureMatches, "Zukünftiges Spiel", "Zukünftige Spiele"))

        val result = parts.filter { it.isNotBlank() }.joinToString("\n\n")

        return listOf(OutgoingMessage(result, ":soccer:", configuration.username))
    }

    private fun processMatches(matches: List<String>, singular: String, plural: String): String {
        val processedMatches = matches.joinToString("\n") { " - $it" }
        return when (matches.size) {
            0 -> ""
            1 -> "*$singular:*\n\n$processedMatches"
            else -> "*$plural:*\n\n$processedMatches"
        }
    }

    override fun getHelp(command: String) = listOf(
        "`!cl` provides some information about past, current, and future matches within the UEFA Champions League"
    )

    override fun getProblems(): List<String> {
        val lastUpdate = DataImportService.lastUpdate
        return if (lastUpdate == null) {
            listOf("Soccer data has never been updated")
        }
        else if (lastUpdate.isBefore(LocalDateTime.now().minusDays(1))) {
            listOf("Last soccer data update is more than one day ago")
        }
        else {
            emptyList()
        }
    }
}
