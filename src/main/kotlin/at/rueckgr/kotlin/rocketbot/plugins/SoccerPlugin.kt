package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.soccer.MatchInfoService
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider

class SoccerPlugin : AbstractPlugin() {
    override fun getCommands() = listOf("cl")

    override fun handle(message: String): List<OutgoingMessage> {
        val configuration = ConfigurationProvider.instance.getSoccerConfiguration()
        val matchesToShow = configuration.matchesToShow ?: 3
        val (pastMatches, liveMatches, futureMatches) = MatchInfoService().getMatchInfo(matchesToShow)

        if (pastMatches.isEmpty() && liveMatches.isEmpty() && futureMatches.isEmpty()) {
            return listOf(OutgoingMessage("Keine Spieldaten vorhanden.", ":emoji:", configuration.username))
        }
        val parts = ArrayList<String>(3)
        parts.add(processMatches(pastMatches, "Verganenes Spiel", "Verganene Spiele"))
        parts.add(processMatches(liveMatches, "Laufendes Spiel", "Laufende Spiele"))
        parts.add(processMatches(futureMatches, "Zukünftiges Spiel", "Zukünftige Spiele"))

        val result = parts.filter { it.isNotBlank() }.joinToString("\n\n")

        return listOf(OutgoingMessage(result, ":emoji:", configuration.username))
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
}
