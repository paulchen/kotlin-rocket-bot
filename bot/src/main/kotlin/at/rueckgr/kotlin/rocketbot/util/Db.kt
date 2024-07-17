package at.rueckgr.kotlin.rocketbot.util

import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect

class Db : Logging {
    val connection: Database

    init {
        val databaseConfiguration = ConfigurationProvider.getConfiguration().database
        try {
            connection = Database.connect(
                url = databaseConfiguration!!.url!!,
                user = databaseConfiguration.username,
                password = databaseConfiguration.password,
                dialect = PostgreSqlDialect()
            )
        }
        catch (e: Throwable) {
            logger().error("Exception when connecting to database", e)
            throw DbException(e.message ?: "Unknown error")
        }
    }
}

class DbException(message: String): Exception(message)
