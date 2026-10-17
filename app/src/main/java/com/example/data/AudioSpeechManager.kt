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
 * Features:
 *   • Supports Indian languages: English (India), Kannada, Hindi, Tamil, Telugu, Malayalam
 *   • Configurable speech rate and pitch
 *   • Dynamic locale resolution from user settings
 *   • Automatic fallback to device default
 *
 * No extra dependencies, no native libraries, no API key required.
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

    // Configurable speech parameters
    private var speechRate: Float = 1.0f
    private var speechPitch: Float = 1.0f
    private var currentLanguage: String = "en-IN"

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                setSpeechParameters(speechRate, speechPitch)
                isTtsReady = true
                setupUtteranceListener()
            } else {
                Logw("TextToSpeech init failed: $status")
            }
        }
    }

    /**
     * Set speech parameters.
     * @param rate Speech rate (0.5f = half speed, 1.0f = normal, 2.0f = double speed)
     * @param pitch Speech pitch (0.5f = lower, 1.0f = normal, 2.0f = higher)
     */
    fun setSpeechParameters(rate: Float = 1.0f, pitch: Float = 1.0f) {
        speechRate = rate.coerceIn(0.1f, 2.0f)
        speechPitch = pitch.coerceIn(0.1f, 2.0f)
        tts?.setSpeechRate(speechRate)
        tts?.setPitch(speechPitch)
    }

    /**
     * Set the language for TTS.
     * @param languageCode Language code (e.g., "en-IN", "kn-IN", "hi-IN")
     */
    fun setLanguage(languageCode: String) {
        currentLanguage = languageCode
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
                    // Fall back to English (India)
                    tts?.setLanguage(Locale("en", "IN"))
                    // Log that the language is not supported
                    Logw("Language $currentLanguage not supported, falling back to en-IN")
                }
                
                // Ensure speech parameters are set
                tts?.setSpeechRate(speechRate)
                tts?.setPitch(speechPitch)
                
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
        // Map language code to Locale
        return when (currentLanguage.uppercase()) {
            "KN-IN", "KN", "KANADA" -> Locale("kn", "IN")
            "HI-IN", "HI", "HINDI" -> Locale("hi", "IN")
            "TA-IN", "TA", "TAMIL" -> Locale("ta", "IN")
            "TE-IN", "TE", "TELUGU" -> Locale("te", "IN")
            "ML-IN", "ML", "MALAYALAM" -> Locale("ml", "IN")
            else -> Locale("en", "IN") // Default to English India
        }
    }

    /**
     * Get list of supported language codes.
     */
    fun getSupportedLanguages(): List<String> {
        return listOf("en-IN", "kn-IN", "hi-IN", "ta-IN", "te-IN", "ml-IN")
    }

    /**
     * Get display name for language code.
     */
    fun getLanguageDisplayName(code: String): String {
        return when (code.uppercase()) {
            "EN-IN" -> "English (India)"
            "KN-IN" -> "Kannada"
            "HI-IN" -> "Hindi"
            "TA-IN" -> "Tamil"
            "TE-IN" -> "Telugu"
            "ML-IN" -> "Malayalam"
            else -> code
        }
    }

    /**
     * Check if TTS engine supports a specific language.
     */
    fun isLanguageSupported(languageCode: String): Boolean {
        val locale = when (languageCode.uppercase()) {
            "KN-IN", "KN", "KANADA" -> Locale("kn", "IN")
            "HI-IN", "HI", "HINDI" -> Locale("hi", "IN")
            "TA-IN", "TA", "TAMIL" -> Locale("ta", "IN")
            "TE-IN", "TE", "TELUGU" -> Locale("te", "IN")
            "ML-IN", "ML", "MALAYALAM" -> Locale("ml", "IN")
            else -> Locale("en", "IN")
        }
        
        if (tts == null || !isTtsReady) return false
        
        val result = tts?.setLanguage(locale)
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
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
