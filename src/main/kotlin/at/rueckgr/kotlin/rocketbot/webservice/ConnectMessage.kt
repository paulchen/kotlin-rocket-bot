package at.rueckgr.kotlin.rocketbot.webservice

data class ConnectMessage(val msg: String = "connect", val version: String = "1", val support: Array<String> = arrayOf("1"))
