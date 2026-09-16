package com.visualassistant.app.accessibility

import android.content.Context

class OnboardingPreferences(context: Context) {

    private val preferences = context.getSharedPreferences(
        "visual_assistant_preferences",
        Context.MODE_PRIVATE
    )

    fun hasCompletedOnboarding(): Boolean {
        return preferences.getBoolean(
            KEY_ONBOARDING_COMPLETED,
            false
        )
    }

    fun markOnboardingComplete() {
        preferences.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .apply()
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETED =
            "onboarding_completed"
    }
}