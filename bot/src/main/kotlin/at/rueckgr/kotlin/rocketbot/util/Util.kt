package at.rueckgr.kotlin.rocketbot.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

private const val ZWNBSP = '\uFEFF'

fun formatUsername(username: String): String {
    val offset = UnicodeHelper().findOffsetOfSecondCharacter(username)
    // insert ZWNBSP to avoid highlighting
    return username.substring(0, offset) + ZWNBSP + username.substring(offset)
}

fun toLocalDateTime(zonedDateTime: ZonedDateTime) =
    LocalDateTime.ofInstant(zonedDateTime.toInstant(), ZoneId.systemDefault())

