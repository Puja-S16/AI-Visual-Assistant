package com.visualassistant.app.modes

sealed class UserAction {

    data object Capture : UserAction()

    data object EndSession : UserAction()

    data object SwitchToRead : UserAction()

    data object SwitchToCurrency : UserAction()

    data object RepeatInstruction : UserAction()

    data object NextTrainingSignal : UserAction()

    data object StartReadMode : UserAction()

    data object StartCurrencyMode : UserAction()
}

class InteractionController {

    fun onVolumeUp(state: AppState): UserAction {
        return when (state) {

            AppState.ONBOARDING ->
                UserAction.NextTrainingSignal

            AppState.HAPTIC_TRAINING ->
                UserAction.RepeatInstruction

            AppState.MODE_SELECTION ->
                UserAction.StartReadMode

            AppState.READ_SCANNING ->
                UserAction.SwitchToRead

            AppState.READ_PROCESSING ->
                UserAction.SwitchToRead

            AppState.READ_CONVERSATION ->
                UserAction.SwitchToRead

            AppState.CURRENCY_SCANNING ->
                UserAction.SwitchToRead

            AppState.CURRENCY_PROCESSING ->
                UserAction.SwitchToRead

            AppState.CURRENCY_RESULT ->
                UserAction.SwitchToRead
        }
    }

    fun onVolumeDown(state: AppState): UserAction {
        return when (state) {

            AppState.ONBOARDING ->
                UserAction.NextTrainingSignal

            AppState.HAPTIC_TRAINING ->
                UserAction.NextTrainingSignal

            AppState.MODE_SELECTION ->
                UserAction.StartCurrencyMode

            AppState.READ_SCANNING ->
                UserAction.SwitchToCurrency

            AppState.READ_PROCESSING ->
                UserAction.SwitchToCurrency

            AppState.READ_CONVERSATION ->
                UserAction.SwitchToCurrency

            AppState.CURRENCY_SCANNING ->
                UserAction.SwitchToCurrency

            AppState.CURRENCY_PROCESSING ->
                UserAction.SwitchToCurrency

            AppState.CURRENCY_RESULT ->
                UserAction.SwitchToCurrency
        }
    }
}