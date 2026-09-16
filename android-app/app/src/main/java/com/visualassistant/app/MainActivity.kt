package com.visualassistant.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

import com.visualassistant.app.accessibility.HapticSignal
import com.visualassistant.app.accessibility.HapticTrainingManager
import com.visualassistant.app.accessibility.OnboardingPreferences
import com.visualassistant.app.accessibility.SpeechManager
import com.visualassistant.app.accessibility.SpeechRecognitionManager
import com.visualassistant.app.accessibility.VibrationManager

import com.visualassistant.app.modes.AppInteractionManager
import com.visualassistant.app.modes.AppState
import com.visualassistant.app.modes.UserAction
import com.visualassistant.app.modes.VolumeButtonHandler

import com.visualassistant.app.read.ReadSessionManager

import com.visualassistant.app.ui.camera.CameraScreen
import com.visualassistant.app.ui.onboarding.HapticTrainingScreen
import com.visualassistant.app.ui.onboarding.OnboardingScreen
import com.visualassistant.app.ui.theme.VisualAssistantTheme
import com.visualassistant.app.accessibility.OfflineTextRecognitionManager
import com.visualassistant.app.currency.CurrencyClassifier

class MainActivity : ComponentActivity() {

    private lateinit var vibrationManager: VibrationManager
    private lateinit var speechManager: SpeechManager
    private lateinit var hapticTrainingManager: HapticTrainingManager
    private lateinit var onboardingPreferences: OnboardingPreferences
    private lateinit var readSessionManager: ReadSessionManager
    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private lateinit var volumeButtonHandler: VolumeButtonHandler

    

    private val interactionManager = AppInteractionManager()

    private lateinit var offlineTextRecognitionManager: OfflineTextRecognitionManager

    private lateinit var currencyClassifier: CurrencyClassifier

    private var currentAppState by mutableStateOf(AppState.ONBOARDING)

    private val mainHandler = Handler(Looper.getMainLooper())

    private var listeningScheduled = false

    private val microphonePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                scheduleQuestionListening(500)
            } else {
                vibrationManager.error()

                speechManager.speak(
                    "Microphone permission is required " +
                            "to ask questions by voice."
                )
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        vibrationManager = VibrationManager(this)
        speechManager = SpeechManager(this)
        hapticTrainingManager = HapticTrainingManager()
        onboardingPreferences = OnboardingPreferences(this)
        readSessionManager = ReadSessionManager()

        offlineTextRecognitionManager =
            OfflineTextRecognitionManager()

        currencyClassifier = CurrencyClassifier(this)

        speechRecognitionManager = SpeechRecognitionManager(
            context = this,

            onResult = { question ->
                handleUserQuestion(question)
            },

            onError = { errorMessage ->

                if (currentAppState == AppState.READ_SCANNING &&
                    readSessionManager.isActive()
                ) {

                    vibrationManager.error()

                    speechManager.speak(errorMessage)

                    speechManager.speak(
                        "Please try asking your question again."
                    )

                    scheduleQuestionListening(3000)
                }
            }
        )

        val hasCompletedOnboarding =
            onboardingPreferences.hasCompletedOnboarding()

        val initialState =
            if (hasCompletedOnboarding) {
                AppState.MODE_SELECTION
            } else {
                AppState.ONBOARDING
            }

        interactionManager.setState(initialState)
        currentAppState = initialState

        volumeButtonHandler = VolumeButtonHandler(
            onVolumeUp = {
                handleVolumeUp()
            },

            onVolumeDown = {
                handleVolumeDown()
            }
        )

        setContent {

            VisualAssistantTheme {

                when (currentAppState) {

                    AppState.ONBOARDING -> {

                        OnboardingScreen(
                            onContinue = {
                                startHapticTraining()
                            }
                        )
                    }

                    AppState.HAPTIC_TRAINING -> {

                        HapticTrainingScreen(
                            vibrationManager = vibrationManager,

                            onComplete = {
                                completeHapticTraining()
                            }
                        )
                    }

                    AppState.MODE_SELECTION -> {

                        ModeSelectionPlaceholder()
                    }

                    AppState.READ_SCANNING -> {

                        CameraScreen(

                            onCapture = { bitmap ->
                                handleCapture(bitmap)
                            },

                            onSessionEnd = {
                                handleSessionEnd()
                            }
                        )
                    }

                    AppState.CURRENCY_SCANNING -> {

                        CameraScreen(

                            onCapture = { bitmap ->
                                handleCapture(bitmap)
                            },

                            onSessionEnd = {
                                handleSessionEnd()
                            }
                        )
                    }

                    else -> {

                        ModeSelectionPlaceholder()
                    }
                }
            }
        }


        if (!hasCompletedOnboarding) {

            vibrationManager.short()

            speechManager.speak(
                "Welcome to Visual Assistant. " +
                        "This application helps you interact with the world using voice, " +
                        "vibrations and your camera. " +
                        "Press Volume Up to begin vibration training."
            )

        } else {

            vibrationManager.tripleShort()

            speechManager.speak(
                "Welcome back to Visual Assistant. " +
                        "Press Volume Up for Read mode. " +
                        "Press Volume Down for Currency mode."
            )
        }
    }


    // ---------------------------------------------------------
    // APP STATE
    // ---------------------------------------------------------

    private fun setAppState(state: AppState) {

        interactionManager.setState(state)
        currentAppState = state
    }


    // ---------------------------------------------------------
    // HAPTIC TRAINING
    // ---------------------------------------------------------

    private fun startHapticTraining() {

        hapticTrainingManager.reset()

        setAppState(AppState.HAPTIC_TRAINING)

        val signal =
            hapticTrainingManager.currentSignal

        speakSignalInstruction(signal)

        playCurrentHapticSignal()
    }


    private fun completeHapticTraining() {

        onboardingPreferences.markOnboardingComplete()

        setAppState(AppState.MODE_SELECTION)

        vibrationManager.tripleShort()

        speechManager.speak(
            "Vibration training is complete. " +
                    "Press Volume Up for Read mode. " +
                    "Press Volume Down for Currency mode."
        )
    }


    private fun repeatCurrentHapticSignal() {

        val signal =
            hapticTrainingManager.currentSignal

        speakSignalInstruction(signal)

        playCurrentHapticSignal()
    }


    private fun nextHapticSignal() {

        if (hapticTrainingManager.next()) {

            val signal =
                hapticTrainingManager.currentSignal

            speakSignalInstruction(signal)

            playCurrentHapticSignal()

        } else {

            completeHapticTraining()
        }
    }


    private fun speakSignalInstruction(
        signal: HapticSignal
    ) {

        speechManager.speak(
            "${signal.name}. " +
                    "${signal.meaning} " +
                    "Feel this vibration carefully. " +
                    "Press Volume Up to repeat it, " +
                    "or Volume Down for the next signal."
        )
    }


    private fun playCurrentHapticSignal() {

        when (hapticTrainingManager.currentIndex) {

            0 -> vibrationManager.short()

            1 -> vibrationManager.doubleShort()

            2 -> vibrationManager.long()

            3 -> vibrationManager.tripleShort()

            4 -> vibrationManager.processing()

            5 -> vibrationManager.error()
        }
    }


    // ---------------------------------------------------------
    // VOLUME BUTTONS
    // ---------------------------------------------------------

    private fun handleVolumeUp() {

        when (currentAppState) {

            AppState.ONBOARDING -> {
                startHapticTraining()
            }

            AppState.HAPTIC_TRAINING -> {
                repeatCurrentHapticSignal()
            }

            else -> {

                val action =
                    interactionManager.handleVolumeUp()

                handleAction(action)
            }
        }
    }


    private fun handleVolumeDown() {

        when (currentAppState) {

            AppState.ONBOARDING -> {

                speechManager.speak(
                    "Press Volume Up to begin vibration training."
                )
            }

            AppState.HAPTIC_TRAINING -> {

                nextHapticSignal()
            }

            else -> {

                val action =
                    interactionManager.handleVolumeDown()

                handleAction(action)
            }
        }
    }


    // ---------------------------------------------------------
    // CAPTURE
    // ---------------------------------------------------------

    private fun handleCapture(bitmap: Bitmap) {

        when (currentAppState) {

            AppState.READ_SCANNING -> {

                cancelScheduledListening()

                speechRecognitionManager.stopListening()

                vibrationManager.doubleShort()

                speechManager.speak(
                    "Reading the captured text."
                )

                offlineTextRecognitionManager.recognizeText(
                    bitmap = bitmap,

                    onSuccess = { detectedText ->

                        if (detectedText.isBlank()) {

                            vibrationManager.error()

                            speechManager.speak(
                                "I could not detect any text. " +
                                "Please point the camera at the text and try again."
                            )

                        } else {

                            speechManager.speak(
                                detectedText
                            )
                        }
                    },

                    onFailure = {

                        vibrationManager.error()

                        speechManager.speak(
                            "I could not read the text. " +
                            "Please try capturing the image again."
                        )
                    }
                )
            }


            AppState.CURRENCY_SCANNING -> {

                /*
                * Currency mode NEVER starts speech recognition.
                */

                cancelScheduledListening()

                speechRecognitionManager.stopListening()

                vibrationManager.doubleShort()

                try {

                    val result = currencyClassifier.classify(bitmap)

                    val denomination = result.denomination
                    val confidence = result.confidence

                    if (confidence >= 0.60f) {

                        vibrationManager.doubleShort()

                        speechManager.speak(
                            "₹$denomination"
                        )

                    } else {

                        vibrationManager.error()

                        speechManager.speak(
                            "I am not confident about the currency. " +
                                    "Please try again with the note clearly visible."
                        )
                    }

                } catch (e: Exception) {

                    vibrationManager.error()

                    speechManager.speak(
                        "I could not recognize the currency. " +
                                "Please try again."
                    )
                }
            }


            else -> {
                // Ignore capture in other states.
            }
        }
    }


    // ---------------------------------------------------------
    // QUESTION LISTENING
    // ---------------------------------------------------------

    private fun scheduleQuestionListening(
        delayMillis: Long
    ) {

        if (currentAppState != AppState.READ_SCANNING) {
            return
        }

        if (!readSessionManager.isActive()) {
            return
        }

        cancelScheduledListening()

        listeningScheduled = true

        mainHandler.postDelayed({

            listeningScheduled = false

            if (currentAppState == AppState.READ_SCANNING &&
                readSessionManager.isActive()
            ) {

                startQuestionListening()
            }

        }, delayMillis)
    }


    private fun startQuestionListening() {

        if (currentAppState != AppState.READ_SCANNING) {
            return
        }

        if (!readSessionManager.isActive()) {
            return
        }

        val hasPermission =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED


        if (!hasPermission) {

            microphonePermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )

            return
        }


        vibrationManager.processing()

        /*
         * Speak BEFORE starting recognition.
         *
         * There is intentionally no immediate
         * SpeechRecognizer start here.
         */

        speechManager.speak(
            "I'm listening."
        )

        /*
         * Give TTS a short moment to finish.
         */

        mainHandler.postDelayed({

            if (currentAppState == AppState.READ_SCANNING &&
                readSessionManager.isActive()
            ) {

                speechRecognitionManager.startListening()
            }

        }, 1200)
    }


    private fun handleUserQuestion(
        question: String
    ) {

        if (currentAppState != AppState.READ_SCANNING) {
            return
        }

        if (!readSessionManager.isActive()) {
            return
        }

        cancelScheduledListening()

        val answer =
            readSessionManager.askQuestion(question)

        vibrationManager.short()

        speechManager.speak(answer)

        speechManager.speak(
            "You can ask another question, " +
                    "or double tap to end the session."
        )

        /*
         * Wait for the answer + instruction to finish,
         * then listen for the next question.
         */

        scheduleQuestionListening(5500)
    }


    // ---------------------------------------------------------
    // SESSION END
    // ---------------------------------------------------------

    private fun handleSessionEnd() {

        /*
         * IMPORTANT:
         * Remember which mode we're currently in.
         */

        val stateBeforeEnding =
            currentAppState

        cancelScheduledListening()

        speechRecognitionManager.stopListening()

        /*
         * Only Read Mode owns a ReadSession.
         */

        if (stateBeforeEnding == AppState.READ_SCANNING) {

            if (readSessionManager.isActive()) {
                readSessionManager.endSession()
            }
        }

        vibrationManager.long()

        speechManager.speak(
            "Session ended. Scanning again."
        )

        /*
         * Return to the SAME mode.
         *
         * Read → Read
         * Currency → Currency
         */

        when (stateBeforeEnding) {

            AppState.READ_SCANNING -> {

                setAppState(
                    AppState.READ_SCANNING
                )
            }

            AppState.CURRENCY_SCANNING -> {

                setAppState(
                    AppState.CURRENCY_SCANNING
                )
            }

            else -> {

                setAppState(
                    AppState.MODE_SELECTION
                )
            }
        }
    }


    // ---------------------------------------------------------
    // LISTENING CLEANUP
    // ---------------------------------------------------------

    private fun cancelScheduledListening() {

        mainHandler.removeCallbacksAndMessages(null)

        listeningScheduled = false
    }


    // ---------------------------------------------------------
    // APP ACTIONS
    // ---------------------------------------------------------

    private fun handleAction(
        action: UserAction
    ) {

        when (action) {

            UserAction.RepeatInstruction -> {

                speechManager.speak(
                    "Please repeat the current instruction."
                )
            }


            UserAction.NextTrainingSignal -> {

                nextHapticSignal()
            }


            UserAction.StartReadMode -> {

                interactionManager.applyAction(action)

                setAppState(
                    AppState.READ_SCANNING
                )

                vibrationManager.tripleShort()

                speechManager.speak(
                    "Read mode selected. " +
                            "Point the camera toward text. " +
                            "Tap anywhere on the screen to capture."
                )
            }


            UserAction.StartCurrencyMode -> {

                interactionManager.applyAction(action)

                setAppState(
                    AppState.CURRENCY_SCANNING
                )

                vibrationManager.tripleShort()

                speechManager.speak(
                    "Currency mode selected. " +
                            "Point the camera toward a currency note. " +
                            "Tap anywhere on the screen to capture."
                )
            }


            UserAction.Capture -> {

                interactionManager.applyAction(action)

                vibrationManager.doubleShort()

                speechManager.speak(
                    "Capture started."
                )
            }


            UserAction.EndSession -> {

                /*
                 * CameraScreen normally handles this directly.
                 * Keep this branch safe if another component triggers it.
                 */

                handleSessionEnd()
            }


            UserAction.SwitchToRead -> {

                cancelScheduledListening()

                speechRecognitionManager.stopListening()

                if (readSessionManager.isActive()) {
                    readSessionManager.endSession()
                }

                interactionManager.applyAction(action)

                setAppState(
                    AppState.READ_SCANNING
                )

                vibrationManager.tripleShort()

                speechManager.speak(
                    "Switched to Read mode. " +
                            "Point the camera toward text. " +
                            "Tap anywhere on the screen to capture."
                )
            }


            UserAction.SwitchToCurrency -> {

                cancelScheduledListening()

                speechRecognitionManager.stopListening()

                if (readSessionManager.isActive()) {
                    readSessionManager.endSession()
                }

                interactionManager.applyAction(action)

                setAppState(
                    AppState.CURRENCY_SCANNING
                )

                vibrationManager.tripleShort()

                speechManager.speak(
                    "Switched to Currency mode. " +
                            "Point the camera toward a currency note. " +
                            "Tap anywhere on the screen to capture."
                )
            }
        }
    }


    // ---------------------------------------------------------
    // PHYSICAL VOLUME BUTTON INTERCEPTION
    // ---------------------------------------------------------

    override fun dispatchKeyEvent(
        event: KeyEvent
    ): Boolean {

        if (volumeButtonHandler.handle(event)) {
            return true
        }

        return super.dispatchKeyEvent(event)
    }


    // ---------------------------------------------------------
    // CLEANUP
    // ---------------------------------------------------------

    override fun onDestroy() {

        cancelScheduledListening()

        speechRecognitionManager.release()

        offlineTextRecognitionManager.release()
        
        speechManager.release()

        vibrationManager.stop()

        super.onDestroy()
    }
}


// ---------------------------------------------------------
// MODE SELECTION PLACEHOLDER
// ---------------------------------------------------------

@Composable
private fun ModeSelectionPlaceholder() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "Mode Selection"
        )

        Text(
            text =
                "Volume Up: Read Mode | " +
                        "Volume Down: Currency Mode"
        )
    }
}