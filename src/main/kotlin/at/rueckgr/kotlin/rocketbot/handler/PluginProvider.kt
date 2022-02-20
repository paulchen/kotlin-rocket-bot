package at.rueckgr.kotlin.rocketbot.handler

import at.rueckgr.kotlin.rocketbot.plugins.AbstractPlugin
import org.reflections.Reflections

object PluginProvider {
    private val commandPlugins = HashMap<String, MutableList<AbstractPlugin>>()
    private val generalPlugins = ArrayList<AbstractPlugin>()
    private val allPlugins = ArrayList<AbstractPlugin>()

    init {
        val pluginInstances = Reflections(AbstractPlugin::class.java.packageName)
            .getSubTypesOf(AbstractPlugin::class.java)
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
                commandPlugins[command] = ArrayList()
            }
            commandPlugins[command]?.add(plugin)
        }
        allPlugins.add(plugin)
    }

    fun getByCommand(command: String): List<AbstractPlugin> = commandPlugins[command].orEmpty()

    fun getCommands() = commandPlugins.keys.toList()

    fun getGeneralPlugins() = generalPlugins.toList()

    fun getAllPlugins() = allPlugins.toList()
}
