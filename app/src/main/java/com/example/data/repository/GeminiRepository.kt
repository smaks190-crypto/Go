package com.example.data.repository

import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.utils.GeminiLiveWebSocketClient
import com.example.utils.GlobalConsoleLogger
import kotlinx.coroutines.flow.SharedFlow

/**
 * Репозиторий для работы с Gemini API:
 * - Live WebSocket клиент с моделью gemini-3.1-flash-live-preview
 * - REST HTTP клиент с моделью gemini-3.5-flash-lite (для HTTP POST generateContent)
 */
class GeminiRepository(
    private val liveWebSocketClient: GeminiLiveWebSocketClient = GeminiLiveWebSocketClient()
) {
    companion object {
        const val LIVE_MODEL = "models/gemini-3.1-flash-live-preview"
        val REST_MODELS = listOf("gemini-3.5-flash-lite", "gemini-3.1-flash-lite")
    }

    val liveResponseTextFlow: SharedFlow<String> get() = liveWebSocketClient.responseTextFlow
    val liveErrorFlow: SharedFlow<String> get() = liveWebSocketClient.errorFlow

    /**
     * Подключение к Gemini Live API через WebSocket.
     * Используется строго модель gemini-3.1-flash-live-preview
     */
    fun connectLive(apiKey: String, systemInstruction: String? = null) {
        GlobalConsoleLogger.i("GEMINI_REPO", "Подключение к Gemini Live WebSocket ($LIVE_MODEL)")
        liveWebSocketClient.connect(apiKey, LIVE_MODEL, systemInstruction)
    }

    fun sendAudioChunk(pcmData: ByteArray) {
        liveWebSocketClient.sendAudioChunk(pcmData)
    }

    fun finishAudioStream() {
        liveWebSocketClient.finishAudioStream()
    }

    fun disconnectLive() {
        liveWebSocketClient.disconnect()
    }

    /**
     * REST HTTP fallback для отправки текстовых/аудио запросов через :generateContent.
     * Используется строго gemini-3.5-flash-lite (без -live-preview в HTTP REST).
     */
    suspend fun generateContentRest(
        apiKey: String,
        userPrompt: String,
        systemPrompt: String? = null
    ): Result<String> {
        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = userPrompt)))),
            systemInstruction = systemPrompt?.let { GeminiContent(parts = listOf(GeminiPart(text = it))) }
        )

        var lastError = "Неизвестная ошибка"
        for (model in REST_MODELS) {
            try {
                GlobalConsoleLogger.d("GEMINI_REPO", "Запрос REST generateContent к $model...")
                val response = RetrofitClient.service.generateContent(model, apiKey, request)
                if (response.error == null) {
                    val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!responseText.isNullOrEmpty()) {
                        GlobalConsoleLogger.i("GEMINI_REPO", "Успешный ответ от $model")
                        return Result.success(responseText)
                    }
                } else {
                    lastError = "HTTP ${response.error.code ?: 400}: ${response.error.message ?: "Ошибка API"}"
                    GlobalConsoleLogger.w("GEMINI_REPO", "Ошибка $model: $lastError")
                }
            } catch (e: Exception) {
                lastError = e.localizedMessage ?: e.toString()
                GlobalConsoleLogger.w("GEMINI_REPO", "Исключение при обращении к $model: $lastError")
            }
        }
        return Result.failure(Exception(lastError))
    }
}
