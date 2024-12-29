package com.quadflame.autoplacepatch.packet

import com.quadflame.autoplacepatch.config.Settings
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.minecraft.server.v1_8_R3.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*


class PacketDecoder(
    private val player: Player,
    private val settings: Settings
) : ChannelDuplexHandler() {

    companion object {
        private val INTERACTABLE_BLOCKS = EnumSet.of(
            Material.DISPENSER,
            Material.NOTE_BLOCK,
            Material.CHEST,
            Material.WORKBENCH,
            Material.FURNACE,
            Material.BURNING_FURNACE,
            Material.LEVER,
            Material.STONE_BUTTON,
            Material.FENCE,
            Material.TRAP_DOOR,
            Material.FENCE_GATE,
            Material.NETHER_FENCE,
            Material.ENCHANTMENT_TABLE,
            Material.ENDER_CHEST,
            Material.BEACON,
            Material.WOOD_BUTTON,
            Material.ANVIL,
            Material.TRAPPED_CHEST,
            Material.DAYLIGHT_DETECTOR,
            Material.DAYLIGHT_DETECTOR_INVERTED,
            Material.HOPPER,
            Material.DROPPER,
            Material.IRON_TRAPDOOR,
            Material.SPRUCE_FENCE,
            Material.BIRCH_FENCE,
            Material.JUNGLE_FENCE,
            Material.DARK_OAK_FENCE,
            Material.ACACIA_FENCE,
            Material.SPRUCE_FENCE_GATE,
            Material.BIRCH_FENCE_GATE,
            Material.JUNGLE_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE,
            Material.ACACIA_FENCE_GATE,
            Material.SIGN_POST,
            Material.WALL_SIGN,
            Material.WOODEN_DOOR,
            Material.IRON_DOOR_BLOCK,
            Material.CAKE_BLOCK,
            Material.BED_BLOCK,
            Material.DIODE_BLOCK_ON,
            Material.DIODE_BLOCK_OFF,
            Material.BREWING_STAND,
            Material.CAULDRON,
            Material.ITEM_FRAME,
            Material.FLOWER_POT,
            Material.REDSTONE_COMPARATOR,
            Material.REDSTONE_COMPARATOR_OFF,
            Material.REDSTONE_COMPARATOR_ON,
            Material.SPRUCE_DOOR,
            Material.BIRCH_DOOR,
            Material.JUNGLE_DOOR,
            Material.ACACIA_DOOR,
            Material.DARK_OAK_DOOR,
            Material.COMMAND
        )
    }

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

    @Suppress("DEPRECATION")
    private fun handleBlockPlacePacket(packet: PacketPlayInBlockPlace): Boolean {
        val blockPlaced = packet.face != 255
        val hasItem = packet.itemStack != null && packet.itemStack.item != null

        if (!blockPlaced || !hasItem) return true

        val isBlock = packet.itemStack.item is ItemBlock

        val worldServer = (player.world as CraftWorld).handle
        val position = packet.a()

        val block = worldServer.getType(position).block
        val material = Material.getMaterial(Block.getId(block))

        val isInteractable = material in INTERACTABLE_BLOCKS
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