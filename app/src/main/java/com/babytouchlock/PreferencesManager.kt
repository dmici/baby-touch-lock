/*
 * Baby Touch Lock
 * Copyright (C) 2026 Davide Micieli
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.babytouchlock

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper object for managing application preferences and persistence via [SharedPreferences].
 */
object PreferencesManager {
    private const val PREFS_NAME = "baby_touch_lock_prefs"
    const val PREF_BLOCK_VOLUME = "pref_block_volume"
    const val PREF_AUTO_WAKE_POWER = "pref_auto_wake_power"
    const val PREF_TILE_ADDED = "pref_tile_added"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks if hardware volume key suppression is enabled.
     * Default: true (enabled).
     */
    fun isBlockVolumeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(PREF_BLOCK_VOLUME, true)
    }

    /**
     * Sets whether hardware volume keys should be suppressed while locked.
     */
    fun setBlockVolumeEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(PREF_BLOCK_VOLUME, enabled).apply()
    }

    /**
     * Checks if automatic display wake-up on power button press is enabled.
     * Default: false (disabled).
     */
    fun isAutoWakePowerEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(PREF_AUTO_WAKE_POWER, false)
    }

    /**
     * Sets whether the display should automatically wake up when the power button is pressed.
     */
    fun setAutoWakePowerEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(PREF_AUTO_WAKE_POWER, enabled).apply()
    }

    /**
     * Checks if the Quick Settings tile was added to the active status bar tiles.
     */
    fun isTileAdded(context: Context): Boolean {
        return getPrefs(context).getBoolean(PREF_TILE_ADDED, false)
    }

    /**
     * Updates whether the Quick Settings tile was added.
     */
    fun setTileAdded(context: Context, added: Boolean) {
        getPrefs(context).edit().putBoolean(PREF_TILE_ADDED, added).apply()
    }
}

