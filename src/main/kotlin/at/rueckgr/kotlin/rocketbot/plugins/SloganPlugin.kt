package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.OutgoingMessage
import at.rueckgr.kotlin.rocketbot.util.formatUsername
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.apache.commons.text.StringEscapeUtils


class SloganPlugin : AbstractPlugin() {
    override fun getCommands() = listOf("slogan")

    override fun handle(message: String): List<OutgoingMessage> {
        val name = stripCommand(message) ?: return emptyList()
        val formattedName = formatUsername(name)

        val response = runBlocking {
            HttpClient(CIO).get {
                url("http://www.sloganizer.net/outbound.php")
            }.body<String>()
        }

        val responseWithoutHtml = StringEscapeUtils.unescapeHtml4(
            response
                .replace("""<[^>]*>""".toRegex(), "")
        )

        return listOf(OutgoingMessage(responseWithoutHtml.replace("Sloganizer", "*$formattedName*")))
    }

    override fun getHelp(command: String) = listOf(
        "`!slogan <object>` prints a slogan for `<object>`"
    )

    override fun getProblems() = emptyList<String>()
}
