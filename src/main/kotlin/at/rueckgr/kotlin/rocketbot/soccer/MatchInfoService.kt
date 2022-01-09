package at.rueckgr.kotlin.rocketbot.soccer

import at.rueckgr.kotlin.rocketbot.database.FixtureState
import at.rueckgr.kotlin.rocketbot.database.FixtureStatePeriod
import at.rueckgr.kotlin.rocketbot.database.Fixtures
import at.rueckgr.kotlin.rocketbot.util.Db
import org.ktorm.database.Database
import org.ktorm.dsl.*

class MatchInfoService {
    fun getMatchInfo(matchesCount: Int): MatchInfo {
        val connection = Db().connection

        return MatchInfo(
            getMatches(connection, FixtureStatePeriod.PAST, matchesCount),
            getMatches(connection, FixtureStatePeriod.LIVE, matchesCount),
            getMatches(connection, FixtureStatePeriod.FUTURE, matchesCount)
        )
    }

    private fun getMatches(connection: Database, period: FixtureStatePeriod, matchesCount: Int): List<String> {
        val matchTitleService = MatchTitleService()
        val list = connection
            .from(Fixtures)
            .joinReferencesAndSelect()
            .where { Fixtures.status inList getStates(period) }
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
        }.map { match -> matchTitleService.formatMatchTitle(match) }
    }

    private fun getStates(period: FixtureStatePeriod): List<String> {
        return FixtureState
            .values()
            .filter { it.period == period }
            .map { it.code }
    }
}

data class MatchInfo(
    val pastMatches: List<String>,
    val liveMatches: List<String>,
    val futureMatches: List<String>
)
