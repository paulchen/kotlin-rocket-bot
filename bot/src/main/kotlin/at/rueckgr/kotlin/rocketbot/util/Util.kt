package at.rueckgr.kotlin.rocketbot.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

private const val ZWNBSP = '\uFEFF'
private const val ZWJ = '\u200D'
private const val REGIONAL_INDICATORS_BLOCK = '\uD83C'
private const val REGIONAL_INDICATORS_START = '\uDDE6'
private const val REGIONAL_INDICATORS_END = '\uDDFF'

fun formatUsername(username: String): String {
    val offset = findOffsetOfSecondCharacter(username)
    // insert ZWNBSP to avoid highlighting
    return username.substring(0, offset) + ZWNBSP + username.substring(offset)
}

fun findOffsetOfSecondCharacter(username: String): Int {
    var regionalIndicatorsBlock = false
    for (i in username.indices) {
        if (!username[i].isSurrogate() && username[i] != ZWJ && !regionalIndicatorsBlock) {
            return i
        }

        if (i % 2 == 1) {
            val char1 = username[i-1]
            val char2 = username[i]
            if (char1 == REGIONAL_INDICATORS_BLOCK && char2 >= REGIONAL_INDICATORS_START && char2 <= REGIONAL_INDICATORS_END) {
                regionalIndicatorsBlock = !regionalIndicatorsBlock
            }
            else if (regionalIndicatorsBlock) {
                regionalIndicatorsBlock = false
            }
        }
    }
    return username.length
}

fun toLocalDateTime(zonedDateTime: ZonedDateTime) =
    LocalDateTime.ofInstant(zonedDateTime.toInstant(), ZoneId.systemDefault())
