package at.rueckgr.kotlin.rocketbot.plugins

import at.rueckgr.kotlin.rocketbot.util.formatUsername
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.apache.commons.text.StringEscapeUtils


class SloganPlugin : AbstractPlugin() {
    override fun getCommands() = listOf("slogan")

    override fun handle(message: String): List<String> {
        val name = stripCommand(message) ?: return emptyList()
        val formattedName = formatUsername(name)

        val response = runBlocking {
            HttpClient(CIO).request<String> {
                url("http://www.sloganizer.net/outbound.php")
                method = HttpMethod.Get
            }
        }

        val responseWithoutHtml = StringEscapeUtils.unescapeHtml4(
            response
                .replace("""<[^>]*>""".toRegex(), "")
        )

        return listOf(responseWithoutHtml.replace("Sloganizer", "*$formattedName*"))
    }

    override fun getHelp(command: String) = listOf(
        "`!slogan <object>` prints a slogan for `<object>`"
    )
}
