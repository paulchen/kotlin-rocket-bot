package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.util.Logging
import java.util.regex.Pattern

class NicePlugin : AbstractPlugin(), Logging {
    override fun getCommands(): List<String> = emptyList()

    override fun handle(message: String) = handle(message, false)

    override fun handle(message: String, botMessage: Boolean): List<OutgoingMessage> = when (containsNiceNumbers(message)) {
        true -> listOf(OutgoingMessage("all numbers in that message add up to 69 -- _nice_"))
        false -> emptyList()
    }

    fun containsNiceNumbers(message: String): Boolean = extractNumbers(message).sum() == 69

    fun extractNumbers(message: String): List<Int> {
        val m = Pattern.compile("\\d+").matcher(message)
        val list = ArrayList<Int>()
        while (m.find()) {
            list.add(m.group().toInt())
        }
        return list
    }

    override fun getHelp(command: String): List<String> = emptyList()

    override fun getProblems(): List<String> = emptyList()
}
