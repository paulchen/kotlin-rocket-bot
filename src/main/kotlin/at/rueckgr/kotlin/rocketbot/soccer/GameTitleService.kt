package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.database.Fixture
import at.rueckgr.kotlin.rocketbot.database.Venue

class GameTitleService {
    fun formatGameTitle(fixture: Fixture): String {
        val venue = formatVenue(fixture.venue!!)
        val score = formatGameScore(fixture)

        return if (score == null) {
            "${fixture.teamHome}\u00a0-\u00a0${fixture.teamAway} ($venue)"
        }
        else {
            "${fixture.teamHome}\u00a0-\u00a0${fixture.teamAway} ($venue): $score"

        }
    }

    private fun formatVenue(venue: Venue): String {
        val builder = StringBuilder("${venue.name}, ${venue.city}")
        if (venue.country != null) {
            builder.append(", ${venue.country}")
        }
        if (venue.capacity != null) {
            builder.append(", max. ${venue.capacity} Zuschauer:innen")
        }
        return builder.toString()
    }

    fun formatGameScore(fixture: Fixture): String? {
        val htHome = fixture.goalsHalftimeHome
        val htAway = fixture.goalsHalftimeAway
        val ftHome = fixture.goalsFullftimeHome
        val ftAway = fixture.goalsFulltimeAway
        val etHome = fixture.goalsExtratimeHome
        val etAway = fixture.goalsExtratimeAway
        val pHome = fixture.goalsPenaltyHome
        val pAway = fixture.goalsPenaltyAway

        return if (htHome == null) {
            null
        }
        else if(ftHome == null) {
            "$htHome:$htAway"
        }
        else if (etHome == null && ftHome == 0 && ftAway == 0) {
            "0:0" + formatPenalty(pHome, pAway)
        }
        else if (etHome == null) {
            "$ftHome:$ftAway ($htHome:$htAway)" + formatPenalty(pHome, pAway)
        }
        else if (etHome == 0 && etAway == 0) {
            "0:0 n.V. (0:0)" + formatPenalty(pHome, pAway)
        }
        else if (etHome > 0 && ftHome == 0 && ftAway == 0) {
            "$etHome:$etAway n.V. (0:0)" + formatPenalty(pHome, pAway)
        }
        else if (etHome > 0) {
            "$etHome:$etAway n.V. ($ftHome:$ftAway, $htHome:$htAway)" + formatPenalty(pHome, pAway)
        }
        else {
            null
        }
    }

    private fun formatPenalty(pHome: Int?, pAway: Int?): String {
        return if (pHome == null) {
            ""
        }
        else {
            ", $pHome:$pAway i.E."
        }
    }
}
