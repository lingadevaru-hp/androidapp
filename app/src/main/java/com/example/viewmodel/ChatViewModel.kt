package com.example.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AndroidTtsSpeechManager
import com.example.data.AssistantRepository
import com.example.data.AudioPlaybackService
import com.example.data.ChatPreferencesRepository
import com.example.data.NativeAssistantRepository
import com.example.model.ChatThread
import com.example.model.IndianLanguages
import com.example.model.Message
import com.example.model.MessageSender
import com.example.model.Reaction
import com.example.model.SpeakerAudioState
import com.example.model.ThemeOption
import com.example.model.UserSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

enum class CallState {
    LISTENING,
    THINKING,
    SPEAKING
}

data class ChatUiState(
    val currentThread: ChatThread = ChatThread(),
    val historyThreads: List<ChatThread> = emptyList(),
    val settings: UserSettings = UserSettings(),
    val composerText: String = "",
    val isAssistantTyping: Boolean = false,
    val isRecording: Boolean = false,
    val activeSpeakingMessageId: String? = null,
    val speakerAudioState: SpeakerAudioState = SpeakerAudioState.IDLE,
    val isCallModeActive: Boolean = false,
    val callElapsedSeconds: Int = 0,
    val callState: CallState = CallState.LISTENING,
    val callUserTranscript: String = "Tell me about Thoshan's top engineering projects.",
    val callAssistantTranscript: String = "Thoshan is a systems and mobile engineer specializing in high-performance Android, Kotlin, and distributed event systems.",
    val isCallMuted: Boolean = false,
    val isCallSpeakerOn: Boolean = true,
    val pendingAttachmentUri: String? = null,
    val pendingAttachmentName: String? = null,
    val pendingAttachmentMime: String? = null,
    val sttError: String? = null
) {
    val hasPendingAttachment: Boolean get() = pendingAttachmentUri != null
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AssistantRepository =
        NativeAssistantRepository(application, viewModelScope)
    private val prefsRepository = ChatPreferencesRepository(application)
    val audioService: AudioPlaybackService = AndroidTtsSpeechManager(application, viewModelScope)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var callTimerJob: Job? = null
    private var callSimulationJob: Job? = null

    init {
        // Collect persisted settings
        viewModelScope.launch {
            prefsRepository.settingsFlow.collectLatest { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }

        // Collect persisted threads
        viewModelScope.launch {
            prefsRepository.threadsFlow.collectLatest { threads ->
                _uiState.value = _uiState.value.copy(historyThreads = threads)
            }
        }

        // Collect speaker audio state
        viewModelScope.launch {
            audioService.audioState.collectLatest { state ->
                _uiState.value = _uiState.value.copy(
                    speakerAudioState = state,
                    activeSpeakingMessageId = audioService.activeMessageId.value
                )
            }
        }
    }

    fun onComposerTextChange(newText: String) {
        _uiState.value = _uiState.value.copy(composerText = newText)
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        val pendingUri = _uiState.value.pendingAttachmentUri
        val pendingName = _uiState.value.pendingAttachmentName
        val pendingMime = _uiState.value.pendingAttachmentMime
        if (trimmed.isEmpty() && pendingUri == null) return

        _uiState.value = _uiState.value.copy(
            composerText = "",
            isAssistantTyping = true,
            pendingAttachmentUri = null,
            pendingAttachmentName = null,
            pendingAttachmentMime = null,
            sttError = null
        )

        val userMessage = Message(
            sender = MessageSender.USER,
            text = trimmed,
            attachmentUri = pendingUri,
            attachmentName = pendingName,
            attachmentMime = pendingMime
        )

        val current = _uiState.value.currentThread
        val updatedMessages = current.messages + userMessage
        val updatedTitle = if (current.messages.isEmpty()) generateThreadTitle(trimmed, pendingName) else current.title
        val updatedThread = current.copy(
            title = updatedTitle,
            messages = updatedMessages,
            updatedAt = System.currentTimeMillis()
        )

        _uiState.value = _uiState.value.copy(currentThread = updatedThread)
        persistCurrentThread(updatedThread)

        viewModelScope.launch {
            val reply = repository.reply(trimmed, updatedMessages)
            val assistantMessage = Message(
                sender = MessageSender.ASSISTANT,
                text = reply.text,
                thoughts = reply.thoughts
            )

            val finalMessages = updatedThread.messages + assistantMessage
            val finalThread = updatedThread.copy(
                messages = finalMessages,
                updatedAt = System.currentTimeMillis()
            )

            _uiState.value = _uiState.value.copy(
                currentThread = finalThread,
                isAssistantTyping = false
            )
            persistCurrentThread(finalThread)

            // Note: autospeak and handsfree settings not yet implemented
            // Uncomment when added to UserSettings
            // if (_uiState.value.settings.autospeak || _uiState.value.settings.handsfree) {
            //     audioService.onSpeakerClick(
            //         "msg_${assistantMessage.id}",
            //         assistantMessage.text
            //     )
            // }
        }
    }

    private fun generateThreadTitle(prompt: String, attachmentFallback: String? = null): String {
        val source = prompt.ifBlank { attachmentFallback ?: "Attachment" }
        return source.take(34).let { if (source.length > 34) "$it…" else it }
    }

    fun setPendingAttachment(uri: String, name: String?, mime: String?) {
        _uiState.value = _uiState.value.copy(
            pendingAttachmentUri = uri,
            pendingAttachmentName = name,
            pendingAttachmentMime = mime
        )
    }

    fun clearPendingAttachment() {
        _uiState.value = _uiState.value.copy(
            pendingAttachmentUri = null,
            pendingAttachmentName = null,
            pendingAttachmentMime = null
        )
    }

    private fun persistCurrentThread(thread: ChatThread) {
        if (thread.messages.isEmpty()) return
        viewModelScope.launch {
            val existing = _uiState.value.historyThreads.toMutableList()
            val index = existing.indexOfFirst { it.id == thread.id }
            if (index >= 0) {
                existing[index] = thread
            } else {
                existing.add(0, thread)
            }
            prefsRepository.saveThreads(existing)
        }
    }

    fun startNewChat() {
        audioService.stop()

        val current = _uiState.value.currentThread
        if (current.messages.isEmpty()) {
            return
        }

        val newThread = ChatThread()
        _uiState.value = _uiState.value.copy(
            currentThread = newThread,
            composerText = "",
            isAssistantTyping = false,
            pendingAttachmentUri = null,
            pendingAttachmentName = null,
            pendingAttachmentMime = null
        )
    }

    fun selectChat(thread: ChatThread) {
        audioService.stop()
        _uiState.value = _uiState.value.copy(
            currentThread = thread,
            composerText = "",
            isAssistantTyping = false,
            pendingAttachmentUri = null,
            pendingAttachmentName = null,
            pendingAttachmentMime = null
        )
    }

    fun deleteChat(threadId: String) {
        if (_uiState.value.currentThread.id == threadId) {
            audioService.stop()
        }

        viewModelScope.launch {
            val updated = _uiState.value.historyThreads.filter { it.id != threadId }
            prefsRepository.saveThreads(updated)

            if (_uiState.value.currentThread.id == threadId) {
                _uiState.value = _uiState.value.copy(currentThread = ChatThread())
            }
        }
    }

    fun clearAllChats() {
        audioService.stop()
        viewModelScope.launch {
            prefsRepository.clearAll()
            _uiState.value = _uiState.value.copy(
                currentThread = ChatThread(),
                historyThreads = emptyList()
            )
        }
    }

    fun toggleReaction(messageId: String, reaction: Reaction) {
        val current = _uiState.value.currentThread
        val updatedMessages = current.messages.map { msg ->
            if (msg.id == messageId) {
                val newReaction = if (msg.reaction == reaction) null else reaction
                msg.copy(reaction = newReaction)
            } else {
                msg
            }
        }
        val updatedThread = current.copy(messages = updatedMessages)
        _uiState.value = _uiState.value.copy(currentThread = updatedThread)
        persistCurrentThread(updatedThread)
    }

    fun onSpeakerClick(message: Message) {
        audioService.onSpeakerClick(message.id, message.text)
    }

    // ── Real STT via SpeechRecognizer ─────────────────────────────────

    fun startRecording() {
        _uiState.value = _uiState.value.copy(
            isRecording = true,
            sttError = null
        )
    }

    fun stopRecording() {
        if (!_uiState.value.isRecording) return
        _uiState.value = _uiState.value.copy(isRecording = false)

        viewModelScope.launch {
            val locale = _uiState.value.settings.replyLanguage
            val transcript = repository.transcribe(locale)
            if (transcript.isNotBlank()) {
                _uiState.value = _uiState.value.copy(composerText = transcript)
            } else {
                _uiState.value = _uiState.value.copy(
                    sttError = "Didn't catch that — try again"
                )
                Toast.makeText(
                    getApplication(),
                    "Didn't catch that — try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun cancelRecording() {
        _uiState.value = _uiState.value.copy(isRecording = false)
        Toast.makeText(
            getApplication(),
            "Recording cancelled",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Settings
    fun updateTheme(theme: ThemeOption) {
        viewModelScope.launch {
            prefsRepository.updateTheme(theme)
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            prefsRepository.updateLanguage(language)
        }
    }

    // ── Real call mode: listen → think → speak loop ───────────────────

    fun enterCallMode() {
        audioService.stop()
        _uiState.value = _uiState.value.copy(
            isCallModeActive = true,
            callElapsedSeconds = 0,
            callState = CallState.LISTENING
        )
        startCallTimer()
        runRealCallLoop()
    }

    fun endCallMode() {
        callTimerJob?.cancel()
        callSimulationJob?.cancel()
        audioService.stop()
        _uiState.value = _uiState.value.copy(isCallModeActive = false)
    }

    fun toggleCallMute() {
        val muted = !_uiState.value.isCallMuted
        _uiState.value = _uiState.value.copy(isCallMuted = muted)
        if (muted) audioService.stop()
    }

    fun toggleCallSpeaker() {
        val speakerOn = !_uiState.value.isCallSpeakerOn
        _uiState.value = _uiState.value.copy(isCallSpeakerOn = speakerOn)
        if (!speakerOn) audioService.stop()
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    callElapsedSeconds = _uiState.value.callElapsedSeconds + 1
                )
            }
        }
    }

    private fun runRealCallLoop() {
        callSimulationJob?.cancel()
        callSimulationJob = viewModelScope.launch {
            while (_uiState.value.isCallModeActive) {
                if (_uiState.value.isCallMuted) {
                    _uiState.value = _uiState.value.copy(callState = CallState.LISTENING)
                    delay(1000)
                    continue
                }

                _uiState.value = _uiState.value.copy(callState = CallState.LISTENING)

                val locale = _uiState.value.settings.replyLanguage
                val heard = repository.transcribe(locale)

                if (!_uiState.value.isCallModeActive) break

                if (heard.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(
                        callUserTranscript = heard,
                        callState = CallState.THINKING
                    )

                    val reply = repository.reply(heard, _uiState.value.currentThread.messages)
                    val assistantText = reply.text

                    if (!_uiState.value.isCallModeActive) break

                    _uiState.value = _uiState.value.copy(
                        callAssistantTranscript = assistantText,
                        callState = CallState.SPEAKING
                    )

                    // Persist call turn to the thread
                    val current = _uiState.value.currentThread
                    val userMsg = Message(sender = MessageSender.USER, text = heard)
                    val botMsg = Message(
                        sender = MessageSender.ASSISTANT,
                        text = assistantText,
                        thoughts = reply.thoughts
                    )
                    val updatedThread = current.copy(
                        messages = current.messages + userMsg + botMsg,
                        updatedAt = System.currentTimeMillis()
                    )
                    _uiState.value = _uiState.value.copy(currentThread = updatedThread)
                    persistCurrentThread(updatedThread)

                    // Speak if speaker is on
                    if (_uiState.value.isCallSpeakerOn) {
                        audioService.onSpeakerClick(
                            "call_${System.currentTimeMillis()}",
                            assistantText
                        )
                        // Wait for speech to complete before listening again
                        val duration = (assistantText.length * 55L).coerceIn(2000L, 12000L)
                        delay(duration)
                    }

                    if (!_uiState.value.isCallModeActive) break
                    _uiState.value = _uiState.value.copy(callState = CallState.LISTENING)
                } else {
                    // Nothing heard — short pause before re-listening
                    delay(800)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioService.release()
    }
}
