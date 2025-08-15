package com.quadflame.autoplacepatch.command

import com.quadflame.autoplacepatch.AutoPlacePatch
import com.quadflame.autoplacepatch.config.Permissions
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Handles the /autoplacepatch command.
 *
 * This class implements the CommandExecutor interface to handle the /autoplacepatch command.
 * It provides the command logic to manage the plugin's auto-place patching features.
 *
 * @param plugin The AutoPlacePatch instance this command belongs to
 * @since 1.0.0
 */
class AutoPlacePatchCommand(private val plugin: AutoPlacePatch) : CommandExecutor {
    private val usage = "${RED}/autoplacepatch <alerts|reload>"
    private val reloaded = "${GREEN}Reloaded AutoPlacePatch configuration"
    private val onlyPlayersAlerts = "${RED}Only players can toggle alerts"
    private val enabledAlerts = "${GREEN}Enabled staff alerts"
    private val disabledAlerts = "${RED}Disabled staff alerts"

    /**
     * Called when the plugin command is executed.
     *
     * This method is called when the plugin command is executed by a player or console.
     * It handles the command logic and returns a boolean indicating success or failure.
     *
     * @param sender The command sender
     * @param command The command instance
     * @param alias The command alias
     * @param args The command arguments
     * @return true if the command was executed successfully, false otherwise
     */
    override fun onCommand(sender: CommandSender, command: Command, alias: String, args: Array<out String>): Boolean {

        if(args.isEmpty()) {
            sender.sendMessage(usage)
            return true
        }

        when (args.first().lowercase()) {
            "alerts" -> {
                if (!sender.hasPermission(Permissions.ALERTS)) return true

                if(sender !is Player) {
                    sender.sendMessage(onlyPlayersAlerts)
                    return true
                }

                val user = plugin.userManager.getUser(sender)
                user.toggleAlerts()

                when (user.alerts) {
                    true -> sender.sendMessage(enabledAlerts)
                    false -> sender.sendMessage(disabledAlerts)
                }
            }

            "reload" -> {
                if (!sender.hasPermission(Permissions.RELOAD)) return true

                plugin.reloadConfig()
                sender.sendMessage(reloaded)
            }

            else -> sender.sendMessage(usage)
        }

        return true
    }
}