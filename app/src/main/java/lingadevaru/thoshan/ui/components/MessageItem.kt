package lingadevaru.thoshan.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lingadevaru.thoshan.model.Message
import lingadevaru.thoshan.model.MessageSender
import lingadevaru.thoshan.model.Reaction
import lingadevaru.thoshan.model.SpeakerAudioState
import lingadevaru.thoshan.ui.theme.PortfolioShapeDefault
import lingadevaru.thoshan.ui.theme.PortfolioTheme

@Composable
fun MessageItem(
    message: Message,
    audioState: SpeakerAudioState,
    onSpeakerClick: () -> Unit,
    onReactionClick: (Reaction) -> Unit,
    modifier: Modifier = Modifier
) {
    when (message.sender) {
        MessageSender.USER -> {
            // User message: right-aligned bubble, surface background, rounded, max ~78% width, selectable
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .clip(PortfolioShapeDefault)
                        .background(PortfolioTheme.colors.surface)
                        .border(1.dp, PortfolioTheme.colors.border, PortfolioShapeDefault)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (message.hasAttachment) {
                            UserAttachmentView(message = message)
                        }
                        if (message.text.isNotBlank()) {
                            SelectionContainer {
                                Text(
                                    text = message.text,
                                    style = PortfolioTheme.typography.bodyLarge.copy(
                                        color = PortfolioTheme.colors.text
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        MessageSender.ASSISTANT -> {
            // Assistant message: left-aligned, NO bubble — plain text on background. Asymmetry is deliberate.
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Above each assistant message: dim Thoughts › row
                if (message.thoughts.isNotEmpty()) {
                    ThoughtsRow(thoughts = message.thoughts)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Plain text markdown content directly on the background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    SelectionContainer {
                        MarkdownContent(content = message.text)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Below each assistant message: action row of 4 icon buttons
                // speaker, thumbs-up, thumbs-down, copy
                AssistantActionRow(
                    messageText = message.text,
                    reaction = message.reaction,
                    audioState = audioState,
                    onSpeakerClick = onSpeakerClick,
                    onReactionClick = onReactionClick
                )
            }
        }
    }
}

@Composable
private fun UserAttachmentView(
    message: Message,
    modifier: Modifier = Modifier
) {
    val uri = message.attachmentUri ?: return
    if (message.isImageAttachment) {
        coil.compose.AsyncImage(
            model = uri,
            contentDescription = message.attachmentName ?: "Attached image",
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
        )
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(PortfolioTheme.colors.surface2)
                .border(1.dp, PortfolioTheme.colors.border, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Attached file",
                tint = PortfolioTheme.colors.accent,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = message.attachmentName ?: "File attachment",
                style = PortfolioTheme.typography.bodySmall.copy(color = PortfolioTheme.colors.text),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ThoughtsRow(
    thoughts: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Thoughts",
                style = PortfolioTheme.typography.labelMedium.copy(
                    color = PortfolioTheme.colors.textFaint,
                    fontSize = 11.5.sp
                )
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = if (expanded) "Collapse thoughts" else "Expand thoughts",
                tint = PortfolioTheme.colors.textFaint,
                modifier = Modifier.size(14.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                    .border(
                        width = 1.dp,
                        color = PortfolioTheme.colors.border,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                        PortfolioTheme.colors.surface.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                thoughts.forEach { thought ->
                    Text(
                        text = "• $thought",
                        style = PortfolioTheme.typography.bodySmall.copy(
                            color = PortfolioTheme.colors.textDim,
                            fontStyle = FontStyle.Italic,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AssistantActionRow(
    messageText: String,
    reaction: Reaction?,
    audioState: SpeakerAudioState,
    onSpeakerClick: () -> Unit,
    onReactionClick: (Reaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Speaker button with state machine:
        // idle -> speaker icon, dim
        // loading -> spinning lotus icon, accent colour
        // speaking -> pause icon, accent, with slow pulsing ring
        // paused -> play icon, accent
        SpeakerActionButton(
            audioState = audioState,
            onClick = onSpeakerClick
        )

        // 2. Thumbs-up toggle
        ActionButton(
            icon = if (reaction == Reaction.THUMBS_UP) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
            contentDescription = "Thumbs up",
            isActive = reaction == Reaction.THUMBS_UP,
            onClick = { onReactionClick(Reaction.THUMBS_UP) },
            testTag = "thumb_up_button"
        )

        // 3. Thumbs-down toggle
        ActionButton(
            icon = if (reaction == Reaction.THUMBS_DOWN) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
            contentDescription = "Thumbs down",
            isActive = reaction == Reaction.THUMBS_DOWN,
            onClick = { onReactionClick(Reaction.THUMBS_DOWN) },
            testTag = "thumb_down_button"
        )

        // 4. Copy full message text (quick copy-all)
        ActionButton(
            icon = Icons.Default.ContentCopy,
            contentDescription = "Copy full message",
            isActive = false,
            onClick = {
                clipboardManager.setText(AnnotatedString(messageText))
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            testTag = "copy_message_button"
        )
    }
}

@Composable
private fun SpeakerActionButton(
    audioState: SpeakerAudioState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .size(34.dp)
            .clickable(onClick = onClick)
            .testTag("speaker_action_button"),
        contentAlignment = Alignment.Center
    ) {
        // Slow pulsing ring around button when speaking
        if (audioState == SpeakerAudioState.SPEAKING) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .border(1.5.dp, PortfolioTheme.colors.accent.copy(alpha = pulseAlpha), CircleShape)
            )
        }

        when (audioState) {
            SpeakerAudioState.IDLE -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Play speech",
                    tint = PortfolioTheme.colors.textFaint,
                    modifier = Modifier.size(17.dp)
                )
            }
            SpeakerAudioState.LOADING -> {
                // Spinning lotus icon in accent colour
                LotusMark(
                    size = 18.dp,
                    petalColor = PortfolioTheme.colors.accent,
                    showGlow = false,
                    isRotating = true
                )
            }
            SpeakerAudioState.SPEAKING -> {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause speech",
                    tint = PortfolioTheme.colors.accent,
                    modifier = Modifier.size(17.dp)
                )
            }
            SpeakerAudioState.PAUSED -> {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Resume speech",
                    tint = PortfolioTheme.colors.accent,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) PortfolioTheme.colors.accent else PortfolioTheme.colors.textFaint,
            modifier = Modifier.size(16.dp)
        )
    }
}
