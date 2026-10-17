package com.example.data

import com.example.model.Message

// The Android app uses native platform APIs for voice — no SDK, no API key,
// no extra APK size:
//
//   • STT  → android.speech.SpeechRecognizer  (built into every Android device,
//            API 26+ / Android 8+ through Android 15+)
//   • TTS  → android.speech.TextToSpeech     (built into every Android device)
//
// Both work offline-ish (STT needs the device's recognition engine; TTS needs
// a TTS voice pack) and require zero third-party dependencies.
//
// The Sarvam / ElevenLabs API key lives ONLY on the Render backend — never in
// this APK. See stt_service/ for the Python backend.

data class AssistantReply(
    val text: String,
    val thoughts: List<String>
)

interface AssistantRepository {
    suspend fun reply(message: String, history: List<Message>): AssistantReply
    suspend fun transcribe(locale: String): String   // locale like "en-IN", "hi-IN", "kn-IN"
    suspend fun speak(text: String, locale: String)   // real TTS via platform API
    suspend fun thoughtsFor(message: String): List<String>
}
