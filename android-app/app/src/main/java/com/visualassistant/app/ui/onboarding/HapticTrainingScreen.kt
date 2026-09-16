package com.visualassistant.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.visualassistant.app.accessibility.VibrationManager

@Composable
fun HapticTrainingScreen(
    vibrationManager: VibrationManager,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Learn Vibration Signals",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Tap Play next to each signal to feel its vibration. " +
                    "These signals will be used throughout the app.",
            style = MaterialTheme.typography.bodyLarge
        )

        HapticSignal(
            name = "One Short Vibration",
            meaning = "Something is detected / the app is ready.",
            onPlay = { vibrationManager.short() }
        )

        HapticSignal(
            name = "Two Short Vibrations",
            meaning = "Action required / capture now.",
            onPlay = { vibrationManager.doubleShort() }
        )

        HapticSignal(
            name = "One Long Vibration",
            meaning = "The current session has ended.",
            onPlay = { vibrationManager.long() }
        )

        HapticSignal(
            name = "Three Short Vibrations",
            meaning = "The app mode has changed.",
            onPlay = { vibrationManager.tripleShort() }
        )

        HapticSignal(
            name = "Two Long Vibrations",
            meaning = "The app is processing your request.",
            onPlay = { vibrationManager.processing() }
        )

        HapticSignal(
            name = "Long + Short",
            meaning = "The app could not recognize something.",
            onPlay = { vibrationManager.error() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I've Learned These Signals")
        }
    }
}

@Composable
private fun HapticSignal(
    name: String,
    meaning: String,
    onPlay: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = meaning,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onPlay,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Play Vibration")
        }
    }
}