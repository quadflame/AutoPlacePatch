package com.quadflame.autoplacepatch.player

import org.bukkit.entity.Player

/**
 * Represents a user manager for the plugin.
 *
 * This class manages users of the plugin and provides methods to get and remove users.
 *
 * @since 1.0.0
 */
class UserManager {
    private val users = mutableMapOf<Player, User>()

    fun getUser(player: Player): User {
        return users.getOrPut(player) { User() }
    }

    fun removeUser(player: Player) {
        users.remove(player)
    }
}