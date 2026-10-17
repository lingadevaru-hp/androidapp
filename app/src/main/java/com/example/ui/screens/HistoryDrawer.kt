package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatThread
import com.example.ui.components.LotusMark
import com.example.ui.theme.PortfolioTheme

@Composable
fun HistoryDrawerContent(
    threads: List<ChatThread>,
    activeThreadId: String?,
    onSelectThread: (ChatThread) -> Unit,
    onNewChatClick: () -> Unit,
    onSearchClick: () -> Unit,
    onDeleteThread: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    onClearAllChats: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var threadToDelete by remember { mutableStateOf<ChatThread?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .width(288.dp)
            .fillMaxHeight()
            .background(PortfolioTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Drawer Header: App logo & Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LotusMark(
                size = 28.dp,
                petalColor = PortfolioTheme.colors.accent,
                showGlow = false
            )
            Column {
                Text(
                    text = "Thoshan",
                    style = PortfolioTheme.typography.titleMedium.copy(
                        color = PortfolioTheme.colors.text,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "Portfolio Assistant",
                    style = PortfolioTheme.typography.labelSmall.copy(
                        color = PortfolioTheme.colors.textDim
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // "New chat" row — symmetric 48dp, 12dp radius
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PortfolioTheme.colors.surface)
                .border(1.dp, PortfolioTheme.colors.border, RoundedCornerShape(12.dp))
                .clickable { onNewChatClick() }
                .padding(horizontal = 12.dp)
                .testTag("drawer_new_chat_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New chat",
                tint = PortfolioTheme.colors.accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "New chat",
                style = PortfolioTheme.typography.bodyMedium.copy(
                    color = PortfolioTheme.colors.text,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search field at top of list (opens Search sheet when tapped) — symmetric 48dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PortfolioTheme.colors.surface2)
                .border(1.dp, PortfolioTheme.colors.border, RoundedCornerShape(12.dp))
                .clickable { onSearchClick() }
                .padding(horizontal = 12.dp)
                .testTag("drawer_search_field"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = PortfolioTheme.colors.textFaint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Search chats…",
                style = PortfolioTheme.typography.bodySmall.copy(
                    color = PortfolioTheme.colors.textFaint
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Conversations",
            style = PortfolioTheme.typography.labelSmall.copy(
                color = PortfolioTheme.colors.textFaint,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        // Chat list rows: title (single line, ellipsised) + relative time
        // Active row highlighted. Delete affordance with confirmation.
        // Symmetric 48dp rows, 10dp radius — same tokens as New chat / Search rows.
        if (threads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved conversations yet",
                    style = PortfolioTheme.typography.bodySmall.copy(
                        color = PortfolioTheme.colors.textFaint
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(threads, key = { it.id }) { thread ->
                    val isActive = thread.id == activeThreadId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isActive) PortfolioTheme.colors.surface2 else PortfolioTheme.colors.surface.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isActive) PortfolioTheme.colors.accent.copy(alpha = 0.35f) else PortfolioTheme.colors.border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectThread(thread) }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = thread.title.ifBlank { "Conversation" },
                                style = PortfolioTheme.typography.bodyMedium.copy(
                                    color = if (isActive) PortfolioTheme.colors.accent else PortfolioTheme.colors.text,
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatRelativeTime(thread.updatedAt),
                                style = PortfolioTheme.typography.labelSmall.copy(
                                    color = PortfolioTheme.colors.textFaint,
                                    fontSize = 10.5.sp
                                )
                            )
                        }

                        // Delete affordance icon button
                        IconButton(
                            onClick = { threadToDelete = thread },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete chat",
                                tint = PortfolioTheme.colors.textFaint,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Footer: Settings + Clear all chats — same 48dp / 12dp-radius row tokens
        DrawerFooterRow(
            icon = Icons.Default.Settings,
            label = "Settings",
            testTag = "drawer_settings_button",
            onClick = onSettingsClick
        )
        Spacer(modifier = Modifier.height(4.dp))
        DrawerFooterRow(
            icon = Icons.Default.DeleteSweep,
            label = "Clear all chats",
            isDanger = true,
            testTag = "drawer_clear_all_button",
            onClick = { showClearAllConfirm = true }
        )
    }

    // Clear-all confirmation dialog
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = {
                Text(
                    text = "Clear all chats?",
                    style = PortfolioTheme.typography.titleMedium.copy(color = PortfolioTheme.colors.text)
                )
            },
            text = {
                Text(
                    text = "All conversations will be deleted. This cannot be undone.",
                    style = PortfolioTheme.typography.bodyMedium.copy(color = PortfolioTheme.colors.textDim)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllChats()
                        showClearAllConfirm = false
                    }
                ) {
                    Text("Clear all", color = PortfolioTheme.colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("Cancel", color = PortfolioTheme.colors.textDim)
                }
            },
            containerColor = PortfolioTheme.colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete confirmation dialog
    threadToDelete?.let { thread ->
        AlertDialog(
            onDismissRequest = { threadToDelete = null },
            title = {
                Text(
                    text = "Delete chat?",
                    style = PortfolioTheme.typography.titleMedium.copy(color = PortfolioTheme.colors.text)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${thread.title}\"? This cannot be undone.",
                    style = PortfolioTheme.typography.bodyMedium.copy(color = PortfolioTheme.colors.textDim)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteThread(thread.id)
                        threadToDelete = null
                    }
                ) {
                    Text("Delete", color = PortfolioTheme.colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { threadToDelete = null }) {
                    Text("Cancel", color = PortfolioTheme.colors.textDim)
                }
            },
            containerColor = PortfolioTheme.colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun DrawerFooterRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    testTag: String,
    modifier: Modifier = Modifier,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PortfolioTheme.colors.surface.copy(alpha = 0.5f))
            .border(1.dp, PortfolioTheme.colors.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isDanger) PortfolioTheme.colors.danger else PortfolioTheme.colors.textDim,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = PortfolioTheme.typography.bodyMedium.copy(
                color = if (isDanger) PortfolioTheme.colors.danger else PortfolioTheme.colors.text,
                fontWeight = FontWeight.Medium,
                fontSize = 13.5.sp
            )
        )
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val diffMillis = System.currentTimeMillis() - timestamp
    val seconds = (diffMillis / 1000).coerceAtLeast(0)
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        days == 1L -> "1 day ago"
        else -> "$days days ago"
    }
}
