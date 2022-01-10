package at.rueckgr.kotlin.rocketbot.database

enum class FixtureState(val code: String, val period: FixtureStatePeriod, val description: String? = null) {
    TO_BE_DEFINED("TBD", FixtureStatePeriod.FUTURE),
    NOT_STARTED("NS", FixtureStatePeriod.FUTURE),
    FIRST_HALF("1H", FixtureStatePeriod.LIVE, "Anpfiff 1. Halbzeit"),
    HALF_TIME("HT", FixtureStatePeriod.LIVE, "Halbzeit"),
    SECOND_HALF("2H", FixtureStatePeriod.LIVE, "Anpfiff 2. Halbzeit"),
    EXTRA_TIME("EXTRA_TIME", FixtureStatePeriod.LIVE, "Verlängerung"),
    PENALTY("PENALTY", FixtureStatePeriod.LIVE, "Elfmeterschießen"),
    MATCH_FINISHED("FT", FixtureStatePeriod.PAST, "Schlusspfiff nach regulärer Spielzeit"),
    MATCH_FINISHED_AFTER_EXTRATIME("AET", FixtureStatePeriod.PAST, "Schlusspfiff nach Verlängerung"),
    MATCH_FINISHED_AFTER_PENALTY("PEN", FixtureStatePeriod.PAST, "Spielende nach Elfmeterschießen"),
    BREAK_TIME("BT", FixtureStatePeriod.LIVE, "Pause"),
    SUSPENDED("SUSP", FixtureStatePeriod.LIVE),
    INTERRUPTED("INT", FixtureStatePeriod.LIVE),
    POSTPONED("PST", FixtureStatePeriod.FUTURE),
    CANCELLED("CANC", FixtureStatePeriod.PAST),
    ABANDONED("ABD", FixtureStatePeriod.PAST),
    TECHNICAL_LOSS("AWD", FixtureStatePeriod.PAST),
    WALK_OVER("WO", FixtureStatePeriod.PAST),
    LIVE("LIVE", FixtureStatePeriod.LIVE);

    companion object {
        fun getByPeriod(period: FixtureStatePeriod) = FixtureState
            .values()
            .filter { it.period == period }
            .map { it.code }

        fun getByCode(code: String): FixtureState? = values().firstOrNull { it.code == code }
    }
}


enum class FixtureStatePeriod {
    PAST,
    LIVE,
    FUTURE
}
