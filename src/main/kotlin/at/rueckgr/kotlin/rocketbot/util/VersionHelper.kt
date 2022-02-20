package at.rueckgr.kotlin.rocketbot.util

import java.io.InputStream
import java.util.*

object VersionHelper {
    fun getVersion(): VersionInfo {
        return when (val resource = VersionHelper::class.java.getResourceAsStream("/git-revision")) {
            null -> VersionInfo("unknown", "unknown")
            else -> readVersionInfo(resource)
        }
    }

    private fun readVersionInfo(stream: InputStream): VersionInfo {
        val properties = Properties()
        properties.load(stream)

        return VersionInfo(properties.getProperty("revision"), properties.getProperty("commitMessage"))
    }
}
