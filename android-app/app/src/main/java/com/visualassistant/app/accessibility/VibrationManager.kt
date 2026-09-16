package com.visualassistant.app.accessibility
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
class VibrationManager(context: Context) {
    private val vibrator: Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    fun short() {
        vibrate(150)
    }
    fun doubleShort() {
        vibratePattern(longArrayOf(0, 150, 100, 150))
    }
    fun long() {
        vibrate(500)
    }
    fun tripleShort() {
        vibratePattern(longArrayOf(0, 120, 100, 120, 100, 120))
    }
    fun processing() {
        vibratePattern(longArrayOf(0, 350, 150, 350))
    }
    fun error() {
        vibratePattern(longArrayOf(0, 500, 150, 150))
    }
    fun stop() {
        vibrator.cancel()
    }
    private fun vibrate(duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    duration,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
    private fun vibratePattern(pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    pattern,
                    -1
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
}
