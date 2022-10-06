package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.RoomMessageHandler
import at.rueckgr.kotlin.rocketbot.util.Logging
import java.util.regex.Pattern

class NicePlugin : AbstractPlugin(), Logging {
    override fun getCommands(): List<String> = emptyList()

    override fun handle(channel: RoomMessageHandler.Channel, user: RoomMessageHandler.User, message: RoomMessageHandler.Message): List<OutgoingMessage> =
        when (containsNiceNumbers(message.message)) {
            true -> listOf(OutgoingMessage("all numbers in that message add up to 69 -- _nice_"))
            false -> emptyList()
        }

    fun containsNiceNumbers(message: String): Boolean {
        val numbers = extractNumbers(message)
        return !numbers.contains(0.0) && numbers.size > 1 && numbers.sum() == 69.0
    }

    fun extractNumbers(message: String): List<Double> {
        val m = Pattern.compile("(^-| -)?\\d+(\\.\\d+)?").matcher(message) //NOSONAR
        val list = ArrayList<Double>()
        while (m.find()) {
            list.add(m.group().trim().toDouble())
        }
        return list
    }

    override fun getHelp(command: String): List<String> = emptyList()

    override fun getProblems(): List<String> = emptyList()
}
