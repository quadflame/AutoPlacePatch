package com.quadflame.autoplacepatch.config

import com.quadflame.autoplacepatch.AutoPlacePatch
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.configuration.file.FileConfiguration

/**
 * Manages configuration settings for the plugin.
 *
 * This class provides a clean interface to access and manage all configuration options
 * from the plugin's config.yml file, including alert settings, punishment configurations,
 * and patch toggles.
 *
 * @param plugin The AutoPlacePatch instance this settings manager belongs to
 * @property config The FileConfiguration instance representing config.yml
 * @see FileConfiguration
 * @since 1.0.0
 */
class Settings(private val plugin: AutoPlacePatch) {
    private val config = plugin.config

    /**
     * Checks if the auto-place exploit patch is enabled.
     * Patching will cancel the block placement packet if the player is caught auto-placing.
     *
     * Configuration path: `patch.enabled`
     *
     * @return true if the patch is enabled, false otherwise
     */
    fun shouldCancel(): Boolean {
        return config.getBoolean("patch.cancel")
    }

    /**
     * Checks if staff alerts are enabled for auto-place detection.
     *
     * Configuration path: `alerts.enabled`
     *
     * @return true if staff alerts are enabled, false otherwise
     */
    fun shouldAlert(): Boolean {
        return config.getBoolean("alerts.enabled")
    }

    /**
     * Formats and returns the alert message for staff members.
     *
     * Configuration path: `alerts.message`
     * Supports color codes with '&' prefix.
     * Available placeholders:
     * - %player% : Player's name
     * - %uuid% : Player's UUID
     *
     * @param player The player caught using auto-place
     * @return Formatted alert message with colors and placeholders replaced
     */
    fun getAlertMessage(player: Player): String {
        val message = config.getString("alerts.message")
        val coloredMessage = ChatColor.translateAlternateColorCodes('&', message)
        val formattedMessage = coloredMessage
            .replace("%player%", player.name)
            .replace("%uuid%", player.uniqueId.toString())
        return formattedMessage
    }

    /**
     * Gets the permission node required for staff to receive auto-place alerts.
     *
     * Configuration path: `alerts.permission`
     *
     * @return The permission node string
     */
    fun getAlertPermission(): String {
        return config.getString("alerts.permission")
    }

    /**
     * Checks if punishments are enabled for auto-place violations.
     *
     * Configuration path: `punishments.enabled`
     *
     * @return true if punishments are enabled, false otherwise
     */
    fun shouldPunish(): Boolean {
        return config.getBoolean("punishments.enabled")
    }

    /**
     * Formats and returns the punishment command for a violation.
     *
     * Configuration path: `punishments.command`
     * Available placeholders:
     * - %player% : Player's name
     * - %uuid% : Player's UUID
     *
     * @param player The player to be punished
     * @return Formatted punishment command with placeholders replaced
     */
    fun getPunishmentCommand(player: Player): String {
        val command = config.getString("punishments.command")
        val formattedCommand = command
            .replace("%player%", player.name)
            .replace("%uuid%", player.uniqueId.toString())
        return formattedCommand
    }
}