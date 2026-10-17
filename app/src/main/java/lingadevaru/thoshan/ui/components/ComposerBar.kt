package lingadevaru.thoshan.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lingadevaru.thoshan.ui.theme.PortfolioTheme
import kotlinx.coroutines.delay

@Composable
fun ComposerBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onAttachClick: () -> Unit,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    isAssistantTyping: Boolean,
    pendingAttachmentUri: String? = null,
    pendingAttachmentName: String? = null,
    pendingAttachmentMime: String? = null,
    onRemoveAttachment: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Assistant typing indicator above the composer
        AnimatedVisibility(
            visible = isAssistantTyping,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Spinning mini lotus mark
                LotusMark(
                    size = 14.dp,
                    petalColor = PortfolioTheme.colors.accent,
                    showGlow = false,
                    isRotating = true
                )
                Text(
                    text = "Thoshan is thinking…",
                    style = PortfolioTheme.typography.labelSmall.copy(
                        color = PortfolioTheme.colors.textDim
                    )
                )
            }
        }

        if (isRecording) {
            // While recording: record bar with animated live level meter, timer reading 0:07/0:30, stop and cancel
            RecordBar(
                onStop = onStopRecording,
                onCancel = onCancelRecording
            )
        } else {
            // Pending attachment preview chip above the composer
            if (pendingAttachmentUri != null) {
                AttachmentPreviewChip(
                    uri = pendingAttachmentUri,
                    name = pendingAttachmentName,
                    mime = pendingAttachmentMime,
                    onRemove = onRemoveAttachment
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            // Normal Composer: symmetric 56dp pill, equal 44dp touch targets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(PortfolioTheme.colors.surface)
                    .border(1.dp, PortfolioTheme.colors.border, RoundedCornerShape(28.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: + button (attach) — symmetric 44dp target
                IconButton(
                    onClick = onAttachClick,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("attach_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Attach file or image",
                        tint = PortfolioTheme.colors.textDim,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Centre: multi-line text field that grows from 1 to ~5 lines
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Ask anything…",
                            style = PortfolioTheme.typography.bodyLarge.copy(
                                color = PortfolioTheme.colors.textFaint
                            )
                        )
                    }

                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("message_input_field"),
                        textStyle = PortfolioTheme.typography.bodyLarge.copy(
                            color = PortfolioTheme.colors.text
                        ),
                        cursorBrush = SolidColor(PortfolioTheme.colors.accent),
                        maxLines = 5,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (text.isNotBlank()) {
                                    onSend(text)
                                }
                            }
                        )
                    )
                }

                // Right: Send button when text or attachment is present, else Mic button
                if (text.isNotBlank() || pendingAttachmentUri != null) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PortfolioTheme.colors.accent)
                            .clickable { onSend(text) }
                            .testTag("send_message_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Send message",
                            tint = PortfolioTheme.colors.accentInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Mic button (available when composer is cleared)
                    IconButton(
                        onClick = onStartRecording,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("mic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record voice note",
                            tint = PortfolioTheme.colors.textDim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentPreviewChip(
    uri: String,
    name: String?,
    mime: String?,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isImage = mime?.startsWith("image") == true ||
        name?.substringAfterLast('.', "")?.lowercase() in setOf("jpg", "jpeg", "png", "webp", "gif")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PortfolioTheme.colors.surface2)
            .border(1.dp, PortfolioTheme.colors.border, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .testTag("attachment_preview"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isImage) {
            coil.compose.AsyncImage(
                model = uri,
                contentDescription = name ?: "Selected image",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PortfolioTheme.colors.surface3),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "File attachment",
                    tint = PortfolioTheme.colors.accent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name ?: "Attachment",
                style = PortfolioTheme.typography.bodyMedium.copy(color = PortfolioTheme.colors.text),
                maxLines = 1
            )
            Text(
                text = "Will be sent with your message",
                style = PortfolioTheme.typography.labelSmall.copy(color = PortfolioTheme.colors.textDim)
            )
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove attachment",
                tint = PortfolioTheme.colors.textDim,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun RecordBar(
    onStop: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 30-second cap timer — survives rotation
    var elapsedSeconds by androidx.compose.runtime.saveable.rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (elapsedSeconds < 30) {
            delay(1000)
            elapsedSeconds++
        }
        // Auto-stop at 30-second hard cap
        onStop()
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timerText = "%d:%02d / 0:30".format(minutes, seconds)

    // Clean chat-style recording card: symmetric 56dp height, 16dp radius
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PortfolioTheme.colors.surface2)
            .border(1.dp, PortfolioTheme.colors.accent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: pulsing red dot — symmetric 10dp
        PulsingDot()

        Spacer(modifier = Modifier.width(10.dp))

        // Centre: status + timer stacked, weights evenly
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = "Listening… speak now",
                style = PortfolioTheme.typography.labelMedium.copy(
                    color = PortfolioTheme.colors.text,
                    fontSize = 13.sp
                )
            )
            Text(
                text = timerText,
                style = PortfolioTheme.typography.labelSmall.copy(
                    color = PortfolioTheme.colors.textDim,
                    fontSize = 12.sp,
                    fontFeatureSettings = "tnum"
                )
            )
        }

        // Mid: uniform 7-bar level meter, symmetric spacing
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            AudioWaveBar(delayMillis = 0)
            AudioWaveBar(delayMillis = 120)
            AudioWaveBar(delayMillis = 240)
            AudioWaveBar(delayMillis = 360)
            AudioWaveBar(delayMillis = 240)
            AudioWaveBar(delayMillis = 120)
            AudioWaveBar(delayMillis = 0)
        }

        // Right: equal 40dp stop + cancel targets, 8dp gap
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PortfolioTheme.colors.accent)
                .clickable { onStop() }
                .testTag("record_stop_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop recording",
                tint = PortfolioTheme.colors.accentInk,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PortfolioTheme.colors.surface3)
                .clickable { onCancel() }
                .testTag("record_cancel_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel recording",
                tint = PortfolioTheme.colors.textDim,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PulsingDot(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "rec_dot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(PortfolioTheme.colors.danger.copy(alpha = alpha))
    )
}

@Composable
private fun AudioWaveBar(delayMillis: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    val heightScale by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, delayMillis = delayMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar_height"
    )

    Box(
        modifier = Modifier
            .width(3.5.dp)
            .height(20.dp * heightScale)
            .clip(RoundedCornerShape(999.dp))
            .background(PortfolioTheme.colors.accent)
    )
}
