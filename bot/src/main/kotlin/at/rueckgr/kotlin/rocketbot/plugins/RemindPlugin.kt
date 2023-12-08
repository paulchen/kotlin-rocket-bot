package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.ArchiveService
import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.EventHandler
import at.rueckgr.kotlin.rocketbot.UserDetails
import at.rueckgr.kotlin.rocketbot.util.Logging

data class MessageParts(val targetUsername: String, val subject: String, val timespec: String)

class RemindException(message: String): Exception(message)

class RemindPlugin : AbstractPlugin(), Logging {
    override fun getCommands(): List<String> = listOf("remind", "unremind")

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message) =
        try {
            if (message.message.startsWith("!remind")) {
                addReminder(channel, user, message)
            }
            else if (message.message.startsWith("!unremind")) {
                removeReminder(channel, user, message)
            }
            else {
                emptyList()
            }
        }
        catch (e: RemindException) {
            listOf(OutgoingMessage(e.message ?: "Unknown problem occurred"))
        }

    private fun addReminder(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        val (targetUsername, subject, timespecString) = splitRemindMessage(message)
        // TODO handle "me"
        val remindee: UserDetails = ArchiveService().getUserDetails(targetUsername)
            ?: throw RemindException("Unknown user {$targetUsername}")
        val timespec = parseTimespec(timespecString)
            ?: throw RemindException("Invalid time/interval specification")

        val id = createReminder(remindee, subject, timespec)
        return listOf(OutgoingMessage("Will do! _(id: $id)"))
    }

    private fun createReminder(remindee: UserDetails, subject: String, timespec: String): Int {
        // TODO
        return 0
    }

    fun splitRemindMessage(message: EventHandler.Message): MessageParts {
        // !remind <user> about <subject> (at|in|every)
        val aboutPos = message.message.indexOf(" about ", 8)
        val timespecPos = listOf(" at ", " in ", " every ")
            .map { message.message.lastIndexOf(it) }
            .max()
        if (aboutPos < 9 || timespecPos == -1) {
            throw RemindException("Invalid format of input")
        }

        val username = message.message.substring(8, aboutPos).trim()
        val subject = message.message.substring(aboutPos + 7, timespecPos).trim()
        val timespec = message.message.substring(timespecPos + 1).trim()
        if (username.isBlank() || subject.isBlank() || timespec.isBlank()) {
            throw RemindException("Invalid format of input")
        }

        return MessageParts(username, subject, timespec)
    }

    private fun parseTimespec(timespecString: String): String? =
        if (timespecString.startsWith("at")) {
            parseAtTimespec(timespecString.substring(3))
        }
        else if (timespecString.startsWith("in")) {
            parseInTimespec(timespecString.substring(3))
        }
        else if (timespecString.startsWith("every")) {
            parseEveryTimespec(timespecString.substring(6))
        }
        else {
            null
        }

    private fun parseAtTimespec(timespecString: String): String? = null

    private fun parseInTimespec(timespecString: String): String? = null

    private fun parseEveryTimespec(timespecString: String): String? = null


    private fun removeReminder(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> =
        emptyList()

    override fun getHelp(command: String) = when(command) {
        "remind" -> listOf("TODO")
        "unremind" -> listOf("TODO")
        else -> emptyList()
    }

    override fun getProblems(): List<String> = emptyList() // TODO
}
