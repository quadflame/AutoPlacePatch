package com.quadflame.autoplacepatch.listener

import com.quadflame.autoplacepatch.AutoPlacePatch
import com.quadflame.autoplacepatch.packet.PacketInjector
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Listens for player join and quit events to inject and uninject packet decoders.
 *
 * This class listens for player join and quit events to inject and uninject packet decoders
 * for auto-place detection. When a player joins, a new packet decoder is injected into the
 * player's network channel to monitor block placements. When a player quits, the packet decoder
 * is removed to prevent memory leaks.
 *
 * @param plugin The AutoPlacePatch instance this listener belongs to
 * @property injector The PacketInjector instance to manage packet decoders
 * @see PacketInjector
 * @since 1.0.0
 */
class EventListeners(private val plugin: AutoPlacePatch) : Listener {
    private val injector = PacketInjector(plugin)

    /**
     * Injects the packet decoder into the player's network channel when they join the server.
     *
     * @param event The PlayerJoinEvent instance
     */
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        injector.inject(event.player)
    }

    /**
     * Removes the packet decoder from the player's network channel when they quit the server.
     *
     * @param event The PlayerQuitEvent instance
     */
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        injector.uninject(event.player)
        plugin.userManager.removeUser(event.player)
    }
}