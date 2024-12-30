package com.quadflame.autoplacepatch

import com.quadflame.autoplacepatch.listener.EventListeners
import org.bukkit.plugin.java.JavaPlugin

/**
 * Main plugin class for AutoPlacePatch.
 *
 * This class initializes the plugin and registers event listeners for player join and quit events.
 *
 * @since 1.0.0
 */
class AutoPlacePatch : JavaPlugin() {

    /**
     * Called when the plugin is enabled.
     *
     * This method is called when the plugin is enabled by the server. It registers event listeners
     * for player join and quit events to inject and uninject packet decoders for auto-place detection.
     */
    override fun onEnable() {
        saveDefaultConfig()

        val pluginManager = server.pluginManager
        pluginManager.registerEvents(EventListeners(this), this)
    }
}