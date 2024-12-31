package com.quadflame.autoplacepatch.player

/**
 * Represents a user of the plugin.
 *
 * This class represents a user of the plugin. A user can toggle alerts on and off.
 *
 * @property alerts Whether the user has alerts enabled
 * @since 1.0.0
 */
class User(var alerts: Boolean = true) {
    fun toggleAlerts() {
        alerts = !alerts
    }
}