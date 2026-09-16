package com.visualassistant.app.modes

import android.os.Handler
import android.os.Looper

class TouchGestureHandler(
    private val onSingleTap: () -> Unit,
    private val onDoubleTap: () -> Unit
) {

    private val handler = Handler(Looper.getMainLooper())

    private var tapPending = false

    private val singleTapRunnable = Runnable {
        if (tapPending) {
            tapPending = false
            onSingleTap()
        }
    }

    fun onTap() {

        if (tapPending) {

            tapPending = false
            handler.removeCallbacks(singleTapRunnable)

            onDoubleTap()

        } else {

            tapPending = true

            handler.postDelayed(
                singleTapRunnable,
                DOUBLE_TAP_TIMEOUT
            )
        }
    }

    fun cancel() {
        tapPending = false
        handler.removeCallbacks(singleTapRunnable)
    }

    companion object {
        private const val DOUBLE_TAP_TIMEOUT = 300L
    }
}