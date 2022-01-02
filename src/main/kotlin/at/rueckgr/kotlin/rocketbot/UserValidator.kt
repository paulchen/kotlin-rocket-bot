package at.rueckgr.kotlin.rocketbot

import at.favre.lib.crypto.bcrypt.BCrypt
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider

class UserValidator: WebserviceUserValidator {
    override fun validate(username: String, password: String): Boolean =
        ConfigurationProvider.instance.getConfiguration()
            .webservice
            ?.users
            ?.stream()
            ?.filter { it.username == username && BCrypt.verifyer().verify(password.toCharArray(), it.password).verified }
            ?.findAny()
            ?.isPresent
            ?: false
}
