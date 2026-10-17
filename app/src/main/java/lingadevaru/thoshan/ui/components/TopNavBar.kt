package lingadevaru.thoshan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lingadevaru.thoshan.ui.theme.PortfolioTheme

@Composable
fun TopNavBar(
    title: String,
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        // Soft ambient glow behind top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PortfolioTheme.colors.backgroundSoft.copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Hamburger icon — symmetric 48dp target
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("history_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open history drawer",
                        tint = PortfolioTheme.colors.textDim,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Centre-left: chat title, single line, ellipsised
                Text(
                    text = title.ifBlank { "New Chat" },
                    style = PortfolioTheme.typography.titleMedium.copy(
                        color = PortfolioTheme.colors.text,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right: symmetric 48dp call target + 48dp grouped pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                // Call mode shortcut button (immersive voice screen) — 48dp circle
                IconButton(
                    onClick = onCallClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PortfolioTheme.colors.surface2)
                        .border(1.dp, PortfolioTheme.colors.border, CircleShape)
                        .testTag("call_mode_shortcut_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call mode",
                        tint = PortfolioTheme.colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // The single grouped pill containing new chat + 3-dot menu — 48dp height
                Box {
                    Row(
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(PortfolioTheme.colors.surface2)
                            .border(1.dp, PortfolioTheme.colors.border, RoundedCornerShape(999.dp))
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "New chat" button within pill — symmetric 40dp target
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { onNewChatClick() }
                                .testTag("new_chat_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New chat",
                                tint = PortfolioTheme.colors.textDim,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Subtle internal divider inside pill
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(PortfolioTheme.colors.border)
                        )

                        // 3-dot overflow menu button within pill — symmetric 40dp target
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { menuExpanded = true }
                                .testTag("top_menu_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu options",
                                tint = PortfolioTheme.colors.textDim,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Dropdown menu
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .background(PortfolioTheme.colors.surface)
                            .border(1.dp, PortfolioTheme.colors.borderStrong, RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Search chats", color = PortfolioTheme.colors.text) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = PortfolioTheme.colors.textDim
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onSearchClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Call mode", color = PortfolioTheme.colors.text) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Call,
                                    contentDescription = null,
                                    tint = PortfolioTheme.colors.accent
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onCallClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings", color = PortfolioTheme.colors.text) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = PortfolioTheme.colors.textDim
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onSettingsClick()
                            }
                        )
                    }
                }
            }
        }
    }
}
