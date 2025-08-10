package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.EventHandler
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.formatUsername
import at.rueckgr.kotlin.rocketbot.util.logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.apache.commons.text.StringEscapeUtils


class SloganPlugin : AbstractPlugin(), Logging {
    private var failedRequests = 0

    override fun getCommands() = listOf("slogan")

    override fun handle(channel: EventHandler.Channel, user: EventHandler.User, message: EventHandler.Message): List<OutgoingMessage> = runBlocking {
        val name = stripCommand(message.message) ?: return@runBlocking emptyList()
        val formattedName = formatUsername(name)

        val response = try {
            HttpClient(CIO).get {
                url("http://www.sloganizer.net/outbound.php")
            }
        }
        catch (e: HttpRequestTimeoutException) {
            failedRequests++
            logger().info("Request to sloganizer failed; {} failed requests in total", failedRequests, e)
            return@runBlocking emptyList()
        }

        if (response.status.value >= 400) {
            failedRequests++
            logger().info("Request to sloganizer failed, status code {}; {} failed requests in total", response.status, failedRequests)
            return@runBlocking emptyList()
        }

        val bodyWithoutHtml = StringEscapeUtils.unescapeHtml4(
            response.body<String>()
                .replace("""<[^>]*>""".toRegex(), "")
        )

        listOf(OutgoingMessage(bodyWithoutHtml.replace("Sloganizer", "*$formattedName*")))
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
