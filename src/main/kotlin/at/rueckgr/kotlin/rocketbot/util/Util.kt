package at.rueckgr.kotlin.rocketbot.util

private const val ZWNBSP = "\uFEFF"

fun formatUsername(username: String): String {
    // insert ZWNBSP to avoid highlighting
    return username.substring(0, 1) + ZWNBSP + username.substring(1)
}
