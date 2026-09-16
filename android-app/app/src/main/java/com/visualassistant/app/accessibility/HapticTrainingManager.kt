package com.visualassistant.app.accessibility

data class HapticSignal(
    val name: String,
    val meaning: String
)

class HapticTrainingManager {

    private val signals = listOf(
        HapticSignal(
            name = "One short vibration",
            meaning = "Something has been detected."
        ),
        HapticSignal(
            name = "Two short vibrations",
            meaning = "An action is required."
        ),
        HapticSignal(
            name = "One long vibration",
            meaning = "The current session has ended."
        ),
        HapticSignal(
            name = "Three short vibrations",
            meaning = "The application mode has changed."
        ),
        HapticSignal(
            name = "Two long vibrations",
            meaning = "The application is processing."
        ),
        HapticSignal(
            name = "Long followed by short",
            meaning = "The application could not recognize something."
        )
    )

    var currentIndex: Int = 0
        private set

    val currentSignal: HapticSignal
        get() = signals[currentIndex]

    val isComplete: Boolean
        get() = currentIndex >= signals.lastIndex

    fun next(): Boolean {
        if (currentIndex < signals.lastIndex) {
            currentIndex++
            return true
        }

        return false
    }

    fun reset() {
        currentIndex = 0
    }
}