package at.rueckgr.kotlin.rocketbot.webservice

data class ResultMessage(val msg: String, val id: String, val result: ResultData)

data class ResultData(val id: String, val token: String, val tokenExpires: ExpirationDetails, val type: String)

data class ExpirationDetails(val date: Long)
