package at.rueckgr.kotlin.rocketbot.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

private const val ZWNBSP = '\uFEFF'
private const val ZWJ = '\u200D'

fun formatUsername(username: String): String {
    val offset = findOffsetOfSecondCharacter(username)
    // insert ZWNBSP to avoid highlighting
    return username.substring(0, offset) + ZWNBSP + username.substring(offset)
}

fun findOffsetOfSecondCharacter(username: String): Int {
    for (i in username.indices) {
        if (!username[i].isSurrogate() && username[i] != ZWJ) {
            return i
        }
    }
    return username.length
}

fun toLocalDateTime(zonedDateTime: ZonedDateTime) =
    LocalDateTime.ofInstant(zonedDateTime.toInstant(), ZoneId.systemDefault())
