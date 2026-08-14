package com.example.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
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

    /**
     * Флаг обхода воспроизведения аудио через AudioTrack.
     * Если установлен в true (по умолчанию), PCM-аудиоданные от сервера не воспроизводятся динамиком,
     * а WebSocket-поток остается полностью открытым для получения и обработки текстовой транскрипции.
     */
    var bypassAudioPlayback: Boolean = true

    private var audioTrack: AudioTrack? = null

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

                // 1. Отправляем начальный Setup кадр с поддержкой TEXT и AUDIO модальностей
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
                val modalities = JSONArray().apply {
                    put("TEXT")
                    put("AUDIO")
                }
                val genConfig = JSONObject().apply {
                    put("response_modalities", modalities)
                    put("responseModalities", modalities)
                    put("speech_config", speechConfig)
                    put("speechConfig", speechConfig)
                }
                val setupMessage = JSONObject().apply {
                    val setupObj = JSONObject().apply {
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
                    }
                    put("setup", setupObj)
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
                            // 1. Извлечение текста и обработка аудио из modelTurn.parts
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

                                    // Проверка наличия аудиоданных в part
                                    val inlineData = part?.optJSONObject("inlineData")
                                    if (inlineData != null) {
                                        val mimeType = inlineData.optString("mimeType", "")
                                        val audioBase64 = inlineData.optString("data", "")
                                        if (audioBase64.isNotEmpty() && mimeType.startsWith("audio/")) {
                                            playOrBypassAudio(audioBase64)
                                        }
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

                            // 3. Извлечение текста из userTurn (транскрипция входящей речи)
                            if (textContent.isNullOrBlank()) {
                                val userTurn = serverContent.optJSONObject("userTurn")
                                val userParts = userTurn?.optJSONArray("parts")
                                if (userParts != null && userParts.length() > 0) {
                                    val firstPart = userParts.optJSONObject(0)
                                    val uText = firstPart?.optString("text")
                                    if (!uText.isNullOrEmpty()) {
                                        textContent = uText
                                    }
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
     * Воспроизведение PCM аудио или его обход (байпасс) в зависимости от флага bypassAudioPlayback
     */
    private fun playOrBypassAudio(base64Data: String) {
        if (bypassAudioPlayback) {
            GlobalConsoleLogger.d("GEMINI_WS", "Аудио-поток от сервера получен, но динамик отключен (bypassAudioPlayback = true). WebSocket сессия продолжается.")
            return
        }

        try {
            val audioBytes = Base64.decode(base64Data, Base64.DEFAULT)
            if (audioBytes.isNotEmpty()) {
                initAudioTrackIfNeeded()
                audioTrack?.write(audioBytes, 0, audioBytes.size)
            }
        } catch (e: Exception) {
            GlobalConsoleLogger.e("GEMINI_WS", "Ошибка воспроизведения аудиочанка: ${e.localizedMessage}")
        }
    }

    private fun initAudioTrackIfNeeded() {
        if (audioTrack == null) {
            try {
                val sampleRate = 24000 // Стандартная частота дискретизации ответа Gemini Live
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize.coerceAtLeast(4096))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build().apply {
                        play()
                    }
            } catch (e: Exception) {
                GlobalConsoleLogger.e("GEMINI_WS", "Ошибка инициализации AudioTrack: ${e.localizedMessage}")
            }
        }
    }

    private fun stopAudioTrack() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            // Игнорируем ошибки при закрытии
        }
    }

    /**
     * Закрытие WebSocket сессии
     */
    fun disconnect() {
        try {
            stopAudioTrack()
            webSocket?.close(1000, "User stopped recording")
            webSocket = null
            isConnected = false
            GlobalConsoleLogger.i("GEMINI_WS", "WebSocket сессия Gemini Live закрыта пользователем")
        } catch (e: Exception) {
            GlobalConsoleLogger.e("GEMINI_WS", "Ошибка закрытия WebSocket: ${e.localizedMessage}")
        }
    }
}
