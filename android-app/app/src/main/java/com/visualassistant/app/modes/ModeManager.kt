package com.visualassistant.app.modes

class ModeManager {

    var currentMode: AppMode = AppMode.READ
        private set

    fun switchToRead() {
        currentMode = AppMode.READ
    }

    fun switchToCurrency() {
        currentMode = AppMode.CURRENCY
    }

    fun toggleMode() {
        currentMode = when (currentMode) {
            AppMode.READ -> AppMode.CURRENCY
            AppMode.CURRENCY -> AppMode.READ
        }
    }
}