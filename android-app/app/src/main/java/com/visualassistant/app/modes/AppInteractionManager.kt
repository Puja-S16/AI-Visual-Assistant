package com.visualassistant.app.modes

class AppInteractionManager {

    private val interactionController = InteractionController()

    var currentState: AppState = AppState.ONBOARDING
        private set

    var currentMode: AppMode = AppMode.READ
        private set

    fun handleVolumeUp(): UserAction {
        return interactionController.onVolumeUp(currentState)
    }

    fun handleVolumeDown(): UserAction {
        return interactionController.onVolumeDown(currentState)
    }

    fun setState(state: AppState) {
        currentState = state
    }

    fun setMode(mode: AppMode) {
        currentMode = mode
    }

    fun applyAction(action: UserAction) {
        when (action) {

            UserAction.StartReadMode -> {
                currentMode = AppMode.READ
                currentState = AppState.READ_SCANNING
            }

            UserAction.StartCurrencyMode -> {
                currentMode = AppMode.CURRENCY
                currentState = AppState.CURRENCY_SCANNING
            }

            UserAction.SwitchToRead -> {
                currentMode = AppMode.READ
                currentState = AppState.READ_SCANNING
            }

            UserAction.SwitchToCurrency -> {
                currentMode = AppMode.CURRENCY
                currentState = AppState.CURRENCY_SCANNING
            }

            UserAction.Capture -> {
                currentState = when (currentMode) {
                    AppMode.READ -> AppState.READ_PROCESSING
                    AppMode.CURRENCY -> AppState.CURRENCY_PROCESSING
                }
            }

            UserAction.EndSession -> {
                currentState = when (currentMode) {
                    AppMode.READ -> AppState.READ_SCANNING
                    AppMode.CURRENCY -> AppState.CURRENCY_SCANNING
                }
            }

            UserAction.RepeatInstruction -> {
                // Speech/haptic replay will be handled by the accessibility layer.
            }

            UserAction.NextTrainingSignal -> {
                // Haptic training navigation will be handled by onboarding.
            }
        }
    }
}