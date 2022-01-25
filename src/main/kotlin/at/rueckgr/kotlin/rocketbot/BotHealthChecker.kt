package at.rueckgr.kotlin.rocketbot

import at.rueckgr.kotlin.rocketbot.handler.PluginProvider
import at.rueckgr.kotlin.rocketbot.util.Db

class BotHealthChecker : HealthChecker {
    override fun performHealthCheck(): List<HealthProblem> {
        val problems = ArrayList<HealthProblem>()

        try {
            Db().connection.useConnection { c -> c.createStatement().use { it.executeQuery("SELECT 1") } }
        }
        catch (e: Exception) {
            problems.add(HealthProblem("database", "connection", e.message ?: "unable to connect to database"))
        }

        problems.addAll(
            PluginProvider.instance
                .getAllPlugins()
                .flatMap {
                    it.getProblems()
                        .map { problem -> HealthProblem("plugins", it.javaClass.simpleName, problem) }
                }
        )

        return problems
    }
}
