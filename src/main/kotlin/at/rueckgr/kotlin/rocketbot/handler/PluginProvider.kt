package at.rueckgr.kotlin.rocketbot.handler

import at.rueckgr.kotlin.rocketbot.plugins.AbstractPlugin
import at.rueckgr.kotlin.rocketbot.util.ConfigurationProvider
import org.reflections.Reflections

object PluginProvider {
    private val commandPlugins = HashMap<String, MutableList<AbstractPlugin>>()
    private val generalPlugins = ArrayList<AbstractPlugin>()
    private val allPlugins = ArrayList<AbstractPlugin>()

    init {
        val mutePlugins = ConfigurationProvider.getConfiguration().plugins?.mutePlugins ?: emptyList()
        val pluginInstances = Reflections(AbstractPlugin::class.java.packageName)
            .getSubTypesOf(AbstractPlugin::class.java)
            .filter { !mutePlugins.contains(it.simpleName) }
            .map { it.getDeclaredConstructor().newInstance() }

        pluginInstances.forEach { plugin ->

            val commands = plugin.getCommands()
            when (commands.isEmpty()) {
                true -> addGeneralPlugin(plugin)
                false -> addCommandPlugin(plugin, commands)
            }
        }
    }

    private fun addGeneralPlugin(plugin: AbstractPlugin) {
        generalPlugins.add(plugin)
        allPlugins.add(plugin)
    }

    private fun addCommandPlugin(plugin: AbstractPlugin, commands: List<String>) {
        commands.forEach { command ->
            if(command !in commandPlugins) {
                commandPlugins[command.lowercase()] = ArrayList()
            }
            commandPlugins[command.lowercase()]?.add(plugin)
        }
        allPlugins.add(plugin)
    }

    fun getByCommand(command: String): List<AbstractPlugin> = commandPlugins[command.lowercase()].orEmpty()

    fun getCommands() = commandPlugins.keys.toList()

    fun getGeneralPlugins() = generalPlugins.toList()

    fun getAllPlugins() = allPlugins.toList()

    fun reinit() {
        allPlugins.forEach { it.reinit() }
    }
}
