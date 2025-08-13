package com.quadflame.autoplacepatch.packet

import com.quadflame.autoplacepatch.AutoPlacePatch
import com.quadflame.autoplacepatch.config.Settings
import com.viaversion.viaversion.api.Via
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.minecraft.server.v1_8_R3.*
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

/**
 * Decodes incoming packets to detect auto-place behavior.
 *
 * This class decodes incoming packets to detect auto-place behavior by players. When a player
 * places a block, the packet decoder checks if the block placement is valid and alerts staff
 * members if necessary. The decoder optionally patches the block placement to prevent the block from
 * being placed if it is invalid.
 *
 * @param player The player to decode packets for
 * @param plugin The AutoPlacePatch instance this decoder belongs to
 * @see Settings
 * @property settings The Settings instance to manage configuration settings
 * @property requestedBlock The last block requested by the player
 * @property lastLocation The last known location of the player
 * @property sentBlock Whether a block placement packet has already been sent
 * @since 1.0.0
 */
class PacketDecoder(
    private val player: Player,
    private val plugin: AutoPlacePatch
) : ChannelDuplexHandler() {

    /**
     * A set of interactable blocks that should be ignored for auto-place detection.
     */
    companion object {
        private const val HEIGHT = 1.8
        private const val HALF_WIDTH = 0.6 / 2.0
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
            Material.COMMAND,
            Material.VINE
        )
    }

    private val settings = plugin.settings
    private var requestedBlock = BlockPosition(0, 0, 0)
    private var lastLocation = player.location
    private var sentBlock = false

    /**
     * Handles incoming packets to detect auto-place behavior.
     *
     * This method handles incoming messages by dispatching packets and cancelling the message if necessary.
     *
     * @param context The channel context
     * @param message The incoming packet
     */
    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        var cancel = false

        if (message is Packet<*>) {
            cancel = !handlePacket(message)
        }

        if (cancel) return

        super.channelRead(context, message)
    }

    /**
     * Handles incoming packets to detect auto-place behavior.
     *
     * This method handles incoming packets by dispatching them to the appropriate packet handler
     *
     * @param packet The incoming packet
     * @return true if the packet should be allowed, false otherwise
     */
    private fun handlePacket(packet: Packet<*>): Boolean {
        return when (packet) {
            is PacketPlayInBlockPlace -> handleBlockPlacePacket(packet)
            is PacketPlayInFlying -> handleFlyPacket(packet)
            else -> true
        }
    }

    /**
     * Handles incoming flying packets to update the player's last known location.
     *
     * This method handles incoming flying packets to update the player's last known location
     * and resets the sentBlock flag.
     *
     * @param packet The incoming flying packet
     * @return true if the packet should be allowed, false otherwise
     */
    private fun handleFlyPacket(packet: PacketPlayInFlying): Boolean {
        sentBlock = false

        if (packet.g()) {
            lastLocation.x = packet.a()
            lastLocation.y = packet.b()
            lastLocation.z = packet.c()
        }

        if (packet.h()) {
            lastLocation.yaw = packet.d()
            lastLocation.pitch = packet.e()
        }

        lastLocation.world = player.world

        return true
    }

    /**
     * Handles incoming block placement packets to detect auto-place behavior.
     *
     * This method handles incoming block placement packets to detect auto-place behavior by players.
     * If a player is caught auto-placing, staff members are alerted and the block placement is optionally
     * patched to prevent the block from being placed.
     *
     * @param packet The incoming block placement packet
     * @return true if the packet should be allowed, false otherwise
     */
    @Suppress("DEPRECATION")
    private fun handleBlockPlacePacket(packet: PacketPlayInBlockPlace): Boolean {

        // Ignores players in adventure or spectator mode
        if (isGameModeInvalid()) return true

        // Checks if no block placement
        if (!isBlockPlaced(packet) || !hasItem(packet)) return true

        // Checks if item is not a block
        if (!isBlock(packet)) return true

        // Ignores slabs
        if (isSlab(packet)) return true

        val worldServer = (player.world as CraftWorld).handle
        val position = packet.a()

        // Checks if block is interactable and not placed
        if (isTargetInteractable(worldServer, position) && !isSneaking()) return true

        // Checks if block is already set
        val direction = EnumDirection.fromType1(packet.face)
        val shifted = position.shift(direction)

        val isAir = worldServer.getType(shifted).block == Blocks.AIR
        if (!isAir) return true

        val similarBlock = requestedBlock == shifted
        if (similarBlock) return true
        requestedBlock = shifted

        // Checks bounding box for 1.9+ clients that send invalid block placements
        if (isPlayerInsideBlock(shifted)) return true

        // Checks if block placement is valid
        if (!sentBlock) {
            sentBlock = true
            return true
        }

        // Alerts staff members of auto-place behavior
        if (settings.shouldAlert()) {
            val message = settings.getAlertMessage(player)
            val permission = settings.getAlertPermission()

            Bukkit.getOnlinePlayers()
                .filter { it.hasPermission(permission) }
                .filter { plugin.userManager.getUser(it).alerts }
                .forEach { it.sendMessage(message) }

            Bukkit.getConsoleSender().sendMessage(message)
        }

        // Punishes the player for auto-place behavior
        if (settings.shouldPunish()) {
            val command = settings.getPunishmentCommand(player)
            Bukkit.getScheduler().runTask(plugin) { -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command) }
        }

        // Patches the block placement to prevent the block from being placed
        if (settings.shouldCancel()) {
            val entityPlayer = (player as CraftPlayer).handle

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

    private fun isGameModeInvalid(): Boolean {
        return player.gameMode == GameMode.ADVENTURE || player.gameMode == GameMode.SPECTATOR
    }

    private fun isBlockPlaced(packet: PacketPlayInBlockPlace): Boolean {
        return packet.face != 255
    }

    private fun hasItem(packet: PacketPlayInBlockPlace): Boolean {
        return packet.itemStack != null && packet.itemStack.item != null
    }

    private fun isBlock(packet: PacketPlayInBlockPlace): Boolean {
        return packet.itemStack.item is ItemBlock
    }

    private fun isSlab(packet: PacketPlayInBlockPlace): Boolean {
        return packet.itemStack.item is ItemStep
    }

    @Suppress("DEPRECATION")
    private fun isTargetInteractable(worldServer: WorldServer, position: BlockPosition): Boolean {
        val block = worldServer.getType(position).block
        val material = Material.getMaterial(Block.getId(block))
        return material in INTERACTABLE_BLOCKS
    }

    private fun isSneaking(): Boolean {
        return player.isSneaking
    }

    private fun isPlayerInsideBlock(shifted: BlockPosition): Boolean {
        val playerBoundingBox = AxisAlignedBB(
            lastLocation.x - HALF_WIDTH,
            lastLocation.y,
            lastLocation.z - HALF_WIDTH,
            lastLocation.x + HALF_WIDTH,
            lastLocation.y + HEIGHT,
            lastLocation.z + HALF_WIDTH
        )
        val blockBoundingBox = AxisAlignedBB(
            shifted.x.toDouble(),
            shifted.y.toDouble(),
            shifted.z.toDouble(),
            shifted.x + 1.0,
            shifted.y + 1.0,
            shifted.z + 1.0
        )
        return playerBoundingBox.b(blockBoundingBox)
    }
}