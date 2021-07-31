package at.rueckgr.kotlin.rocketbot.handler.message

import at.rueckgr.kotlin.rocketbot.BotConfiguration
import at.rueckgr.kotlin.rocketbot.webservice.LoginMessage
import at.rueckgr.kotlin.rocketbot.webservice.PasswordData
import at.rueckgr.kotlin.rocketbot.webservice.UserData
import at.rueckgr.kotlin.rocketbot.webservice.WebserviceRequestParam
import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.codec.digest.DigestUtils

class ConnectedMessageHandler : AbstractMessageHandler() {
    override fun getHandledMessage() = "connected"

    override fun handleMessage(configuration: BotConfiguration, data: JsonNode, timestamp: Long): Array<Any> {
        val digest = DigestUtils.sha256Hex(configuration.password)
        return arrayOf(
            LoginMessage(
                id = "login-initial",
                params = arrayOf(
                    WebserviceRequestParam(
                        UserData(configuration.username),
                        PasswordData(digest, "sha-256")
                    )
                )
            )
        )
    }
}
