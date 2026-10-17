package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LotusMark
import com.example.ui.theme.DarkCallBackground
import com.example.ui.theme.PortfolioTheme
import com.example.viewmodel.CallState

@Composable
fun CallModeScreen(
    elapsedSeconds: Int,
    callState: CallState,
    userTranscript: String,
    assistantTranscript: String,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transcriptScrollState = rememberScrollState()

    // Orb animation: pulses gently while listening and scales slightly while speaking
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val listeningPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listening_scale"
    )

    val speakingPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_scale"
    )

    val orbScale = when (callState) {
        CallState.LISTENING -> listeningPulse
        CallState.SPEAKING -> speakingPulse
        CallState.THINKING -> 1.0f
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val formattedTimer = "%02d:%02d".format(minutes, seconds)

    val stateWord = when (callState) {
        CallState.LISTENING -> "Listening…"
        CallState.THINKING -> "Thinking…"
        CallState.SPEAKING -> "Speaking…"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCallBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Left: Thoshan's Assistant. Right: elapsed timer 00:13
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thoshan's Assistant",
                    style = PortfolioTheme.typography.titleMedium.copy(
                        color = PortfolioTheme.colors.textDim,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                )

                Text(
                    text = formattedTimer,
                    style = PortfolioTheme.typography.bodyMedium.copy(
                        color = PortfolioTheme.colors.textDim,
                        fontSize = 14.sp,
                        fontFeatureSettings = "tnum"
                    )
                )
            }

            Spacer(modifier = Modifier.weight(0.7f))

            // Central Orb: large circle with soft orange glow, containing lotus mark
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(orbScale),
                contentAlignment = Alignment.Center
            ) {
                // Ambient outer glow
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    PortfolioTheme.colors.accent.copy(alpha = 0.25f),
                                    PortfolioTheme.colors.accent.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Main circle orb
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .background(PortfolioTheme.colors.surface)
                        .border(
                            width = 1.5.dp,
                            color = PortfolioTheme.colors.accent.copy(alpha = 0.45f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    LotusMark(
                        size = 68.dp,
                        petalColor = PortfolioTheme.colors.accent,
                        showGlow = false,
                        isRotating = (callState == CallState.THINKING)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Under the orb: the current state word, dim
            Text(
                text = stateWord,
                style = PortfolioTheme.typography.bodyMedium.copy(
                    color = PortfolioTheme.colors.textDim,
                    letterSpacing = 0.5.sp,
                    fontSize = 14.5.sp
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Live transcript area: User line is dim; assistant line is bright and can scroll
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f)
                    .clip(CircleShape.copy(all = androidx.compose.foundation.shape.CornerSize(16.dp)))
                    .background(PortfolioTheme.colors.surface.copy(alpha = 0.4f))
                    .border(1.dp, PortfolioTheme.colors.border, CircleShape.copy(all = androidx.compose.foundation.shape.CornerSize(16.dp)))
                    .padding(16.dp)
                    .verticalScroll(transcriptScrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (userTranscript.isNotEmpty()) {
                    Text(
                        text = "You: $userTranscript",
                        style = PortfolioTheme.typography.bodySmall.copy(
                            color = PortfolioTheme.colors.textFaint,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp
                        )
                    )
                }

                if (assistantTranscript.isNotEmpty()) {
                    Text(
                        text = assistantTranscript,
                        style = PortfolioTheme.typography.bodyMedium.copy(
                            color = PortfolioTheme.colors.text,
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom row of three controls — symmetric: equal 88dp columns,
            // 24dp gaps, labels share one style.
            // MUTE (mic), END (large, danger red, centre), SPEAKER (volume)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Top
            ) {
                // 1. MUTE
                CallControlButton(
                    label = "MUTE",
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    isActive = isMuted,
                    activeColor = PortfolioTheme.colors.accent,
                    onClick = onToggleMute,
                    testTag = "call_mute_button",
                    modifier = Modifier.width(88.dp)
                )

                // 2. END (large, danger red, centre)
                Column(
                    modifier = Modifier.width(88.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PortfolioTheme.colors.danger)
                            .clickable { onEndCall() }
                            .testTag("call_end_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End call",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Text(
                        text = "END",
                        style = PortfolioTheme.typography.labelSmall.copy(
                            color = PortfolioTheme.colors.textDim,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // 3. SPEAKER
                CallControlButton(
                    label = "SPEAKER",
                    icon = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    isActive = isSpeakerOn,
                    activeColor = PortfolioTheme.colors.accent,
                    onClick = onToggleSpeaker,
                    testTag = "call_speaker_button",
                    modifier = Modifier.width(88.dp)
                )
            }
        }
    }
}

@Composable
private fun CallControlButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) activeColor.copy(alpha = 0.2f)
                    else PortfolioTheme.colors.surface2
                )
                .border(
                    width = 1.dp,
                    color = if (isActive) activeColor else PortfolioTheme.colors.border,
                    shape = CircleShape
                )
                .clickable { onClick() }
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else PortfolioTheme.colors.textDim,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = PortfolioTheme.typography.labelSmall.copy(
                color = PortfolioTheme.colors.textDim,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
