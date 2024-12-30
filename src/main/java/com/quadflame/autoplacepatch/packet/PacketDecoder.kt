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

    private var lastLocation = player.location
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
            is PacketPlayInFlying -> handleFlyPacket(packet)
            else -> true
        }
    }

    private fun handleFlyPacket(packet: PacketPlayInFlying): Boolean {
        sentBlock = false

        if(packet.g()) {
            lastLocation.x = packet.a()
            lastLocation.y = packet.b()
            lastLocation.z = packet.c()
        }

        if(packet.h()) {
            lastLocation.yaw = packet.d()
            lastLocation.pitch = packet.e()
        }

        lastLocation.world = player.world

        return true
    }

    @Suppress("DEPRECATION")
    private fun handleBlockPlacePacket(packet: PacketPlayInBlockPlace): Boolean {

        val blockPlaced = packet.face != 255
        val hasItem = packet.itemStack != null && packet.itemStack.item != null
        if (!blockPlaced || !hasItem) return true

        val item = packet.itemStack.item
        val isBlock = item is ItemBlock
        if(!isBlock) return true

        val isSlab = item is ItemStep
        if(isSlab) return true

        val worldServer = (player.world as CraftWorld).handle
        val position = packet.a()
        val block = worldServer.getType(position).block
        val material = Material.getMaterial(Block.getId(block))

        val isInteractable = material in INTERACTABLE_BLOCKS
        val sneaking = player.isSneaking
        if (isInteractable && !sneaking) return true

        val entityPlayer = (player as CraftPlayer).handle
        val direction = EnumDirection.fromType1(packet.face)
        val shifted = position.shift(direction)

        val isAir = worldServer.getType(shifted).block == Blocks.AIR
        if (!isAir) return true

        val height = 1.8
        val width = 0.6
        val halfWidth = width / 2.0
        val playerBoundingBox = AxisAlignedBB(
            lastLocation.x - halfWidth,
            lastLocation.y,
            lastLocation.z - halfWidth,
            lastLocation.x + halfWidth,
            lastLocation.y + height,
            lastLocation.z + halfWidth
        )
        val blockBoundingBox = AxisAlignedBB(
            shifted.x.toDouble(),
            shifted.y.toDouble(),
            shifted.z.toDouble(),
            shifted.x + 1.0,
            shifted.y + 1.0,
            shifted.z + 1.0
        )

        val isInside = playerBoundingBox.b(blockBoundingBox)
        if(isInside) return true

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
            val inventory = entityPlayer.inventory
            val container = entityPlayer.activeContainer
            val slot = container.getSlot(inventory, inventory.itemInHandIndex)
            val windowId = container.windowId
            val rawSlotIndex = slot.rawSlotIndex

            val connection = entityPlayer.playerConnection

            connection.sendPacket(PacketPlayOutBlockChange(worldServer, position))
            connection.sendPacket(PacketPlayOutBlockChange(worldServer, shifted))
            connection.sendPacket(PacketPlayOutSetSlot(windowId, rawSlotIndex, inventory.itemInHand))

            return false
        }

        return true
    }
}