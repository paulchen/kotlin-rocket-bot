package at.rueckgr.kotlin.rocketbot.handler

import at.rueckgr.kotlin.rocketbot.plugins.AbstractPlugin
import org.reflections.Reflections

class PluginProvider {
    private val plugins = HashMap<String, MutableList<AbstractPlugin>>()

    init {
        val pluginInstances = Reflections(AbstractPlugin::class.java.packageName)
            .getSubTypesOf(AbstractPlugin::class.java)
            .map { it.getDeclaredConstructor().newInstance() }

        pluginInstances.forEach { plugin ->
            plugin.getCommands().forEach { command ->
                if(command !in plugins) {
                    plugins[command] = ArrayList()
                }
                plugins[command]?.add(plugin)
            }
        }
    }


    fun getByCommand(command: String): List<AbstractPlugin> = plugins[command].orEmpty()
}
