package at.rueckgr.kotlin.rocketbot.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

private const val ZWNBSP = "\uFEFF"

fun formatUsername(username: String): String {
    // insert ZWNBSP to avoid highlighting
    return username.substring(0, 1) + ZWNBSP + username.substring(1)
}

fun toLocalDateTime(zonedDateTime: ZonedDateTime) =
    LocalDateTime.ofInstant(zonedDateTime.toInstant(), ZoneId.systemDefault())
