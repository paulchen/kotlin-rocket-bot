package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.plugins.AbstractPlugin
import at.rueckgr.kotlin.rocketbot.webservice.*
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.digest.DigestUtils
import org.reflections.Reflections
import java.util.*

fun main() {
    // TODO store authentication token somewhere
    val host = System.getenv("ROCKETCHAT_HOST") ?: return
    val username = System.getenv("ROCKETCHAT_USERNAME") ?: return
    val password = System.getenv("ROCKETCHAT_PASSWORD") ?: return

    val client = HttpClient(CIO) {
        install(WebSockets)
    }
    runBlocking {
        client.wss(
            method = HttpMethod.Get,
            host = host,
            path = "/websocket"
        ) {
            val messageOutputRoutine = launch { outputMessages(username, password) }
            val userInputRoutine = launch { inputMessages() }

            userInputRoutine.join()
            messageOutputRoutine.join()
        }
    }
}

suspend fun DefaultClientWebSocketSession.inputMessages() {
    sendMessage(ConnectMessage())
}

suspend fun DefaultClientWebSocketSession.sendMessage(message: Any) {
    val gson = Gson()

    // TODO implement token refresh

    // TODO use logging framework
    println("OUT: " + gson.toJson(message))
    send(Frame.Text(gson.toJson(message)))
}

suspend fun DefaultClientWebSocketSession.outputMessages(username: String, password: String) {
    try {
        for (message in incoming) {
            message as? Frame.Text ?: continue
            val text = message.readText()
            println("IN: " + text)

            @Suppress("UNCHECKED_CAST") val data = Gson().fromJson(text, Object::class.java) as Map<String, Any>
            if("msg" !in data) {
                continue
            }
            val responses: Array<Any> = when (val messageType = data["msg"]) {
                "connected" -> handleConnectedMessage(data, username, password)
                "result" -> handleResultMessage(data)
                "ping" -> handlePingMessage(data)
                "changed" -> handleChangedMessage(username, data)
                else -> {
                    println("Unknown message type \"$messageType\", ignoring message")
                    continue
                }
            }

            responses.forEach {
                sendMessage(it)
            }
        }
    }
    catch (e: Exception) {
        e.printStackTrace()
        println("Error while receiving: " + e.localizedMessage)
    }
}

fun handleConnectedMessage(data: Map<String, Any>, username: String, password: String): Array<Any> {
    val digest = DigestUtils.sha256Hex(password)
    return arrayOf(LoginMessage(
        id = "login-initial",
        params = arrayOf(
            WebserviceRequestParam(
                UserData(username),
                PasswordData(digest, "sha-256")
            )
        )
    ))
}

fun handleResultMessage(data: Map<String, Any>): Array<Any> {
    return when (data["id"]) {
        "login-initial" -> {
            val userId = (data["result"] as Map<*, *>)["id"]
            arrayOf(
                RoomsGetMessage(id = "get-rooms-initial"),
                SubscriptionMessage(id = "subscribe-stream-notify-user", name = "stream-notify-user", params = arrayOf("$userId/rooms-changed", false))
            )
        }
        "get-rooms-initial" -> handleGetRoomsResult(data)
        else -> emptyArray()
    }
}

@Suppress("UNCHECKED_CAST")
fun handleGetRoomsResult(data: Map<String, Any>): Array<Any> {
    val rooms: List<Map<String, Any>> = data["result"] as List<Map<String, Any>>
    return rooms.map {
        val id = it["_id"]
        SubscriptionMessage(id = "subscribe-$id", name = "stream-room-messages", params = arrayOf(id, false))
    }.toTypedArray()
}

fun handlePingMessage(data: Map<String, Any>): Array<Any> {
    return arrayOf(PongMessage())
}


fun handleChangedMessage(ownUsername: String, data: Map<String, Any>): Array<Any> {
    return when (data["collection"]) {
        "stream-room-messages" -> handleStreamRoomMessages(ownUsername, data)
        "stream-notify-user" -> handleStreamNotifyUser(data)
        else -> return emptyArray()
    }.flatten().toTypedArray()
}

@Suppress("UNCHECKED_CAST")
fun handleStreamRoomMessages(ownUsername: String, data: Map<String, Any>): List<List<Any>> {
    val fields = data["fields"] as Map<String, Any>
    val args = fields["args"] as List<Map<String, Any>>

    return args.map {
        val message = it["msg"] as String
        val roomId = it["rid"] as String

        if ("t" in it && it["t"] == "ru" && message == ownUsername) {
            listOf(UnsubscribeMessage(id = "subscribe-$roomId"))
        }
        else {
            val userData = it["u"] as Map<String, String>
            val username = userData["username"] ?: ""
            handleUserMessage(ownUsername, roomId, username, message.trim())
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun handleStreamNotifyUser(data: Map<String, Any>): List<List<Any>> {
    val fields = data["fields"] as Map<String, Any>
    val args = fields["args"] as List<Any>

    if (args[0] != "inserted") {
        return emptyList()
    }

    return args.subList(1, args.size).map {
        val details = it as Map<String, String>
        val roomId = details["_id"]

        listOf(SubscriptionMessage(id = "subscribe-$roomId", name = "stream-room-messages", params = arrayOf(roomId, false)))
    }
}

fun handleUserMessage(ownUsername: String, roomId: String, username: String, message: String): List<SendMessageMessage> {
    if (username == ownUsername) {
        return emptyList()
    }
    if (!message.startsWith("!")) {
        return emptyList()
    }

    val command = message.split(" ")[0].substring(1)
    val flatMap = Reflections(AbstractPlugin::class.java.packageName)
        .getSubTypesOf(AbstractPlugin::class.java)
        .flatMap {
            val plugin = it.getDeclaredConstructor().newInstance()
            if (plugin.getCommands().contains(command)) {
                plugin.handle(message)
            }
            else {
                emptyList()
            }
        }
    return flatMap
        .map {
            val id = UUID.randomUUID().toString()
            SendMessageMessage(id = id, params = listOf(mapOf("_id" to id, "rid" to roomId, "msg" to it)))
        }
}
