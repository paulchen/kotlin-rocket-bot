package at.rueckgr.kotlin.rocketbot

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimestampFormatter {
    private val PATTERN = "YYYY-MM-dd HH:mm:ss"

    fun formatTimestamp(timestamp: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern(PATTERN)
        return timestamp.format(formatter)
    }
}
