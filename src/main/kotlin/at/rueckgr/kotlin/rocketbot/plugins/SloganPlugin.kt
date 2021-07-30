package at.rueckgr.kotlin.rocketbot.plugins

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking


class SloganPlugin : AbstractPlugin() {
    override fun getCommands() = listOf("slogan")

    override fun handle(message: String): List<String> {
        val pos = message.indexOf(" ")
        if (pos < 0) {
            return emptyList()
        }
        val name = message.substring(pos + 1)

        val response = runBlocking {
            HttpClient(CIO).request<String> {
                url("http://www.sloganizer.net/outbound.php")
                method = HttpMethod.Get
            }
        }

        return listOf(
            response
                .replace("""<[^>]*>""".toRegex(), "")
                .replace("Sloganizer", "*$name*")
        )
    }

    override fun getHelp(command: String) = listOf(
        "`!slogan <object>` prints a slogan for `<object>`"
    )
}