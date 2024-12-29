package com.quadflame.autoplacepatch.packet

import com.quadflame.autoplacepatch.config.Settings
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.minecraft.server.v1_8_R3.*
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player


class PacketDecoder(
    private val player: Player,
    private val settings: Settings
) : ChannelDuplexHandler() {
    private var sentBlock = false

    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        var cancel = false

        if (message is Packet<*>) {
            cancel = !handlePacket(message)
        }

        if (cancel) return

        super.channelRead(context, message)
    }

    private fun handlePacket(packet: Packet<*>): Boolean {
        return when (packet) {
            is PacketPlayInBlockPlace -> handleBlockPlacePacket(packet)
            is PacketPlayInFlying -> handleFlyPacket()
            else -> true
        }
    }

    private fun handleFlyPacket(): Boolean {
        sentBlock = false
        return true
    }

    private fun handleBlockPlacePacket(packet: PacketPlayInBlockPlace): Boolean {
        val blockPlaced = packet.face != 255
        val hasItem = packet.itemStack != null && packet.itemStack.item != null

        if (!blockPlaced || !hasItem) return true

        val isBlock = packet.itemStack.item is ItemBlock

        val worldServer = (player.world as CraftWorld).handle
        val position = packet.a()

        val block = worldServer.getType(position).block
        val isInteractable = block is BlockContainer
        val sneaking = player.isSneaking

        if (!isBlock || (isInteractable && !sneaking)) return true

        if (!sentBlock) {
            sentBlock = true
            return true
        }

        if(settings.shouldAlert()) {
            val message = settings.getAlertMessage(player)
            val permission = settings.getAlertPermission()

            Bukkit.broadcast(message, permission)
        }

        if(settings.shouldPunish()) {
            val command = settings.getPunishmentCommand(player)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
        }

        if(settings.shouldPatch()) {
            val entityPlayer = (player as CraftPlayer).handle

            val inventory = entityPlayer.inventory
            val container = entityPlayer.activeContainer
            val slot = container.getSlot(inventory, inventory.itemInHandIndex)
            val windowId = container.windowId
            val rawSlotIndex = slot.rawSlotIndex

            val direction = EnumDirection.fromType1(packet.face)
            val shifted = position.shift(direction)

            val connection = entityPlayer.playerConnection

            connection.sendPacket(PacketPlayOutBlockChange(worldServer, position))
            connection.sendPacket(PacketPlayOutBlockChange(worldServer, shifted))
            connection.sendPacket(PacketPlayOutSetSlot(windowId, rawSlotIndex, inventory.itemInHand))

            return false
        }

        return true
    }
}