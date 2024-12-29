package com.quadflame.autoplacepatch.packet

import com.quadflame.autoplacepatch.config.Settings
import io.netty.channel.ChannelPipeline
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class PacketInjector(plugin: JavaPlugin) {
    private val packetDecoders = mutableMapOf<Player, PacketDecoder>()
    private val settings = Settings(plugin)

    fun inject(player: Player) {
        val decoder = PacketDecoder(player, settings)
        packetDecoders += player to decoder
        getPipeline(player).addAfter("decoder", "autoplacepatch-decoder", decoder)
    }

    fun uninject(player: Player) {
        packetDecoders[player]?.let {
            if(getPipeline(player).get("autoplacepatch-decoder") == null) return@let
            getPipeline(player).remove("autoplacepatch-decoder")
        }
        packetDecoders -= player
    }

    private fun getPipeline(player: Player): ChannelPipeline {
        return (player as CraftPlayer)
            .handle
            .playerConnection
            .networkManager
            .channel
            .pipeline()
    }
}