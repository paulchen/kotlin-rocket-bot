package at.rueckgr.kotlin.rocketbot.webservice

data class StreamRoomMessagesSubscriptionMessage(val msg: String = "sub", val id: String, val name: String = "stream-room-messages", val params: Array<Any?>)
