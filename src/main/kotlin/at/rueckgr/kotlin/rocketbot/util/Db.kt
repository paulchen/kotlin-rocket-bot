package at.rueckgr.kotlin.rocketbot.util

import org.ktorm.database.Database

class Db {
    val connection: Database;

    init {
        val databaseConfiguration = ConfigurationProvider.getConfiguration().database
        connection = Database.connect(
            url = "jdbc:postgresql://${databaseConfiguration!!.host}/${databaseConfiguration.database}",
            user = databaseConfiguration.username,
            password = databaseConfiguration.password)
    }
}
