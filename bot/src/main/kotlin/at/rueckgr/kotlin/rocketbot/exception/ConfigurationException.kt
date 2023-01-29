package at.rueckgr.kotlin.rocketbot.exception

class ConfigurationException(val exitCode: Int, message: String) : Exception(message) {
}
