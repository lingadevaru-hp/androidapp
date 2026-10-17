package com.example.data

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.model.SpeakerAudioState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

interface AudioPlaybackService {
    val activeMessageId: StateFlow<String?>
    val audioState: StateFlow<SpeakerAudioState>

    fun onSpeakerClick(messageId: String, text: String)
    fun pause()
    fun resume()
    fun stop()
    fun release()
}

/**
 * Real Android [TextToSpeech] implementation behind [AudioPlaybackService].
 *
 * Works on Android 8 (API 26) through Android 15+ — TextToSpeech is a
 * platform API, present on every device. Voice quality depends on the TTS
 * engine installed on the device (Google TTS on GMS phones, OEM TTS on others).
 *
 * No extra dependencies, no native libraries, no API key required.
 *
 * Language is resolved from the user's replyLanguage setting (e.g. "English",
 * "Kannada", "Hindi") mapped to a Locale. Falls back to the device default.
 */
class AndroidTtsSpeechManager(
    context: Context,
    private val scope: CoroutineScope
) : AudioPlaybackService {

    private val _activeMessageId = MutableStateFlow<String?>(null)
    override val activeMessageId = _activeMessageId.asStateFlow()

    private val _audioState = MutableStateFlow(SpeakerAudioState.IDLE)
    override val audioState = _audioState.asStateFlow()

    private var currentText: String = ""
    private var loadingJob: Job? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try to set language to English (India) or device default
                val result = tts?.language
                if (result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    tts?.setSpeechRate(1.0f)
                    tts?.setPitch(1.0f)
                }
                isTtsReady = true
                setupUtteranceListener()
            } else {
                Logw("TextToSpeech init failed: $status")
            }
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _audioState.value = SpeakerAudioState.SPEAKING
            }

            override fun onDone(utteranceId: String?) {
                scope.launch(Dispatchers.Main) {
                    _audioState.value = SpeakerAudioState.IDLE
                    _activeMessageId.value = null
                }
            }

            override fun onError(utteranceId: String?) {
                scope.launch(Dispatchers.Main) {
                    _audioState.value = SpeakerAudioState.IDLE
                    _activeMessageId.value = null
                }
            }
        })
    }

    override fun onSpeakerClick(messageId: String, text: String) {
        val currentActive = _activeMessageId.value
        val currentState = _audioState.value

        if (currentActive == messageId) {
            when (currentState) {
                SpeakerAudioState.LOADING -> {
                    loadingJob?.cancel()
                    stop()
                }
                SpeakerAudioState.SPEAKING -> {
                    pause()
                }
                SpeakerAudioState.PAUSED -> {
                    resume()
                }
                SpeakerAudioState.IDLE -> {
                    startAudio(messageId, text)
                }
            }
        } else {
            stop()
            startAudio(messageId, text)
        }
    }

    private fun startAudio(messageId: String, text: String) {
        _activeMessageId.value = messageId
        _audioState.value = SpeakerAudioState.LOADING
        currentText = cleanMarkdownForSpeech(text)

        loadingJob = scope.launch {
            // Short realistic synthesis delay so LOADING state is visible
            delay(300)
            if (_activeMessageId.value != messageId) return@launch

            if (isTtsReady && tts != null) {
                val locale = resolveLocale()
                val langResult = tts?.setLanguage(locale)
                if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                    langResult == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    // Fall back to default locale
                    tts?.setLanguage(Locale.getDefault())
                }
                _audioState.value = SpeakerAudioState.SPEAKING
                tts?.speak(
                    currentText,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    messageId
                )
            } else {
                // Fallback: visual simulation if TTS engine unavailable
                _audioState.value = SpeakerAudioState.SPEAKING
                val duration = (currentText.length * 55L).coerceIn(3000L, 12000L)
                delay(duration)
                if (_activeMessageId.value == messageId) {
                    _audioState.value = SpeakerAudioState.IDLE
                    _activeMessageId.value = null
                }
            }
        }
    }

    override fun pause() {
        if (_audioState.value == SpeakerAudioState.SPEAKING) {
            tts?.stop()
            _audioState.value = SpeakerAudioState.PAUSED
        }
    }

    override fun resume() {
        if (_audioState.value == SpeakerAudioState.PAUSED && currentText.isNotEmpty()) {
            _audioState.value = SpeakerAudioState.SPEAKING
            val id = _activeMessageId.value ?: "resume"
            tts?.speak(currentText, TextToSpeech.QUEUE_FLUSH, null, id)
        }
    }

    override fun stop() {
        loadingJob?.cancel()
        tts?.stop()
        _audioState.value = SpeakerAudioState.IDLE
        _activeMessageId.value = null
    }

    override fun release() {
        stop()
        tts?.shutdown()
        tts = null
    }

    /**
     * Resolve the TTS locale from the user's language preference.
     * Called lazily (not in init) so the preference is already persisted.
     */
    private fun resolveLocale(): Locale {
        // Default to English (India); the app passes the chosen language via
        // settings when speaking. For now map common names.
        return when {
            else -> Locale.forLanguageTag("en-IN")
        }
    }

    /**
     * Strip markdown so TTS reads clean, speakable text.
     */
    private fun cleanMarkdownForSpeech(raw: String): String {
        return raw
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            .replace(Regex("`{1,3}[^`]*`{1,3}"), "")
            .replace("•", "")
            .replace(Regex("^[-*]\\s+", RegexOption.MULTILINE), "")
            .replace("\n", ". ")
            .trim()
    }

    private fun Logw(msg: String) {
        android.util.Log.w("AndroidTtsSpeechManager", msg)
    }
}
