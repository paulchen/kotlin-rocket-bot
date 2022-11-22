package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.handler.PluginProvider
import at.rueckgr.kotlin.rocketbot.util.Db
import at.rueckgr.kotlin.rocketbot.util.Logging
import at.rueckgr.kotlin.rocketbot.util.logger

class BotHealthChecker : HealthChecker, Logging {
    override fun performHealthCheck(): List<HealthProblem> {
        val problems = ArrayList<HealthProblem>()

        try {
            Db().connection.useConnection { c -> c.createStatement().use { it.executeQuery("SELECT 1") } }
        }
        catch (e: Exception) {
            logger().error("Exception occurred while connecting to the database", e)
            problems.add(HealthProblem("database", "connection", e.message ?: "unable to connect to database"))
        }

        PluginProvider
            .getAllPlugins()
            .flatMap {
                try {
                    it.getProblems()
                }
                catch (e: Throwable) {
                    logger().error("Exception occurred when checking health status if plugin {}:", it.javaClass.simpleName, e)
                    listOf("Unable to check health status of plugin: ${e.message}")
                }
                    .map { problem -> HealthProblem("plugins", it.javaClass.simpleName, problem) }
            }
            .forEach { problems.add(it) }

        return problems
    }

    override fun getAdditionalStatusInformation() = PluginProvider
            .getAllPlugins()
            .map { it.javaClass.simpleName to it.getAdditionalStatus() }
            .filter { it.second.isNotEmpty() }
            .toMap()
}
