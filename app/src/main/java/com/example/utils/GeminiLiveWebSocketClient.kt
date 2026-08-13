package com.example.utils

import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Потоковый клиент WebSocket для работы с Gemini Multimodal Live API
 * (Direct Audio Streaming over WebSocket)
 */
class GeminiLiveWebSocketClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // Потоковое чтение
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    @Volatile private var isConnected = false

    private val _responseTextFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val responseTextFlow: SharedFlow<String> = _responseTextFlow.asSharedFlow()

    private val _errorFlow = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val errorFlow: SharedFlow<String> = _errorFlow.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Открытие WebSocket соединения и отправка конфигурационного кадра (Setup)
     */
    fun connect(
        apiKey: String,
        modelName: String = "models/gemini-3.1-flash-live-preview",
        systemInstructionText: String? = null
    ) {
        if (isConnected) {
            GlobalConsoleLogger.d("GEMINI_WS", "WebSocket уже подключен")
            return
        }

        val url = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .build()

        GlobalConsoleLogger.i("GEMINI_WS", "Инициализация WebSocket соединения с Gemini Live API ($modelName)...")

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                isConnected = true
                GlobalConsoleLogger.i("GEMINI_WS", "WebSocket соединение успешно установлено!")

                // 1. Отправляем начальный Setup кадр с модальностью AUDIO
                val prebuiltVoiceConfig = JSONObject().apply {
                    put("voice_name", "Puck")
                    put("voiceName", "Puck")
                }
                val voiceConfig = JSONObject().apply {
                    put("prebuilt_voice_config", prebuiltVoiceConfig)
                    put("prebuiltVoiceConfig", prebuiltVoiceConfig)
                }
                val speechConfig = JSONObject().apply {
                    put("voice_config", voiceConfig)
                    put("voiceConfig", voiceConfig)
                }
                val genConfig = JSONObject().apply {
                    put("response_modalities", JSONArray().put("AUDIO"))
                    put("responseModalities", JSONArray().put("AUDIO"))
                    put("speech_config", speechConfig)
                    put("speechConfig", speechConfig)
                }
                val setupMessage = JSONObject().apply {
                    put("setup", JSONObject().apply {
                        put("model", modelName)
                        put("generation_config", genConfig)
                        put("generationConfig", genConfig)
                        systemInstructionText?.let { sysPrompt ->
                            val sysInst = JSONObject().apply {
                                put("parts", JSONArray().put(JSONObject().apply {
                                    put("text", sysPrompt)
                                }))
                            }
                            put("system_instruction", sysInst)
                            put("systemInstruction", sysInst)
                        }
                    })
                }

                ws.send(setupMessage.toString())
                GlobalConsoleLogger.d("GEMINI_WS", "Отправлен Setup кадр конфигурации Gemini Live API")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                GlobalConsoleLogger.d("GEMINI_WS", "Получено сообщение от Gemini Live API: $text")
                try {
                    val json = JSONObject(text)
                    
                    // Парсинг ответа
                    var textContent: String? = null

                    if (json.has("serverContent")) {
                        val serverContent = json.optJSONObject("serverContent")
                        if (serverContent != null) {
                            // 1. Извлечение текста из modelTurn.parts
                            val modelTurn = serverContent.optJSONObject("modelTurn")
                            val parts = modelTurn?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val sb = StringBuilder()
                                for (i in 0 until parts.length()) {
                                    val part = parts.optJSONObject(i)
                                    val partText = part?.optString("text")
                                    if (!partText.isNullOrEmpty()) {
                                        sb.append(partText)
                                    }
                                }
                                if (sb.isNotEmpty()) {
                                    textContent = sb.toString()
                                }
                            }

                            // 2. Извлечение текста из outputAudioTranscription
                            if (textContent.isNullOrBlank()) {
                                val audioTranscription = serverContent.optJSONObject("outputAudioTranscription")
                                val transcriptText = audioTranscription?.optString("text")
                                if (!transcriptText.isNullOrEmpty()) {
                                    textContent = transcriptText
                                }
                            }
                        }
                    } else if (json.has("text")) {
                        textContent = json.optString("text")
                    }

                    if (!textContent.isNullOrBlank()) {
                        scope.launch {
                            _responseTextFlow.emit(textContent)
                        }
                    }
                } catch (e: Exception) {
                    GlobalConsoleLogger.e("GEMINI_WS", "Ошибка парсинга ответа Gemini: ${e.localizedMessage}", e)
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                val errorMsg = t.localizedMessage ?: "Ошибка подключения WebSocket"
                GlobalConsoleLogger.e("GEMINI_WS", "WebSocket сбой соединения: $errorMsg", t)
                scope.launch {
                    _errorFlow.emit(errorMsg)
                }
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                isConnected = false
                GlobalConsoleLogger.i("GEMINI_WS", "WebSocket закрывается: $code / $reason")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                isConnected = false
                GlobalConsoleLogger.i("GEMINI_WS", "WebSocket закрыт: $code / $reason")
            }
        })
    }

    /**
     * Потоковая передача чанка сырых PCM-байтов с микрофона
     */
    fun sendAudioChunk(pcmData: ByteArray) {
        val ws = webSocket
        if (!isConnected || ws == null) {
            return
        }

        try {
            val base64Data = Base64.encodeToString(pcmData, Base64.NO_WRAP)
            val audioMessage = JSONObject().apply {
                put("realtimeInput", JSONObject().apply {
                    put("mediaChunks", JSONArray().put(JSONObject().apply {
                        put("mimeType", "audio/pcm;rate=16000")
                        put("data", base64Data)
                    }))
                })
            }
            ws.send(audioMessage.toString())
        } catch (e: Exception) {
            GlobalConsoleLogger.e("GEMINI_WS", "Ошибка отправки аудиочанка в WebSocket: ${e.localizedMessage}", e)
        }
    }

    /**
     * Завершение отправки аудиопотока
     */
    fun finishAudioStream() {
        val ws = webSocket
        if (!isConnected || ws == null) return

        try {
            // Сигнал завершения клиентского ввода
            val clientContent = JSONObject().apply {
                put("clientContent", JSONObject().apply {
                    put("turnComplete", true)
                })
            }
            ws.send(clientContent.toString())
            GlobalConsoleLogger.d("GEMINI_WS", "Отправлен маркер завершения turnComplete")
        } catch (e: Exception) {
            GlobalConsoleLogger.e("GEMINI_WS", "Ошибка отправки turnComplete: ${e.localizedMessage}")
        }
    }

    /**
     * Закрытие WebSocket сессии
     */
    fun disconnect() {
        try {
            webSocket?.close(1000, "User stopped recording")
            webSocket = null
            isConnected = false
            GlobalConsoleLogger.i("GEMINI_WS", "WebSocket сессия Gemini Live закрыта пользователем")
        } catch (e: Exception) {
            GlobalConsoleLogger.e("GEMINI_WS", "Ошибка закрытия WebSocket: ${e.localizedMessage}")
        }
    }
}
