package at.rueckgr.kotlin.rocketbot.webservice

data class SubscribeMessage(val msg: String = "sub", val id: String, val name: String, val params: Array<Any?>)