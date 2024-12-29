package com.quadflame.autoplacepatch.config

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Settings(plugin: JavaPlugin) {
    private val config = plugin.config

    fun shouldAlert(): Boolean {
        return config.getBoolean("alerts.enabled")
    }

    fun getAlertMessage(player: Player): String {
        val message = config.getString("alerts.message")
        val coloredMessage = ChatColor.translateAlternateColorCodes('&', message)
        val formattedMessage = coloredMessage
            .replace("%player%", player.name)
            .replace("%uuid%", player.uniqueId.toString())
        return formattedMessage
    }

    fun getAlertPermission(): String {
        return config.getString("alerts.permission")
    }

    fun shouldPatch(): Boolean {
        return config.getBoolean("patch.enabled")
    }

    fun shouldPunish(): Boolean {
        return config.getBoolean("punishments.enabled")
    }

    fun getPunishmentCommand(player: Player): String {
        val command = config.getString("punishments.command")
        val formattedCommand = command
            .replace("%player%", player.name)
            .replace("%uuid%", player.uniqueId.toString())
        return formattedCommand
    }
}