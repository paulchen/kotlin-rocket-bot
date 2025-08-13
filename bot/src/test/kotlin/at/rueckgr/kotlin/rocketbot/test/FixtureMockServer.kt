package at.rueckgr.kotlin.rocketbot.test

import io.ktor.http.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.apache.commons.io.FileUtils
import java.io.File

class FixtureMockServer(private val directory: String) {
    private val mockWebServer = MockWebServer()

    fun start(): Int {
        val files = File({}.javaClass.getResource(directory)!!.toURI()).listFiles()!!
        files.sort()
        files.forEach {
            mockWebServer.enqueue(
                MockResponse()
                    .setHeader(HttpHeaders.ContentType, "application/json")
                    .setBody(FileUtils.readFileToString(it, "UTF-8"))
            )
        }

        mockWebServer.start()

        return mockWebServer.port
    }

    fun shutdown() = mockWebServer.shutdown()

    fun hasMoreResponses() = mockWebServer.dispatcher.peek().getBody() != null
}
