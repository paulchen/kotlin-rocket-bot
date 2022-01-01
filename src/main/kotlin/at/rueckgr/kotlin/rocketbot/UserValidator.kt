package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider

class UserValidator: WebserviceUserValidator {
    override fun validate(username: String, password: String): Boolean =
        ConfigurationProvider.instance.getConfiguration()
            .webservice
            ?.users
            ?.stream()
            ?.filter { it.username == username && it.password == password }
            ?.findAny()
            ?.isPresent
            ?: false
}
