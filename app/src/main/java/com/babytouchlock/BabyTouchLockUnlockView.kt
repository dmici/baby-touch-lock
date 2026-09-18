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
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import kotlin.math.abs

/**
 * Compact floating unlock widget (Window 2 in WindowManager).
 *
 * Displays a floating semi-transparent lock button that expands into a vertical slide-to-unlock
 * slider upon touch. Configured with `FLAG_NOT_FOCUSABLE` to avoid interfering with the underlying screen.
 */
class BabyTouchLockUnlockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var btnFloatingUnlock: TextView? = null
    private var sliderContainer: FrameLayout? = null
    private var sliderThumb: TextView? = null
    private var onUnlockConfirmed: (() -> Unit)? = null

    init {
        isFocusable = false
        isFocusableInTouchMode = false
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        btnFloatingUnlock = findViewById(R.id.btn_floating_unlock)
        sliderContainer = findViewById(R.id.slider_container)
        sliderThumb = findViewById(R.id.slider_thumb)
        setupSliderTouchListener()
    }

    /**
     * Registers a callback invoked when the user successfully completes the unlock slider drag.
     */
    fun setOnUnlockConfirmedListener(listener: () -> Unit) {
        this.onUnlockConfirmed = listener
    }

    /**
     * Resets the widget to its initial collapsed state showing only the floating lock icon.
     */
    fun resetToInitialState() {
        btnFloatingUnlock?.visibility = View.VISIBLE
        btnFloatingUnlock?.alpha = 0.5f
        sliderContainer?.visibility = View.GONE
        sliderThumb?.translationY = 0f
        sliderThumb?.text = context.getString(R.string.lock_icon_locked)
        sliderThumb?.alpha = 0.7f
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSliderTouchListener() {
        var initialRawY = 0f
        var maxDragDistance = 0f
        var isUnlockedTriggered = false

        val touchListener = View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialRawY = event.rawY
                    isUnlockedTriggered = false

                    btnFloatingUnlock?.visibility = View.GONE
                    sliderContainer?.visibility = View.VISIBLE

                    val trackH = sliderContainer?.height ?: 0
                    val thumbH = sliderThumb?.height ?: 0
                    val minDrag = resources.getDimension(R.dimen.slider_min_drag_distance)
                    maxDragDistance = (trackH - thumbH - 16).toFloat().coerceAtLeast(minDrag)

                    sliderThumb?.translationY = 0f
                    sliderThumb?.text = context.getString(R.string.lock_icon_locked)
                    sliderThumb?.alpha = 0.7f
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isUnlockedTriggered) return@OnTouchListener true

                    val deltaY = event.rawY - initialRawY
                    if (deltaY < 0) { // Dragging upwards
                        val targetY = deltaY.coerceIn(-maxDragDistance, 0f)
                        sliderThumb?.translationY = targetY

                        val progress = abs(targetY) / maxDragDistance
                        sliderThumb?.alpha = 0.6f + (progress * 0.4f)

                        // Unlock confirmed when reaching 95% of vertical travel distance
                        if (progress >= 0.95f) {
                            isUnlockedTriggered = true
                            sliderThumb?.text = context.getString(R.string.lock_icon_unlocked)
                            onUnlockConfirmed?.invoke()
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!isUnlockedTriggered) {
                        val animDuration = resources.getInteger(R.integer.slider_reset_anim_duration_ms).toLong()
                        sliderThumb?.animate()
                            ?.translationY(0f)
                            ?.setDuration(animDuration)
                            ?.withEndAction {
                                resetToInitialState()
                            }
                            ?.start()
                    }
                    true
                }

                else -> false
            }
        }

        btnFloatingUnlock?.setOnTouchListener(touchListener)
        sliderContainer?.setOnTouchListener(touchListener)
        sliderThumb?.setOnTouchListener(touchListener)
    }
}

