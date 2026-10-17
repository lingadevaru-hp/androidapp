package lingadevaru.thoshan.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lingadevaru.thoshan.model.ChatThread
import lingadevaru.thoshan.ui.theme.PortfolioRadiusLarge
import lingadevaru.thoshan.ui.theme.PortfolioTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchChatsSheet(
    threads: List<ChatThread>,
    onSelectThread: (ChatThread) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }

    val filteredThreads = remember(query, threads) {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) {
            threads
        } else {
            threads.filter { thread ->
                thread.title.lowercase().contains(trimmed) ||
                thread.messages.any { it.text.lowercase().contains(trimmed) }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = PortfolioRadiusLarge, topEnd = PortfolioRadiusLarge),
        containerColor = PortfolioTheme.colors.surface,
        dragHandle = {
            // Grabber handle
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
            // Title in bold sans
            Text(
                text = "Search chats",
                style = PortfolioTheme.typography.titleLarge.copy(
                    color = PortfolioTheme.colors.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )

            // Subtitle dim
            Text(
                text = "Find a past conversation by its title, or by something said in it.",
                style = PortfolioTheme.typography.bodySmall.copy(
                    color = PortfolioTheme.colors.textDim,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )

            // Search input with magnifier icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PortfolioTheme.colors.surface2)
                    .border(1.dp, PortfolioTheme.colors.border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = PortfolioTheme.colors.textDim,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search by title or message…",
                            style = PortfolioTheme.typography.bodyMedium.copy(
                                color = PortfolioTheme.colors.textFaint
                            )
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_chats_input"),
                        textStyle = PortfolioTheme.typography.bodyMedium.copy(
                            color = PortfolioTheme.colors.text
                        ),
                        cursorBrush = SolidColor(PortfolioTheme.colors.accent),
                        singleLine = true
                    )
                }

                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { query = "" },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = PortfolioTheme.colors.textDim,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Below: list of matching chats
            if (filteredThreads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (query.isEmpty()) "No recent chats found." else "No matches found for \"$query\".",
                        style = PortfolioTheme.typography.bodyMedium.copy(
                            color = PortfolioTheme.colors.textFaint
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredThreads, key = { it.id }) { thread ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PortfolioTheme.colors.surface2.copy(alpha = 0.6f))
                                .border(1.dp, PortfolioTheme.colors.border, RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectThread(thread)
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                        val defaultTextColor = PortfolioTheme.colors.text
                        val accentColor = PortfolioTheme.colors.accent
                        val highlightedTitle = remember(thread.title, query, defaultTextColor, accentColor) {
                            buildHighlightedTitle(
                                title = thread.title,
                                query = query.trim(),
                                defaultColor = defaultTextColor,
                                highlightColor = accentColor
                            )
                        }

                            Text(
                                text = highlightedTitle,
                                style = PortfolioTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = formatRelativeTime(thread.updatedAt),
                                style = PortfolioTheme.typography.labelSmall.copy(
                                    color = PortfolioTheme.colors.textFaint,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildHighlightedTitle(
    title: String,
    query: String,
    defaultColor: androidx.compose.ui.graphics.Color,
    highlightColor: androidx.compose.ui.graphics.Color
): androidx.compose.ui.text.AnnotatedString {
    if (query.isEmpty()) {
        return buildAnnotatedString {
            append(title)
        }
    }

    val lowerTitle = title.lowercase()
    val lowerQuery = query.lowercase()
    var startIndex = 0

    return buildAnnotatedString {
        while (startIndex < title.length) {
            val matchIndex = lowerTitle.indexOf(lowerQuery, startIndex)
            if (matchIndex == -1) {
                append(title.substring(startIndex))
                break
            }

            if (matchIndex > startIndex) {
                append(title.substring(startIndex, matchIndex))
            }

            val endIndex = matchIndex + lowerQuery.length
            pushStyle(
                SpanStyle(
                    color = highlightColor,
                    fontWeight = FontWeight.Bold
                )
            )
            append(title.substring(matchIndex, endIndex))
            pop()

            startIndex = endIndex
        }
    }
}
