package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.soccer.MatchInfoService

class SoccerPlugin : AbstractPlugin() {
    override fun getCommands() = listOf("cl")

    override fun handle(message: String): List<OutgoingMessage> {
        // TODO configurable
        val (pastMatches, liveMatches, futureMatches) = MatchInfoService().getMatchInfo(3)

        if (pastMatches.isEmpty() && liveMatches.isEmpty() && futureMatches.isEmpty()) {
            // TODO emoji, username
            return listOf(OutgoingMessage("Keine Spieldaten vorhanden."))
        }
        val parts = ArrayList<String>(3)
        parts.add(processMatches(pastMatches, "Verganenes Spiel", "Verganene Spiele"))
        parts.add(processMatches(liveMatches, "Laufendes Spiel", "Laufende Spiele"))
        parts.add(processMatches(futureMatches, "Zukünftiges Spiel", "Zukünftige Spiele"))

        val result = parts.filter { it.isNotBlank() }.joinToString("\n\n")

        // TODO emoji, username
        return listOf(OutgoingMessage(result))
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
