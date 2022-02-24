package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.database.Fixture
import at.rueckgr.kotlin.rocketbot.database.FixtureState
import at.rueckgr.kotlin.rocketbot.database.FixtureStatePeriod
import at.rueckgr.kotlin.rocketbot.database.Venue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MatchTitleService {
    private val zwnbsp = "\ufeff"

    fun formatMatchTitle(fixture: Fixture): String {
        val time = formatTime(fixture.date)
        val venue = formatVenue(fixture.venue!!)
        val score = formatMatchScore(fixture)

        val teamHome = TeamMapper.mapTeamName(fixture.teamHome)
        val teamAway = TeamMapper.mapTeamName(fixture.teamAway)

        val fixtureState = FixtureState.getByCode(fixture.status)
        val state = when (fixtureState?.period) {
            FixtureStatePeriod.LIVE -> "${fixtureState.description}, "
            else -> ""
        }
        val elapsed = if (fixture.elapsed != null && fixtureState?.period == FixtureStatePeriod.LIVE) {
            "${fixture.elapsed}. Spielminute, "
        }
        else {
            ""
        }

        return if (score == null) {
            "$time: *$teamHome\u00a0-\u00a0$teamAway* ($venue)"
        }
        else {
            "$time: *$teamHome\u00a0-\u00a0$teamAway* ($venue): $state$elapsed$score"

        }
    }

    private fun formatTime(date: LocalDateTime): String {
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
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

    fun formatMatchScore(fixture: Fixture): String? {
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
            "$htHome$zwnbsp:$zwnbsp$htAway"
        }
        else if (etHome == null && ftHome == 0 && ftAway == 0) {
            "0$zwnbsp:${zwnbsp}0" + formatPenalty(pHome, pAway)
        }
        else if (etHome == null) {
            "$ftHome$zwnbsp:$zwnbsp$ftAway ($htHome$zwnbsp:$zwnbsp$htAway)" + formatPenalty(pHome, pAway)
        }
        else if (etHome == 0 && etAway == 0) {
            "0$zwnbsp:${zwnbsp}0 n.V. (0$zwnbsp:${zwnbsp}0)" + formatPenalty(pHome, pAway)
        }
        else if (etHome > 0 && ftHome == 0 && ftAway == 0) {
            "$etHome$zwnbsp:$zwnbsp$etAway n.V. (0$zwnbsp:${zwnbsp}0)" + formatPenalty(pHome, pAway)
        }
        else if (etHome > 0) {
            "$etHome$zwnbsp:$zwnbsp$etAway n.V. ($ftHome$zwnbsp:$zwnbsp$ftAway, $htHome$zwnbsp:$zwnbsp$htAway)" + formatPenalty(pHome, pAway)
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
            ", $pHome$zwnbsp:$zwnbsp$pAway i.E."
        }
    }
}
