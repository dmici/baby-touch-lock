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

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

/**
 * Quick Settings Tile service providing 1-tap lock/unlock toggle from the notification drawer.
 */
class BabyTouchLockTileService : TileService() {

    override fun onClick() {
        super.onClick()

        val service = BabyTouchLockAccessibilityService.instance
        if (service == null) {
            updateTileState(false)
            Toast.makeText(this, R.string.accessibility_needed_toast, Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivityAndCollapseCompat(intent)
            return
        }

        if (BabyTouchLockAccessibilityService.isLocked) {
            // Unlock and remove lock overlays
            service.unlock()
            updateTileState(false)
        } else {
            // 1. Collapse Quick Settings drawer via AccessibilityService
            service.collapseSystemUI()

            // 2. Engage screen lock
            service.lock()
            updateTileState(true)
        }
    }

    /**
     * Starts activity and collapses the notification shade across different Android versions.
     */
    private fun startActivityAndCollapseCompat(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    override fun onTileAdded() {
        super.onTileAdded()
        PreferencesManager.setTileAdded(this, true)
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        PreferencesManager.setTileAdded(this, false)
    }

    override fun onStartListening() {
        super.onStartListening()
        PreferencesManager.setTileAdded(this, true)
        activeInstance = this
        updateTileState(BabyTouchLockAccessibilityService.isLocked)
    }

    override fun onStopListening() {
        super.onStopListening()
        if (activeInstance === this) {
            activeInstance = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activeInstance === this) {
            activeInstance = null
        }
    }

    /**
     * Updates visual state (active/inactive) and label of the Quick Settings tile.
     */
    fun updateTileState(isActive: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        tile.updateTile()
    }

    companion object {
        private var activeInstance: BabyTouchLockTileService? = null

        /**
         * Requests tile UI update on the active tile instance or notifies SystemUI.
         */
        fun updateTile(context: Context, isLocked: Boolean) {
            // 1. Update active listening tile instance immediately if available
            activeInstance?.updateTileState(isLocked)

            // 2. Request SystemUI to update tile state
            try {
                requestListeningState(
                    context,
                    ComponentName(context, BabyTouchLockTileService::class.java)
                )
            } catch (_: Exception) {}
        }
    }
}

