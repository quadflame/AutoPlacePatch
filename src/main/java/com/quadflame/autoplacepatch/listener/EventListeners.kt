package com.quadflame.autoplacepatch.listener

import com.quadflame.autoplacepatch.packet.PacketInjector
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class EventListeners(plugin: JavaPlugin) : Listener {
    private val injector = PacketInjector(plugin)

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        injector.inject(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        injector.uninject(event.player)
    }
}