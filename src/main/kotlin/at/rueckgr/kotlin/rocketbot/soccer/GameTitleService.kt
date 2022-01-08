package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.database.Fixture

class GameTitleService {
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
