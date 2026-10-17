package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CallActionBus
import com.example.data.CallNotificationManager
import com.example.model.SpeakerAudioState
import com.example.model.ThemeOption
import com.example.ui.screens.CallModeScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.theme.PortfolioTheme
import com.example.ui.theme.ThoshanAssistantTheme
import com.example.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* best-effort: notification simply won't show if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val viewModel: ChatViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val isDark = when (uiState.settings.theme) {
                ThemeOption.DARK -> true
                ThemeOption.LIGHT -> false
                ThemeOption.SYSTEM -> isSystemInDarkTheme()
            }

            // Forward dropdown notification actions to the ViewModel
            LaunchedEffect(viewModel) {
                CallActionBus.onToggleMute = viewModel::toggleCallMute
                CallActionBus.onToggleSpeaker = viewModel::toggleCallSpeaker
                CallActionBus.onEndCall = viewModel::endCallMode
            }

            // Dialer-style ongoing notification while call mode or TTS speech is active
            LaunchedEffect(
                uiState.isCallModeActive,
                uiState.speakerAudioState,
                uiState.isCallMuted,
                uiState.isCallSpeakerOn
            ) {
                val speaking = uiState.speakerAudioState == SpeakerAudioState.SPEAKING ||
                    uiState.speakerAudioState == SpeakerAudioState.LOADING
                if (uiState.isCallModeActive || speaking) {
                    CallNotificationManager.show(
                        context = this@MainActivity,
                        title = "Thoshan's Assistant",
                        text = CallNotificationManager.stateText(
                            uiState.isCallModeActive,
                            uiState.speakerAudioState
                        ),
                        isMuted = uiState.isCallMuted,
                        isSpeakerOn = uiState.isCallSpeakerOn,
                        elapsedSeconds = uiState.callElapsedSeconds
                    )
                } else {
                    CallNotificationManager.hide(this@MainActivity)
                }
            }

            ThoshanAssistantTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PortfolioTheme.colors.background
                ) {
                    if (uiState.isCallModeActive) {
                        BackHandler {
                            viewModel.endCallMode()
                        }
                        CallModeScreen(
                            elapsedSeconds = uiState.callElapsedSeconds,
                            callState = uiState.callState,
                            userTranscript = uiState.callUserTranscript,
                            assistantTranscript = uiState.callAssistantTranscript,
                            isMuted = uiState.isCallMuted,
                            isSpeakerOn = uiState.isCallSpeakerOn,
                            onToggleMute = viewModel::toggleCallMute,
                            onToggleSpeaker = viewModel::toggleCallSpeaker,
                            onEndCall = viewModel::endCallMode
                        )
                    } else {
                        ChatScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                    }
                }
            }
        }
    }
}
