package lingadevaru.thoshan.data.api

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

private val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

/**
 * Retrofit service for the Thoshan Portfolio Assistant backend on Render.
 *
 * Endpoints:
 *   POST /chat  — proxy to Sarvam chat (SARVAM_API_KEY held server-side)
 *   POST /tts   — proxy to Sarvam TTS (SARVAM_API_KEY held server-side)
 *   POST /stt   — Vosk offline STT (no API key)
 *
 * The base URL is the Render web service URL (e.g. https://chatt-api.onrender.com),
 * injected via [ApiClient.create].
 *
 * Request/Response bodies use raw [RequestBody] / [ResponseBody] so no
 * serialization library is needed — JSON is built/parsed with org.json.
 */
interface ApiService {

    @POST("/chat")
    suspend fun chat(@Body request: RequestBody): Response<ResponseBody>

    @POST("/tts")
    suspend fun tts(@Body request: RequestBody): Response<ResponseBody>

    @POST("/stt")
    @Headers("Content-Type: audio/webm")
    suspend fun stt(@Body body: RequestBody): Response<ResponseBody>

    companion object {
        val jsonBody by lazy { JSON }
    }
}
