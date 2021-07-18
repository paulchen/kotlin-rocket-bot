package at.rueckgr.kotlin.rocketbot.plugins

abstract class AbstractPlugin {
    abstract fun getCommands(): List<String>

    abstract fun handle(message: String): List<String>

    abstract fun getHelp(command: String): List<String>
}
