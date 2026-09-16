package com.visualassistant.app.accessibility

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeechManager(
    context: Context
) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isReady = false

    private var pendingSpeech: String? = null

    init {
        textToSpeech = TextToSpeech(
            context.applicationContext,
            this
        )
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result = textToSpeech?.setLanguage(Locale.US)

            isReady =
                result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED

            if (isReady) {
                pendingSpeech?.let { message ->
                    speakNow(message)
                    pendingSpeech = null
                }
            }
        }
    }

    fun speak(text: String) {

        if (!isReady) {
            pendingSpeech = text
            return
        }

        speakNow(text)
    }

    private fun speakNow(text: String) {

        textToSpeech?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "visual_assistant_message"
        )
    }

    fun stop() {
        textToSpeech?.stop()
        pendingSpeech = null
    }

    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()

        textToSpeech = null
        pendingSpeech = null
        isReady = false
    }
}