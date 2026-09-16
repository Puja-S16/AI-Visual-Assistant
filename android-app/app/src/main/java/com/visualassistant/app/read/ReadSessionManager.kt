package com.visualassistant.app.read

import android.graphics.Bitmap

class ReadSessionManager {

    private var capturedImage: Bitmap? = null

    private var sessionActive = false

    private val conversation = mutableListOf<ConversationTurn>()

    fun startSession(image: Bitmap): String {

        capturedImage = image
        sessionActive = true
        conversation.clear()

        val summary =
            "This image contains readable information. " +
            "I have analyzed the captured content. " +
            "You can now ask questions about it."

        conversation.add(
            ConversationTurn(
                role = "assistant",
                message = summary
            )
        )

        return summary
    }

    fun askQuestion(question: String): String {

        if (!sessionActive) {
            return "There is no active reading session."
        }

        conversation.add(
            ConversationTurn(
                role = "user",
                message = question
            )
        )

        val answer =
            "I will answer your question about the captured content. " +
            "The AI vision service will provide the actual answer when the API is connected."

        conversation.add(
            ConversationTurn(
                role = "assistant",
                message = answer
            )
        )

        return answer
    }

    fun endSession() {

        capturedImage?.recycle()
        capturedImage = null

        conversation.clear()

        sessionActive = false
    }

    fun isActive(): Boolean {
        return sessionActive
    }

    fun getConversation(): List<ConversationTurn> {
        return conversation.toList()
    }
}

data class ConversationTurn(
    val role: String,
    val message: String
)