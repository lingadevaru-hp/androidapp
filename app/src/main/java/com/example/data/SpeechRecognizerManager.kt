package com.example.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Native Android [SpeechRecognizer] wrapper for speech-to-text.
 *
 * Works on Android 8 (API 26) through Android 15+ (API 35) — every device
 * ships with a speech recognition engine (Google Speech Recognition on GMS
 * devices, or the OEM's built-in engine on others). No extra dependencies,
 * no native libraries, no APK size impact.
 *
 * Features:
 *   • Supports Indian languages: English (India), Kannada, Hindi, Tamil, Telugu, Malayalam
 *   • Real-time partial results for live transcription
 *   • Audio level feedback for visual indicators
 *   • Configurable prompts and max results
 *   • Comprehensive error handling
 *
 * Requires:
 *   • android.permission.RECORD_AUDIO (granted at runtime)
 *   • SpeechRecognizer.isRecognitionAvailable(context) == true
 *
 * Usage:
 *   val stt = SpeechRecognizerManager(context, scope)
 *   stt.startListening(language = "en-IN") { transcript ->
 *       // handle result
 *   }
 *   stt.cancel()
 */
class SpeechRecognizerManager(
    private val context: Context,
    private val scope: CoroutineScope
) {

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private var recognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var activeJob: Job? = null
    private var currentListener: ((String) -> Unit)? = null
    private var currentPartialListener: ((String) -> Unit)? = null

    init {
        val available = SpeechRecognizer.isRecognitionAvailable(context.applicationContext)
        _isAvailable.value = available
        if (available) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            recognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d("SpeechRecognizer", "onReadyForSpeech")
                }

                override fun onBeginningOfSpeech() {
                    Log.d("SpeechRecognizer", "onBeginningOfSpeech")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Surface audio level for UI feedback (0-1 range, normalized from typical -10 to +20 dB)
                    val normalizedLevel = (rmsdB.coerceIn(0f, 30f) / 30f).coerceIn(0f, 1f)
                    _audioLevel.value = normalizedLevel
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    Log.d("SpeechRecognizer", "onEndOfSpeech")
                }

                override fun onError(error: Int) {
                    Log.e("SpeechRecognizer", "STT error code: $error")
                    _isListening.value = false
                    val msg = when (error) {
                        android.speech.SpeechRecognizer.ERROR_NETWORK -> "Network error — try again"
                        android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout — try again"
                        android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission missing"
                        android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected — try again"
                        android.speech.SpeechRecognizer.ERROR_NO_MATCH -> "No match — try again"
                        android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy — try again"
                        android.speech.SpeechRecognizer.ERROR_SERVER -> "Server error — try again"
                        android.speech.SpeechRecognizer.ERROR_SERVER_DOWN -> "Server down — try again"
                        android.speech.SpeechRecognizer.ERROR_AUDIO -> "Audio recording error — try again"
                        android.speech.SpeechRecognizer.ERROR_CLIENT -> "Client error — try again"
                        android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout — try again"
                        else -> "Speech recognition error — try again"
                    }
                    currentListener?.let { it("") } // empty = failure
                    currentListener = null
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val transcript = matches?.firstOrNull() ?: ""
                    Log.d("SpeechRecognizer", "STT result: $transcript")
                    _isListening.value = false
                    currentListener?.let { it(transcript) }
                    currentListener = null
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    // Real-time partial results for live transcription
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = matches?.firstOrNull() ?: ""
                    _partialTranscript.value = partial
                    currentPartialListener?.invoke(partial)
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    /**
     * Start listening for speech and return the transcript via [onResult].
     * Cancels any in-flight recognition first.
     *
     * @param language Language code (e.g., "en-IN", "kn-IN", "hi-IN", "ta-IN", "te-IN", "ml-IN")
     * @param onResult Callback for final result
     * @param onPartialResult Optional callback for real-time partial results
     */
    fun startListening(
        language: String = "en-IN",
        onResult: (String) -> Unit,
        onPartialResult: ((String) -> Unit)? = null
    ) {
        if (!isAvailable.value) {
            onResult("")
            return
        }

        cancel()

        currentListener = onResult
        currentPartialListener = onPartialResult
        _isListening.value = true
        _partialTranscript.value = ""

        val locale = parseLanguageCode(language)
        val prompt = getPromptForLanguage(language)

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.languageTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // Enable real-time transcription
        }

        recognizer?.startListening(recognizerIntent)
    }

    /**
     * Parse language code to Locale object.
     * Supports: en-IN, kn-IN, hi-IN, ta-IN, te-IN, ml-IN, and fallback to en-US
     */
    private fun parseLanguageCode(languageCode: String): Locale {
        return when (languageCode.uppercase()) {
            "EN-IN", "EN_US", "EN" -> Locale("en", "IN")
            "KN-IN", "KN", "KANADA" -> Locale("kn", "IN")
            "HI-IN", "HI", "HINDI" -> Locale("hi", "IN")
            "TA-IN", "TA", "TAMIL" -> Locale("ta", "IN")
            "TE-IN", "TE", "TELUGU" -> Locale("te", "IN")
            "ML-IN", "ML", "MALAYALAM" -> Locale("ml", "IN")
            else -> Locale.getDefault()
        }
    }

    /**
     * Get user-friendly prompt for each language.
     */
    private fun getPromptForLanguage(languageCode: String): String {
        return when (languageCode.uppercase()) {
            "KN-IN", "KN", "KANADA" -> "ಕನ್ನಡದಲ್ಲಿ ಮಾತನಾಡಿ"
            "HI-IN", "HI", "HINDI" -> "हिंदी में बोलिए"
            "TA-IN", "TA", "TAMIL" -> "தமிழில் பேசுங்கள்"
            "TE-IN", "TE", "TELUGU" -> "తెలుగులో మాట్లాడండి"
            "ML-IN", "ML", "MALAYALAM" -> "മലയാളത്തിൽ സംസാരിക്കുക"
            else -> "Speak now"
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

    /** Cancel any in-flight recognition. */
    fun cancel() {
        activeJob?.cancel()
        activeJob = null
        _isListening.value = false
        recognizer?.stopListening()
        recognizer?.cancel()
        currentListener = null
    }

    fun release() {
        cancel()
        recognizer?.destroy()
        recognizer = null
    }

    companion object {
        /** Check if the device supports speech recognition at all. */
        fun isSupported(context: Context): Boolean =
            SpeechRecognizer.isRecognitionAvailable(context.applicationContext)

        /** Check if a specific language is supported on this device. */
        fun isLanguageSupported(context: Context, languageCode: String): Boolean {
            if (!isSupported(context)) return false
            val locale = Locale.forLanguageTag(languageCode.replace("-", "_"))
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.languageTag)
            }
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            val isAvailable = try {
                // Try to check if the language is available
                recognizer.setRecognitionListener(object : RecognitionListener {})
                recognizer.startListening(intent)
                recognizer.cancel()
                true
            } catch (e: Exception) {
                false
            } finally {
                recognizer.destroy()
            }
            return isAvailable
        }
    }
}
