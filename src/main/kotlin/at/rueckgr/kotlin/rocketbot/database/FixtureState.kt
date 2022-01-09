package at.rueckgr.kotlin.rocketbot.database

enum class FixtureState(val code: String, val period: FixtureStatePeriod) {
    TO_BE_DEFINED("TBD", FixtureStatePeriod.FUTURE),
    NOT_STARTED("NS", FixtureStatePeriod.FUTURE),
    FIRST_HALF("1H", FixtureStatePeriod.LIVE),
    HALF_TIME("HT", FixtureStatePeriod.LIVE),
    SECOND_HALF("2H", FixtureStatePeriod.LIVE),
    EXTRA_TIME("EXTRA_TIME", FixtureStatePeriod.LIVE),
    PENALTY("PENALTY", FixtureStatePeriod.LIVE),
    MATCH_FINISHED("FT", FixtureStatePeriod.PAST),
    MATCH_FINISHED_AFTER_EXTRATIME("AET", FixtureStatePeriod.PAST),
    MATCH_FINISHED_AFTER_PENALTY("PEN", FixtureStatePeriod.PAST),
    BREAK_TIME("BT", FixtureStatePeriod.LIVE),
    SUSPENDED("SUSP", FixtureStatePeriod.LIVE),
    INTERRUPTED("INT", FixtureStatePeriod.LIVE),
    POSTPONED("PST", FixtureStatePeriod.FUTURE),
    CANCELLED("CANC", FixtureStatePeriod.PAST),
    ABANDONED("ABD", FixtureStatePeriod.PAST),
    TECHNICAL_LOSS("AWD", FixtureStatePeriod.PAST),
    WALK_OVER("WO", FixtureStatePeriod.PAST),
    LIVE("LIVE", FixtureStatePeriod.LIVE)
}

enum class FixtureStatePeriod {
    PAST,
    LIVE,
    FUTURE
}
