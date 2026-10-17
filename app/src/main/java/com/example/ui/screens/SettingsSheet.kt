package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IndianLanguages
import com.example.model.ThemeOption
import com.example.model.UserSettings
import com.example.ui.theme.PortfolioRadiusLarge
import com.example.ui.theme.PortfolioTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: UserSettings,
    onThemeChange: (ThemeOption) -> Unit,
    onLanguageChange: (String) -> Unit,
    onClearAllChats: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showClearDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = PortfolioRadiusLarge, topEnd = PortfolioRadiusLarge),
        containerColor = PortfolioTheme.colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(PortfolioTheme.colors.borderStrong)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Settings",
                style = PortfolioTheme.typography.titleLarge.copy(
                    color = PortfolioTheme.colors.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )

            Text(
                text = "Preferences and personalization for Thoshan's Assistant.",
                style = PortfolioTheme.typography.bodySmall.copy(
                    color = PortfolioTheme.colors.textDim
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Row 1: Theme (Dark / Light / System)
            Text(
                text = "Theme",
                style = PortfolioTheme.typography.labelSmall.copy(
                    color = PortfolioTheme.colors.textFaint,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOption.entries.forEach { option ->
                    val isSelected = settings.theme == option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) PortfolioTheme.colors.accent.copy(alpha = 0.18f)
                                else PortfolioTheme.colors.surface2
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PortfolioTheme.colors.accent else PortfolioTheme.colors.border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onThemeChange(option) }
                            .testTag("theme_option_${option.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.displayName,
                            style = PortfolioTheme.typography.bodyMedium.copy(
                                color = if (isSelected) PortfolioTheme.colors.accent else PortfolioTheme.colors.text,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Row 2: Reply language (Indian languages picker)
            Text(
                text = "Reply language",
                style = PortfolioTheme.typography.labelSmall.copy(
                    color = PortfolioTheme.colors.textFaint,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(IndianLanguages) { language ->
                    val isSelected = settings.replyLanguage.equals(language, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (isSelected) PortfolioTheme.colors.accent
                                else PortfolioTheme.colors.surface2
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PortfolioTheme.colors.accent else PortfolioTheme.colors.border,
                                shape = RoundedCornerShape(999.dp)
                            )
                            .clickable { onLanguageChange(language) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("lang_chip_$language"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = language,
                            style = PortfolioTheme.typography.bodySmall.copy(
                                color = if (isSelected) PortfolioTheme.colors.accentInk else PortfolioTheme.colors.text,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Row 3: Clear all chats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PortfolioTheme.colors.surface2)
                    .border(1.dp, PortfolioTheme.colors.danger.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .clickable { showClearDialog = true }
                    .padding(horizontal = 16.dp)
                    .testTag("clear_all_chats_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear all chats",
                        tint = PortfolioTheme.colors.danger,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Clear all chats",
                        style = PortfolioTheme.typography.bodyMedium.copy(
                            color = PortfolioTheme.colors.danger,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Clear all conversations?",
                    style = PortfolioTheme.typography.titleMedium.copy(color = PortfolioTheme.colors.text)
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all past chat threads. This action cannot be undone.",
                    style = PortfolioTheme.typography.bodyMedium.copy(color = PortfolioTheme.colors.textDim)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllChats()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear all", color = PortfolioTheme.colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = PortfolioTheme.colors.textDim)
                }
            },
            containerColor = PortfolioTheme.colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
