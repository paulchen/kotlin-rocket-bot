package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.EventHandler
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.formatUsername
import at.rueckgr.kotlin.rocketbot.util.logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.apache.commons.text.StringEscapeUtils


class SloganPlugin : AbstractPlugin(), Logging {
    private var failedRequests = 0

    override fun getCommands() = listOf("slogan")

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> {
        val name = stripCommand(message.message) ?: return emptyList()
        val formattedName = formatUsername(name)

        val (status, body) = runBlocking {
            val response = HttpClient(CIO).get {
                url("http://www.sloganizer.net/outbound.php")
            }
            Pair(response.status, response.body<String>())
        }

        if (status.value >= 400) {
            failedRequests++
            logger().info("Request to sloganizer failed, status code {}; {} failed requests in total", status, failedRequests)
            return emptyList()
        }

        val bodyWithoutHtml = StringEscapeUtils.unescapeHtml4(
            body
                .replace("""<[^>]*>""".toRegex(), "")
        )

        return listOf(OutgoingMessage(bodyWithoutHtml.replace("Sloganizer", "*$formattedName*")))
    }

    override fun getHelp(command: String) = listOf(
        "`!slogan <object>` prints a slogan for `<object>`"
    )

    override fun getProblems(): List<String> {
        if (failedRequests > 0) {
            return listOf("$failedRequests failed requests")
        }
        return emptyList()
    }

    override fun getAdditionalStatus() = mapOf("Failed requests" to failedRequests.toString())
}
