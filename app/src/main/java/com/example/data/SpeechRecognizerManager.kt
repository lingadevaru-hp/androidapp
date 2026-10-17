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
 * Requires:
 *   • android.permission.RECORD_AUDIO (granted at runtime)
 *   • SpeechRecognizer.isRecognitionAvailable(context) == true
 *
 * Usage:
 *   val stt = SpeechRecognizerManager(context, scope)
 *   stt.startListening(locale = Locale.forLanguageTag("en-IN")) { transcript ->
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

    private var recognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var activeJob: Job? = null
    private var currentListener: ((String) -> Unit)? = null

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
                    // Volume level — could be surfaced in UI later
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
                    // Interim results — optional UI feedback
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    /**
     * Start listening for speech and return the transcript via [onResult].
     * Cancels any in-flight recognition first.
     */
    fun startListening(
        locale: Locale = Locale.getDefault(),
        onResult: (String) -> Unit
    ) {
        if (!isAvailable.value) {
            onResult("")
            return
        }

        cancel()

        currentListener = onResult
        _isListening.value = true

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.languageTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        recognizer?.startListening(recognizerIntent)
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
    }
}
