package at.rueckgr.kotlin.rocketbot.reminder

import at.rueckgr.kotlin.rocketbot.util.time.TimeUnit
import java.time.LocalDateTime


fun calculateNextExecution(count: Long, unit: TimeUnit, referenceTime: LocalDateTime): LocalDateTime {
    var nextExecution = referenceTime
    // avoid multiple reminders in case multiple executins were missed,
    // but keep the interval between the executions
    while (nextExecution.isBefore(LocalDateTime.now())) {
        nextExecution = unit.plusFunction.invoke(nextExecution, count)
    }
    return nextExecution
}

