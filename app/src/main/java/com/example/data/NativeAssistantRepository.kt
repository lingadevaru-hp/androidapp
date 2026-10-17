package com.example.data

import com.example.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Real Android implementation of [AssistantRepository] using native platform APIs:
 *
 *   • STT  → SpeechRecognizerManager  (android.speech.SpeechRecognizer)
 *   • TTS  → AndroidTtsSpeechManager  (android.speech.TextToSpeech)
 *
 * No SDK, no API key, no extra APK size, works on Android 8 (API 26) through
 * Android 15+ (API 35).
 *
 * The chat "reply" logic still uses the local knowledge base (mock pattern)
 * so the app works fully offline — the backend is only needed if the owner
 * later wants a cloud model. STT and TTS themselves are real and local.
 */
class NativeAssistantRepository(
    context: android.content.Context,
    private val scope: CoroutineScope
) : AssistantRepository {

    private val stt = SpeechRecognizerManager(context, scope)
    private val tts = AndroidTtsSpeechManager(context, scope)

    override suspend fun reply(message: String, history: List<Message>): AssistantReply {
        // Local knowledge base — same responses as the web demo, works offline.
        return withContext(Dispatchers.Default) {
            buildReply(message.trim().lowercase())
        }
    }

    override suspend fun transcribe(locale: String): String {
        return withContext(Dispatchers.Main) {
            stt.isAvailable.value.takeIf { it == true } ?: run {
                "Speech recognition is not available on this device."
            }
            // The SpeechRecognizer is callback-based; wrap in a suspending call.
            suspendCancellableCoroutine<String> { cont ->
                val lang = resolveLocale(locale)
                stt.startListening(locale = lang) { transcript ->
                    cont.resume(transcript)
                }
                // Auto-cancel after 30 seconds if no result
                scope.launch {
                    kotlinx.coroutines.delay(30_000)
                    if (stt.isListening.value) {
                        stt.cancel()
                        cont.resume("")
                    }
                }
            }
        }
    }

    override suspend fun speak(text: String, locale: String) {
        withContext(Dispatchers.Main) {
            val lang = resolveLocale(locale)
            // AndroidTtsSpeechManager currently resolves locale internally.
            // In a future pass, pass `lang` into startAudio via a parameter.
            tts.onSpeakerClick("tts_${System.currentTimeMillis()}", text)
            // Wait roughly for speech to finish (heuristic by length)
            val duration = (text.length * 55L).coerceIn(2000L, 15000L)
            kotlinx.coroutines.delay(duration)
        }
    }

    override suspend fun thoughtsFor(message: String): List<String> {
        return reply(message, emptyList()).thoughts
    }

    private fun resolveLocale(locale: String): Locale {
        return when (locale.lowercase()) {
            "en-in", "english" -> Locale.forLanguageTag("en-IN")
            "hi-in", "hindi" -> Locale.forLanguageTag("hi-IN")
            "kn-in", "kannada" -> Locale.forLanguageTag("kn-IN")
            "ta-in", "tamil" -> Locale.forLanguageTag("ta-IN")
            "te-in", "telugu" -> Locale.forLanguageTag("te-IN")
            "ml-in", "malayalam" -> Locale.forLanguageTag("ml-IN")
            "mr-in", "marathi" -> Locale.forLanguageTag("mr-IN")
            "bn-in", "bengali" -> Locale.forLanguageTag("bn-IN")
            "gu-in", "gujarati" -> Locale.forLanguageTag("gu-IN")
            "or-in", "odia" -> Locale.forLanguageTag("or-IN")
            "pa-in", "punjabi" -> Locale.forLanguageTag("pa-IN")
            else -> Locale.getDefault()
        }
    }

    // ── Local knowledge base (same replies as web demo, offline) ──────────

    private fun buildReply(clean: String): AssistantReply {
        return when {
            clean.contains("hi") || clean.contains("hello") || clean.contains("namaskara") ||
            clean.contains("namaste") || clean.contains("hey") -> {
                AssistantReply(
                    text = "Namaskara! 🙏 Welcome to Thoshan's portfolio space. I can converse in English, Kannada, Hindi, or any Indian language you prefer. How can I help you explore his work today?",
                    thoughts = listOf(
                        "Recognized friendly greeting intent",
                        "Switched to bilingual Kannada-English tone",
                        "Highlighted support for Indian languages"
                    )
                )
            }
            clean.contains("project") || clean.contains("built") || clean.contains("portfolio") ||
            clean.contains("work") || clean.contains("app") -> {
                AssistantReply(
                    text = "Here are a few notable projects Thoshan has crafted: 🛠️\n\n" +
                            "• **FOSS Coin** — A live Solana SPL token to fund open-source projects, trading on Orca DEX.\n" +
                            "• **Thoshan Flash** — Fine-tuned Gemma-2-9B language model (QLoRA/PEFT, 4-bit quantized).\n" +
                            "• **Decentralized Insurance Claim D-App** — Ethereum smart-contract insurance automation.\n" +
                            "• **Voice Portfolio Assistant** — This chatbot: STT + TTS + hands-free voice call mode.\n\n" +
                            "Explore live demos and source at github.com/lingadevaru-hp!",
                    thoughts = listOf(
                        "Identified query regarding developer projects and engineering portfolio",
                        "Retrieved key systems: FOSS Coin, Thoshan Flash, Insurance D-App",
                        "Formatted structured bulleted list with repository link"
                    )
                )
            }
            clean.contains("stack") || clean.contains("tech") || clean.contains("skill") ||
            clean.contains("experience") || clean.contains("language") -> {
                AssistantReply(
                    text = "Thoshan specializes in systems and mobile engineering: ⚡\n\n" +
                            "• **Mobile & Systems**: Kotlin, Jetpack Compose, Android Internals, Coroutines & Flow\n" +
                            "• **Backend & Distributed**: Go, Cloudflare Workers, PostgreSQL, Redis\n" +
                            "• **Architecture**: Offline-first patterns, Clean Architecture, Reactive event streams\n" +
                            "• **AI/ML**: QLoRA / PEFT fine-tuning, vector DBs, speech pipelines\n\n" +
                            "Code quality, strict type safety, and polished aesthetics are top priorities.",
                    thoughts = listOf(
                        "Analyzing core proficiencies across mobile, distributed backend, and architecture",
                        "Selected highest-impact language and framework specialties",
                        "Synthesized concise technical overview"
                    )
                )
            }
            clean.contains("hire") || clean.contains("contact") || clean.contains("email") ||
            clean.contains("available") || clean.contains("reach") || clean.contains("call") -> {
                AssistantReply(
                    text = "Thoshan is open to select engineering opportunities and consulting! 📬\n\n" +
                            "Write directly to contact@lingadevaru.in.",
                    thoughts = listOf(
                        "Parsed engagement query for hiring, consulting, and direct contact",
                        "Retrieved verified contact channel: contact@lingadevaru.in"
                    )
                )
            }
            clean.contains("2 + 2") || clean.contains("2+2") -> {
                AssistantReply(
                    text = "2 + 2 is 4! 🔢 While math is dependable, I'm best at questions about Thoshan's projects, tech stack, and background. What would you like to explore?",
                    thoughts = listOf(
                        "Computed numeric expression 2 + 2 = 4",
                        "Detected off-domain query",
                        "Politely redirected context back to portfolio domain"
                    )
                )
            }
            else -> {
                AssistantReply(
                    text = "Thanks for asking! ✨ I can tell you about Thoshan's projects, tech stack, and background, or help you connect with him directly. Try asking about his mobile apps or FOSS Coin!",
                    thoughts = listOf(
                        "Query does not match standard index keywords",
                        "Synthesizing contextual portfolio suggestions",
                        "Guided user to projects and contact options"
                    )
                )
            }
        }
    }
}
