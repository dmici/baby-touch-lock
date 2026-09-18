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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Full-screen background overlay view (Window 1 in WindowManager).
 *
 * Responsibilities:
 * - Covers the entire display using physical screen dimensions.
 * - Registers system gesture exclusion rects (`setSystemGestureExclusionRects`) to neutralize Back swipe and gesture navigation (Android 10+).
 * - Enforces `SYSTEM_UI_FLAG_IMMERSIVE_STICKY` and `SYSTEM_UI_FLAG_HIDE_NAVIGATION`.
 * - Intercepts and consumes all touch events to block unintended interactions.
 * - Detects touches near the top edge to proactively collapse the system notification shade.
 */
@SuppressLint("ViewConstructor")
class BabyTouchLockBackgroundView(
    context: Context,
    private val onSwipeTopDetected: (() -> Unit)? = null
) : FrameLayout(context) {

    private val exclusionRects = listOf(Rect())

    init {
        isFocusable = false
        isFocusableInTouchMode = false

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemGestures = insets.getInsets(
                WindowInsetsCompat.Type.systemGestures()
            )
            setHideNavigation(Build.VERSION.SDK_INT >= 29 && systemGestures.left != 0 && systemGestures.right != 0)
            insets
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (Build.VERSION.SDK_INT >= 29) {
            exclusionRects[0].set(left, top, right, bottom)
            setSystemGestureExclusionRects(exclusionRects)
        }
    }

    private fun setHideNavigation(hideNavigation: Boolean) {
        @Suppress("DEPRECATION")
        systemUiVisibility = if (hideNavigation) {
            systemUiVisibility or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        } else {
            systemUiVisibility and
                    (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION).inv()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_MOVE) {
            val topThreshold = resources.getDimension(R.dimen.touch_top_threshold)
            if (event.rawY < topThreshold) {
                onSwipeTopDetected?.invoke()
            }
        }
        return true
    }
}

