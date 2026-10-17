package com.example.model

import java.util.UUID

enum class MessageSender {
    USER,
    ASSISTANT
}

enum class Reaction {
    THUMBS_UP,
    THUMBS_DOWN
}

enum class SpeakerAudioState {
    IDLE,     // speaker icon, dim
    LOADING,  // spinning lotus icon, accent colour (the "fetching audio" state)
    SPEAKING, // pause icon, accent, with a slow pulsing ring around the button
    PAUSED    // play icon, accent
}

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val thoughts: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val reaction: Reaction? = null,
    val audioState: SpeakerAudioState = SpeakerAudioState.IDLE,
    val attachmentUri: String? = null,
    val attachmentName: String? = null,
    val attachmentMime: String? = null
) {
    val hasAttachment: Boolean get() = attachmentUri != null
    val isImageAttachment: Boolean get() =
        attachmentMime?.startsWith("image") == true ||
            attachmentName?.substringAfterLast('.', "")?.lowercase() in setOf("jpg", "jpeg", "png", "webp", "gif")
}

data class ChatThread(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Chat",
    val messages: List<Message> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ThemeOption(val displayName: String) {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("System")
}

val IndianLanguages = listOf(
    "English",
    "Kannada",
    "Hindi",
    "Tamil",
    "Telugu",
    "Malayalam",
    "Marathi",
    "Bengali",
    "Gujarati",
    "Odia",
    "Punjabi"
)

data class UserSettings(
    val theme: ThemeOption = ThemeOption.DARK,
    val replyLanguage: String = "English"
)
