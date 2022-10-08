package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.database.FixtureState
import at.rueckgr.kotlin.rocketbot.database.FixtureStatePeriod
import at.rueckgr.kotlin.rocketbot.database.Fixtures
import at.rueckgr.kotlin.rocketbot.util.Db
import org.ktorm.database.Database
import org.ktorm.dsl.*

class MatchInfoService {
    fun getMatchInfo(matchesCount: Int, leagueId: Long, season: Int): MatchInfo {
        val connection = Db().connection

        return MatchInfo(
            getMatches(connection, FixtureStatePeriod.PAST, matchesCount, leagueId, season),
            getMatches(connection, FixtureStatePeriod.LIVE, matchesCount, leagueId, season),
            getMatches(connection, FixtureStatePeriod.FUTURE, matchesCount, leagueId, season)
        )
    }

    private fun getMatches(connection: Database, period: FixtureStatePeriod, matchesCount: Int, leagueId: Long, season: Int): List<String> {
        val list = connection
            .from(Fixtures)
            .joinReferencesAndSelect()
            .whereWithConditions {
                it += Fixtures.status inList FixtureState.getByPeriod(period)
                it += Fixtures.leagueId eq leagueId
                it += Fixtures.season eq season
            }
            .orderBy(when (period) {
                FixtureStatePeriod.PAST -> Fixtures.date.desc()
                else -> Fixtures.date.asc()
            })
            .limit(0, when (period) {
                FixtureStatePeriod.LIVE -> Int.MAX_VALUE
                else -> matchesCount
            })
            .map { row -> Fixtures.createEntity(row) }
            .toList()

        return if (period == FixtureStatePeriod.PAST) {
            list.reversed()
        }
        else {
            list
        }.map { match -> MatchTitleService.formatMatchTitle(match) }
    }
}

data class MatchInfo(
    val pastMatches: List<String>,
    val liveMatches: List<String>,
    val futureMatches: List<String>
)
