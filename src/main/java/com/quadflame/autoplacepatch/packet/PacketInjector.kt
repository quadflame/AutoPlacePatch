package com.quadflame.autoplacepatch.packet

import com.quadflame.autoplacepatch.config.Settings
import io.netty.channel.ChannelPipeline
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 * Injects and uninjects packet decoders for auto-place detection.
 *
 * This class manages the injection and un-injection of packet decoders for auto-place detection.
 * When a player joins the server, a new packet decoder is injected into the player's network channel
 * to monitor block placements. When a player quits the server, the packet decoder is removed to prevent
 * memory leaks.
 *
 * @param plugin The JavaPlugin instance this injector belongs to
 * @property packetDecoders A map of players to their respective packet decoders
 * @property settings The Settings instance to manage configuration settings
 * @see PacketDecoder
 * @see Settings
 * @since 1.0.0
 */
class PacketInjector(plugin: JavaPlugin) {
    private val packetDecoders = mutableMapOf<Player, PacketDecoder>()
    private val settings = Settings(plugin)

    /**
     * Injects the packet decoder into the player's network channel.
     *
     * @param player The player to inject the packet decoder for
     */
    fun inject(player: Player) {
        val decoder = PacketDecoder(player, settings)
        packetDecoders += player to decoder
        getPipeline(player).addAfter("decoder", "autoplacepatch-decoder", decoder)
    }

    /**
     * Removes the packet decoder from the player's network channel.
     *
     * @param player The player to remove the packet decoder from
     */
    fun uninject(player: Player) {
        packetDecoders[player]?.let {
            if(getPipeline(player).get("autoplacepatch-decoder") == null) return@let
            getPipeline(player).remove("autoplacepatch-decoder")
        }
        packetDecoders -= player
    }

    /**
     * Gets the player's network channel pipeline.
     *
     * @param player The player to get the pipeline for
     * @return The player's network channel pipeline
     * @see ChannelPipeline
     */
    private fun getPipeline(player: Player): ChannelPipeline {
        return (player as CraftPlayer)
            .handle
            .playerConnection
            .networkManager
            .channel
            .pipeline()
    }
}