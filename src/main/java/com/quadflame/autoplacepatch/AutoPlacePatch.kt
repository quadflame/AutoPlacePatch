package com.quadflame.autoplacepatch

import com.quadflame.autoplacepatch.command.AutoPlacePatchCommand
import com.quadflame.autoplacepatch.config.Settings
import com.quadflame.autoplacepatch.listener.EventListeners
import com.quadflame.autoplacepatch.player.UserManager
import org.bukkit.plugin.java.JavaPlugin

/**
 * Main plugin class for AutoPlacePatch.
 *
 * This class initializes the plugin and registers event listeners for player join and quit events.
 *
 * @since 1.0.0
 */
class AutoPlacePatch : JavaPlugin() {
    val settings = Settings(this)
    val userManager = UserManager()

    /**
     * Called when the plugin is enabled.
     *
     * This method is called when the plugin is enabled by the server. It registers event listeners
     * for player join and quit events to inject and uninject packet decoders for auto-place detection.
     */
    override fun onEnable() {
        saveDefaultConfig()
        config.options().copyDefaults(true)

        getCommand("autoplacepatch")?.executor = AutoPlacePatchCommand(this)

        val pluginManager = server.pluginManager
        pluginManager.registerEvents(EventListeners(this), this)
    }
}