package at.rueckgr.kotlin.rocketbot.reminder

import at.rueckgr.kotlin.rocketbot.util.time.TimeUnit
import java.time.LocalDateTime


fun calculateNextExecution(count: Long, unit: TimeUnit, referenceTime: LocalDateTime) =
    unit.plusFunction.invoke(referenceTime, count)
