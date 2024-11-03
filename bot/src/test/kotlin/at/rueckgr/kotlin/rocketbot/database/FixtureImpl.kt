package at.rueckgr.kotlin.rocketbot.database

import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder
import java.time.LocalDateTime
import kotlin.reflect.KClass

class FixtureImpl(
    override var id: Long,
    override var leagueId: Long,
    override var season: Int,
    override var date: LocalDateTime,
    override var round: String,
    override var teamHome: String,
    override var teamAway: String,
    override var status: String,
    override var elapsed: Int?,
    override var goalsHalftimeHome: Int?,
    override var goalsHalftimeAway: Int?,
    override var goalsFulltimeHome: Int?,
    override var goalsFulltimeAway: Int?,
    override var goalsExtratimeHome: Int?,
    override var goalsExtratimeAway: Int?,
    override var goalsPenaltyHome: Int?,
    override var goalsPenaltyAway: Int?,
    override var eventsProcessed: Int,
    override var venue: Venue?,
    override var endDate: LocalDateTime?,
    override var announced: Boolean,
    override var pendingScoreChange: Boolean,
    override var firstPenaltyTeam: Long?
) : Fixture {
    override val changedProperties: Map<String, Any?>
        get() = TODO("Not yet implemented")
    override val entityClass: KClass<Fixture>
        get() = TODO("Not yet implemented")
    override val properties: Map<String, Any?>
        get() = TODO("Not yet implemented")

    override fun copy(): Fixture {
        TODO("Not yet implemented")
    }

    override fun delete(): Int {
        TODO("Not yet implemented")
    }

    override fun discardChanges() {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun flushChanges(): Int {
        TODO("Not yet implemented")
    }

    override fun get(name: String): Any? {
        TODO("Not yet implemented")
    }

    override fun hashCode() = HashCodeBuilder.reflectionHashCode(this)

    override fun set(name: String, value: Any?) {
        TODO("Not yet implemented")
    }

    override fun toString(): String = ToStringBuilder.reflectionToString(this)
}
