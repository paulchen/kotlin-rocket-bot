package at.rueckgr.kotlin.rocketbot.webservice

data class RoomsGetMessage(val msg: String = "method", val method: String = "rooms/get", val id: String)
