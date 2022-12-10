package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.database.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MatchTitleService {
    private val zwnbsp = "\ufeff"

    fun formatMatchTitleShort(fixture: Fixture): String {
        val teamHome = formatTeamWithFlag(fixture.teamHome)
        val teamAway = formatTeamWithFlag(fixture.teamAway)

        return "$teamHome\u00a0-\u00a0$teamAway"
    }

    fun formatTeamWithFlag(teamName: String): String {
        val (team, flag) = TeamMapper.mapTeamName(teamName)
        return when (flag) {
            "" -> "*$team*"
            else -> "$flag\u00a0*$team*"
        }
    }

    fun formatMatchTitle(fixture: Fixture): String {
        val time = formatTime(fixture.date)
        val venue = formatVenue(fixture.venue!!)
        val score = formatMatchScore(fixture)
        val matchTitleShort = formatMatchTitleShort(fixture)

        val fixtureState = FixtureState.getByCode(fixture.status)
        val state = when (fixtureState?.period) {
            FixtureStatePeriod.LIVE -> getDescription(fixtureState) + ", "
            else -> ""
        }
        val elapsed = if (fixture.elapsed != null && fixtureState?.period == FixtureStatePeriod.LIVE) {
            "${fixture.elapsed}. Spielminute, "
        }
        else {
            ""
        }

        return if (score == null) {
            "$time: $matchTitleShort ($venue)"
        }
        else {
            "$time: $matchTitleShort ($venue): $state$elapsed$score"
        }
    }

    private fun getDescription(state: FixtureState) = FixtureStateTransition
            .values()
            .first { it.oldState == null && it.newState == state }
            .description ?: ""

    private fun formatTime(date: LocalDateTime): String {
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH$zwnbsp:${zwnbsp}mm"))
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
        val ftHome = fixture.goalsFulltimeHome
        val ftAway = fixture.goalsFulltimeAway
        val etHome = fixture.goalsExtratimeHome
        val etAway = fixture.goalsExtratimeAway
        val pHome = fixture.goalsPenaltyHome
        val pAway = fixture.goalsPenaltyAway

        val status = FixtureState.getByCode(fixture.status)
        val extratime = if (isLiveMatch(fixture) && status != FixtureState.BREAK_TIME && status != FixtureState.PENALTY) {
            "i.V."
        }
        else {
            "n.V."
        }

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
            "0$zwnbsp:${zwnbsp}0 $extratime (0$zwnbsp:${zwnbsp}0)" + formatPenalty(pHome, pAway)
        }
        else if ((etHome > 0 || etAway!! > 0) && ftHome == 0 && ftAway == 0) {
            "$etHome$zwnbsp:$zwnbsp$etAway $extratime (0$zwnbsp:${zwnbsp}0)" + formatPenalty(pHome, pAway)
        }
        else if (etHome > 0) {
            "$etHome$zwnbsp:$zwnbsp$etAway $extratime ($ftHome$zwnbsp:$zwnbsp$ftAway, $htHome$zwnbsp:$zwnbsp$htAway)" + formatPenalty(pHome, pAway)
        }
        else {
            null
        }
    }

    private fun isLiveMatch(fixture: Fixture): Boolean =
        FixtureState.getByCode(fixture.status)?.period == FixtureStatePeriod.LIVE

    private fun formatPenalty(pHome: Int?, pAway: Int?): String {
        return if (pHome == null) {
            ""
        }
        else {
            ", $pHome$zwnbsp:$zwnbsp$pAway i.E."
        }
    }
}
