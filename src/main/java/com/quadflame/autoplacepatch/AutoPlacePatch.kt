package com.quadflame.autoplacepatch

import com.quadflame.autoplacepatch.listener.EventListeners
import org.bukkit.plugin.java.JavaPlugin

class AutoPlacePatch : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()

        val pluginManager = server.pluginManager
        pluginManager.registerEvents(EventListeners(this), this)
    }
}