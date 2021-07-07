package at.rueckgr.kotlin.rocketbot.webservice

data class LoginMessage(val msg: String = "method", val method: String = "login", val id: String = "42", val params: Array<WebserviceRequestParam>)

data class WebserviceRequestParam(val user: UserData, val password: PasswordData)

data class UserData(val username: String)

data class PasswordData(val digest: String, val algorithm: String)
