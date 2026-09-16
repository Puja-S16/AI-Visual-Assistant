package com.visualassistant.app.modes

import android.view.KeyEvent

class VolumeButtonHandler(
    private val onVolumeUp: () -> Unit,
    private val onVolumeDown: () -> Unit
) {

    fun handle(event: KeyEvent): Boolean {

        if (event.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        if (event.repeatCount > 0) {
            return true
        }

        return when (event.keyCode) {

            KeyEvent.KEYCODE_VOLUME_UP -> {
                onVolumeUp()
                true
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                onVolumeDown()
                true
            }

            else -> false
        }
    }
}