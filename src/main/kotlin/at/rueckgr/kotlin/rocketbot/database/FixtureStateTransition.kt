package at.rueckgr.kotlin.rocketbot.database

enum class FixtureStateTransition(val oldState: FixtureState?, val newState: FixtureState?, val description: String? = null, val appendScore: Boolean = false) {
    POSTPONED(null, FixtureState.POSTPONED, "Spiel verschoben"),
    CANCELLED(null, FixtureState.CANCELLED, "Spiel abgebrochen"),

    SUSP_INT(FixtureState.SUSPENDED, FixtureState.INTERRUPTED),
    INT_SUSP(FixtureState.INTERRUPTED, FixtureState.SUSPENDED),

    INT_START(null, FixtureState.INTERRUPTED, "Spiel unterbrochen"),
    SUSP_START(null, FixtureState.SUSPENDED, "Spiel unterbrochen"),
    INT_END(FixtureState.INTERRUPTED, null, "Spiel fortgesetzt", true),
    SUSP_END(FixtureState.SUSPENDED, null, "Spiel fortgesetzt", true),

    FIRST_HALF(null, FixtureState.FIRST_HALF, "Anpfiff 1. Halbzeit"),
    HALF_TIME(null, FixtureState.HALF_TIME, "Halbzeit", true),
    SECOND_HALF(null, FixtureState.SECOND_HALF, "Anpfiff 2. Halbzeit", true),
    EXTRA_TIME(null, FixtureState.EXTRA_TIME, "Verlängerung", true),
    PENALTY(null, FixtureState.PENALTY, "Elfmeterschießen", true),
    MATCH_FINISHED(null, FixtureState.MATCH_FINISHED, "Schlusspfiff nach regulärer Spielzeit", true),
    MATCH_FINISHED_AFTER_EXTRATIME(null, FixtureState.MATCH_FINISHED_AFTER_EXTRATIME, "Schlusspfiff nach Verlängerung", true),
    MATCH_FINISHED_AFTER_PENALTY(null, FixtureState.MATCH_FINISHED_AFTER_PENALTY, "Spielende nach Elfmeterschießen", true),
    BREAK_TIME(null, FixtureState.BREAK_TIME, "Pause"),
}
