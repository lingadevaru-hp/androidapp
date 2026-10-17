package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Message
import com.example.model.SpeakerAudioState
import com.example.ui.components.AttachOption
import com.example.ui.components.AttachPickerSheet
import com.example.ui.components.ComposerBar
import com.example.ui.components.LotusMark
import com.example.ui.components.MessageItem
import com.example.ui.components.TopNavBar
import com.example.ui.theme.InstrumentSerif
import com.example.ui.theme.PortfolioTheme
import com.example.viewmodel.ChatUiState
import com.example.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    uiState: ChatUiState,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Runtime microphone permission launcher (required on Android 6+)
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startRecording()
        }
    }

    var showSearchSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showAttachSheet by remember { mutableStateOf(false) }

    // Attach launchers: gallery image, camera photo, generic file
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val mime = context.contentResolver.getType(uri) ?: "image/*"
            val name = queryDisplayName(context, uri) ?: "Image"
            viewModel.setPendingAttachment(uri.toString(), name, mime)
        }
    }
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val name = queryDisplayName(context, uri) ?: uri.lastPathSegment ?: "File"
            viewModel.setPendingAttachment(uri.toString(), name, mime)
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val saved = saveCameraBitmap(context, bitmap)
            if (saved != null) {
                viewModel.setPendingAttachment(saved, "Camera photo", "image/jpeg")
            }
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.currentThread.messages.size) {
        if (uiState.currentThread.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.currentThread.messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HistoryDrawerContent(
                threads = uiState.historyThreads,
                activeThreadId = uiState.currentThread.id,
                onSelectThread = { thread ->
                    viewModel.selectChat(thread)
                    scope.launch { drawerState.close() }
                },
                onNewChatClick = {
                    viewModel.startNewChat()
                    scope.launch { drawerState.close() }
                },
                onSearchClick = {
                    scope.launch { drawerState.close() }
                    showSearchSheet = true
                },
                onDeleteThread = { threadId ->
                    viewModel.deleteChat(threadId)
                },
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    showSettingsSheet = true
                },
                onClearAllChats = {
                    viewModel.clearAllChats()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopNavBar(
                    title = if (uiState.currentThread.messages.isEmpty()) "New Chat" else uiState.currentThread.title,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onNewChatClick = { viewModel.startNewChat() },
                    onSearchClick = { showSearchSheet = true },
                    onSettingsClick = { showSettingsSheet = true },
                    onCallClick = { viewModel.enterCallMode() }
                )
            },
            bottomBar = {
                ComposerBar(
                    text = uiState.composerText,
                    onTextChange = viewModel::onComposerTextChange,
                    onSend = viewModel::sendMessage,
                    onAttachClick = { showAttachSheet = true },
                    isRecording = uiState.isRecording,
                    onStartRecording = {
                        // Request mic permission before recording
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
                                PackageManager.PERMISSION_GRANTED
                            ) {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.startRecording()
                            }
                        } else {
                            viewModel.startRecording()
                        }
                    },
                    onStopRecording = viewModel::stopRecording,
                    onCancelRecording = viewModel::cancelRecording,
                    isAssistantTyping = uiState.isAssistantTyping,
                    pendingAttachmentUri = uiState.pendingAttachmentUri,
                    pendingAttachmentName = uiState.pendingAttachmentName,
                    pendingAttachmentMime = uiState.pendingAttachmentMime,
                    onRemoveAttachment = viewModel::clearPendingAttachment
                )
            },
            containerColor = PortfolioTheme.colors.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.currentThread.messages.isEmpty()) {
                    // Empty state: clean home — lotus + heading only, no suggestion chips
                    EmptyChatState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                    )
                } else {
                    // Message thread
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = uiState.currentThread.messages,
                            key = { it.id }
                        ) { message ->
                            val isThisSpeaking = (message.id == uiState.activeSpeakingMessageId)
                            val effectiveAudioState = if (isThisSpeaking) {
                                uiState.speakerAudioState
                            } else {
                                SpeakerAudioState.IDLE
                            }

                            MessageItem(
                                message = message,
                                audioState = effectiveAudioState,
                                onSpeakerClick = { viewModel.onSpeakerClick(message) },
                                onReactionClick = { reaction ->
                                    viewModel.toggleReaction(message.id, reaction)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSearchSheet) {
        SearchChatsSheet(
            threads = uiState.historyThreads,
            onSelectThread = { thread ->
                viewModel.selectChat(thread)
            },
            onDismiss = { showSearchSheet = false }
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(
            settings = uiState.settings,
            onThemeChange = viewModel::updateTheme,
            onLanguageChange = viewModel::updateLanguage,
            onClearAllChats = viewModel::clearAllChats,
            onDismiss = { showSettingsSheet = false }
        )
    }

    if (showAttachSheet) {
        AttachPickerSheet(
            onDismiss = { showAttachSheet = false },
            onPick = { option ->
                showAttachSheet = false
                when (option) {
                    AttachOption.IMAGES -> imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                    AttachOption.CAMERA -> cameraLauncher.launch(null)
                    AttachOption.FILES -> filePicker.launch(arrayOf("*/*"))
                }
            }
        )
    }
}

/**
 * Empty state — clean home:
 * • Centred lotus mark with soft radial glow.
 * • Heading in serif: "Let's get to work."
 * No suggestion chips, no clutter.
 */
@Composable
private fun EmptyChatState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Centred lotus mark — 5-petal orange flower with soft radial glow
        LotusMark(
            size = 80.dp,
            petalColor = PortfolioTheme.colors.accent,
            showGlow = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Heading in serif: Let's get to work.
        Text(
            text = "Let's get to work.",
            style = PortfolioTheme.typography.displayMedium.copy(
                fontFamily = InstrumentSerif,
                fontWeight = FontWeight.Normal,
                color = PortfolioTheme.colors.text,
                fontSize = 36.sp,
                lineHeight = 40.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

private fun queryDisplayName(context: android.content.Context, uri: android.net.Uri): String? {
    return runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
        }
    }.getOrNull()
}

private fun saveCameraBitmap(context: android.content.Context, bitmap: Bitmap): String? {
    return runCatching {
        val dir = File(context.cacheDir, "captures").apply { mkdirs() }
        val file = File(dir, "cap_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
        file.toURI().toString()
    }.getOrNull()
}
