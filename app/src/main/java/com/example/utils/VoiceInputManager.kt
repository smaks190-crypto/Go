package com.example.utils

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class VoiceInputManager(private val context: Context) {
    var currentEngineType: SpeechEngineType = SpeechEngineType.GEMINI_LIVE
        private set

    fun setSpeechEngine(type: SpeechEngineType) {
        currentEngineType = type
        GlobalConsoleLogger.i("VOICE", "Движок распознавания речи изменен на: ${type.displayName}")
    }

    private val liveClient = GeminiLiveWebSocketClient()

    @Volatile private var isAudioRecording = false
    private var audioRecordThread: Thread? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private val _frequencies = MutableStateFlow<List<Float>>(List(32) { 0.08f })
    val frequencies: StateFlow<List<Float>> = _frequencies.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val _voskStatus = MutableStateFlow("")
    val voskStatus: StateFlow<String> = _voskStatus.asStateFlow()

    private val _voskProgress = MutableStateFlow<Float?>(null)
    val voskProgress: StateFlow<Float?> = _voskProgress.asStateFlow()

    private var accumulatedText = ""
    private var isContinuous = false
    private var isPaused = false
    @Volatile private var isProcessingAllowed = true

    private var activeContextRef: java.lang.ref.WeakReference<Context>? = null

    var onErrorCallback: (() -> Unit)? = null
    var onChunkRecognized: ((String) -> Unit)? = null

    private fun getSavedApiKey(): String {
        val securePrefs = try {
            val masterKey = androidx.security.crypto.MasterKey.Builder(context)
                .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                .build()
            androidx.security.crypto.EncryptedSharedPreferences.create(
                context,
                "secure_prefs",
                masterKey,
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        }
        val key = securePrefs.getString("gemini_api_key", "") ?: ""
        return if (key.isNotBlank()) key else BuildConfig.GEMINI_API_KEY
    }

    private fun getWavHeader(pcmLength: Long): ByteArray {
        val totalDataLen = pcmLength + 36
        val byteRate = 16000 * 2
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = 1
        header[23] = 0
        header[24] = (16000 and 0xff).toByte()
        header[25] = ((16000 shr 8) and 0xff).toByte()
        header[26] = ((16000 shr 16) and 0xff).toByte()
        header[27] = ((16000 shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2
        header[33] = 0
        header[34] = 16
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (pcmLength and 0xff).toByte()
        header[41] = ((pcmLength shr 8) and 0xff).toByte()
        header[42] = ((pcmLength shr 16) and 0xff).toByte()
        header[43] = ((pcmLength shr 24) and 0xff).toByte()
        return header
    }

    private fun transcribeRecordedAudio(audioBytes: ByteArray, modelEngine: String) {
        if (audioBytes.isEmpty()) {
            _errorState.value = "Запись пуста"
            return
        }

        _voskStatus.value = "TRANSCRIBING"
        _partialText.value = "Распознавание речи..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val wavHeader = getWavHeader(audioBytes.size.toLong())
                val wavBytes = ByteArray(wavHeader.size + audioBytes.size)
                System.arraycopy(wavHeader, 0, wavBytes, 0, wavHeader.size)
                System.arraycopy(audioBytes, 0, wavBytes, wavHeader.size, audioBytes.size)

                val base64Audio = Base64.encodeToString(wavBytes, Base64.NO_WRAP)

                val apiKey = getSavedApiKey()
                if (apiKey.isBlank()) {
                    throw Exception("Gemini API ключ не найден. Задайте его в настройках.")
                }

                val request = com.example.data.api.GeminiRequest(
                    contents = listOf(
                        com.example.data.api.GeminiContent(
                            parts = listOf(
                                com.example.data.api.GeminiPart(
                                    inlineData = com.example.data.api.GeminiInlineData(
                                        mimeType = "audio/wav",
                                        data = base64Audio
                                    )
                                ),
                                com.example.data.api.GeminiPart(
                                    text = "Ты — встроенный STT-движок $modelEngine. Твоя задача — максимально точно расшифровать этот аудиофайл в текст на русском языке. Выведи ТОЛЬКО расшифрованный текст, без комментариев, пояснений и знаков препинания (кроме необходимых). Если в аудио тишина или шум, выведи пустую строку."
                                )
                            )
                        )
                    )
                )

                val modelsToTry = listOf(
                    "gemini-3.5-flash-lite",
                    "gemini-3.1-flash-lite"
                )

                var text = ""
                var lastErrorMsg: String? = null

                for (model in modelsToTry) {
                    try {
                        GlobalConsoleLogger.d("VOICE", "[$modelEngine] Отправка запроса к модели $model...")
                        val response = com.example.data.api.RetrofitClient.service.generateContent(model, apiKey, request)
                        if (response.error == null) {
                            val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                            if (!candidateText.isNullOrEmpty()) {
                                text = candidateText
                                GlobalConsoleLogger.i("VOICE", "[$modelEngine] Успешно распознано через $model: «$text»")
                                break
                            }
                        } else {
                            lastErrorMsg = response.error.message
                            GlobalConsoleLogger.w("VOICE", "[$modelEngine] Модель $model вернула ошибку: ${response.error.message}")
                        }
                    } catch (e: Exception) {
                        lastErrorMsg = e.localizedMessage
                        GlobalConsoleLogger.w("VOICE", "[$modelEngine] Исключение при обращении к $model: ${e.localizedMessage}")
                    }
                }

                if (text.isBlank() && lastErrorMsg != null) {
                    GlobalConsoleLogger.e("VOICE", "[$modelEngine] Ни одна из моделей Gemini не вернула результат: $lastErrorMsg")
                }

                withContext(Dispatchers.Main) {
                    _voskStatus.value = "READY"
                    _partialText.value = ""
                    _recognizedText.value = text
                    if (text.isNotBlank()) {
                        GlobalConsoleLogger.i("VOICE", "[$modelEngine] Распознано: «$text»")
                        onChunkRecognized?.invoke(text)
                    } else {
                        GlobalConsoleLogger.w("VOICE", "[$modelEngine] Речь не обнаружена")
                    }
                }
            } catch (e: Exception) {
                Log.e("VoiceInputManager", "Transcription error", e)
                withContext(Dispatchers.Main) {
                    _voskStatus.value = "READY"
                    _partialText.value = ""
                    _errorState.value = "Ошибка распознавания: ${e.localizedMessage}"
                    onErrorCallback?.invoke()
                }
            }
        }
    }

    private fun stopAudioThread() {
        isAudioRecording = false
        try {
            audioRecordThread?.interrupt()
            audioRecordThread = null
        } catch (_: Throwable) {}
        _rmsDb.value = 0f
    }

    fun startListening(callerContext: Context) {
        GlobalConsoleLogger.i("VOICE", "Запуск распознавания микрофона (${currentEngineType.displayName})...")
        muteSystemBeeps()
        activeContextRef = java.lang.ref.WeakReference(callerContext)
        isContinuous = true
        isPaused = false
        isProcessingAllowed = true

        accumulatedText = ""
        _recognizedText.value = ""
        _partialText.value = ""
        _errorState.value = null

        stopAudioThread()

        val apiKey = getSavedApiKey()
        if (apiKey.isNotBlank()) {
            val sttPrompt = "Ты — модуль распознавания финансовой речи. Твоя единственная задача — точнейше транскрибировать все слова пользователя о доходах и расходах (например: 'потратил 500 рублей на продукты', 'такси 300'). Выводи только текст распознанной речи без приветствий и сносок."
            liveClient.connect(apiKey, "models/gemini-3.1-flash-live-preview", sttPrompt)
            
            CoroutineScope(Dispatchers.IO).launch {
                liveClient.responseTextFlow.collect { text ->
                    withContext(Dispatchers.Main) {
                        if (text.isNotBlank()) {
                            accumulatedText = if (accumulatedText.isBlank()) text else "$accumulatedText $text"
                            _recognizedText.value = accumulatedText
                            _partialText.value = accumulatedText
                            GlobalConsoleLogger.i("VOICE", "[Gemini Live] Распознано: «$text» (Итого: «$accumulatedText»)")
                            onChunkRecognized?.invoke(accumulatedText)
                        }
                    }
                }
            }
            
            CoroutineScope(Dispatchers.IO).launch {
                liveClient.errorFlow.collect { err ->
                    withContext(Dispatchers.Main) {
                        GlobalConsoleLogger.e("VOICE", "[Gemini Live] Ошибка WebSocket: $err")
                        _errorState.value = err
                        onErrorCallback?.invoke()
                    }
                }
            }
        }

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = maxOf(minBufferSize, sampleRate * 2 / 10)

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            _errorState.value = "Не удалось инициализировать микрофон"
            _isListening.value = false
            return
        }

        _isListening.value = true
        isAudioRecording = true
        audioRecord.startRecording()

        audioRecordThread = Thread {
            val buffer = ShortArray(2048)
            val audioBuffer = ByteArrayOutputStream()

            while (isAudioRecording && !Thread.currentThread().isInterrupted) {
                val nread = audioRecord.read(buffer, 0, buffer.size)
                if (nread > 0) {
                    val pcmBytes = ByteArray(nread * 2)
                    var sumSquare = 0.0
                    for (i in 0 until nread) {
                        val sample = buffer[i].toDouble()
                        sumSquare += sample * sample
                        val s = buffer[i]
                        pcmBytes[i * 2] = (s.toInt() and 0xFF).toByte()
                        pcmBytes[i * 2 + 1] = ((s.toInt() shr 8) and 0xFF).toByte()
                    }
                    audioBuffer.write(pcmBytes, 0, pcmBytes.size)

                    val rms = Math.sqrt(sumSquare / nread) / 32768.0
                    val volumeLevel = (Math.sqrt(rms) * 12.0).toFloat().coerceIn(0f, 12f)
                    _rmsDb.value = volumeLevel

                    liveClient.sendAudioChunk(pcmBytes)

                    try {
                        val real = FloatArray(64)
                        val imag = FloatArray(64)
                        for (i in 0 until minOf(nread, 64)) {
                            real[i] = buffer[i] / 32768.0f
                        }
                        fft(real, imag)
                        val prev = _frequencies.value
                        val smoothed = List(32) { index ->
                            val mag = Math.sqrt((real[index] * real[index] + imag[index] * imag[index]).toDouble()).toFloat()
                            val normMag = (mag / 8.0f).coerceIn(0f, 1f)
                            val prevVal = if (index < prev.size) prev[index] else 0.08f
                            (prevVal * 0.60f + normMag * 0.40f).coerceIn(0.08f, 1.0f)
                        }
                        _frequencies.value = smoothed
                    } catch (_: Exception) {}
                }
            }

            try {
                audioRecord.stop()
                audioRecord.release()
            } catch (_: Throwable) {}

            _frequencies.value = List(32) { 0.08f }
            liveClient.finishAudioStream()

            if (_recognizedText.value.isBlank()) {
                transcribeRecordedAudio(audioBuffer.toByteArray(), "Gemini Live")
            }
        }.apply {
            name = "GeminiAudioRecordThread"
            start()
        }
    }

    fun startListening() {
        activeContextRef = null
        isContinuous = true
        isPaused = false
        startListening(context)
    }

    fun stopListening() {
        GlobalConsoleLogger.i("VOICE", "Остановка распознавания речи")
        isProcessingAllowed = false
        isContinuous = false
        isPaused = false
        stopRecognizerOnly()
    }

    private var isMutedByVoice = false

    private fun muteSystemBeeps() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager ?: return
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(android.media.AudioManager.STREAM_SYSTEM, android.media.AudioManager.ADJUST_MUTE, 0)
                audioManager.adjustStreamVolume(android.media.AudioManager.STREAM_NOTIFICATION, android.media.AudioManager.ADJUST_MUTE, 0)
            } else {
                @Suppress("DEPRECATION")
                audioManager.setStreamMute(android.media.AudioManager.STREAM_SYSTEM, true)
                @Suppress("DEPRECATION")
                audioManager.setStreamMute(android.media.AudioManager.STREAM_NOTIFICATION, true)
            }
            isMutedByVoice = true
        } catch (_: Throwable) {}
    }

    private fun restoreSystemBeeps() {
        if (!isMutedByVoice) return
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager ?: return
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(android.media.AudioManager.STREAM_SYSTEM, android.media.AudioManager.ADJUST_UNMUTE, 0)
                audioManager.adjustStreamVolume(android.media.AudioManager.STREAM_NOTIFICATION, android.media.AudioManager.ADJUST_UNMUTE, 0)
            } else {
                @Suppress("DEPRECATION")
                audioManager.setStreamMute(android.media.AudioManager.STREAM_SYSTEM, false)
                @Suppress("DEPRECATION")
                audioManager.setStreamMute(android.media.AudioManager.STREAM_NOTIFICATION, false)
            }
            isMutedByVoice = false
        } catch (_: Throwable) {}
    }

    private fun stopRecognizerOnly() {
        stopAudioThread()
        liveClient.disconnect()
        restoreSystemBeeps()

        activeContextRef = null
        _isListening.value = false
        _rmsDb.value = 0f
    }

    fun updateSimulatedFrequencies(volume: Float) {
        val norm = volume.coerceIn(0f, 1f)
        _rmsDb.value = norm * 12f
        val base = norm.coerceIn(0.08f, 1.0f)
        _frequencies.value = List(32) { index ->
            val harmonic = (kotlin.math.sin(index * 0.4f) * 0.25f).toFloat()
            (base + harmonic * norm).coerceIn(0.08f, 1.0f)
        }
    }

    fun pauseListening() {
        if (isContinuous) {
            isPaused = true
            stopRecognizerOnly()
        }
    }

    fun resumeListening() {
        if (isContinuous && isPaused) {
            isPaused = false
            val currentContext = activeContextRef?.get() ?: context
            startListening(currentContext)
        }
    }

    fun clear() {
        accumulatedText = ""
        _recognizedText.value = ""
        _partialText.value = ""
        _errorState.value = null
    }

    fun setRecognizedTextManually(text: String) {
        accumulatedText = text
        _recognizedText.value = text
        _partialText.value = ""
    }

    fun destroy() {
        stopListening()
    }

    private fun fft(real: FloatArray, imag: FloatArray) {
        val n = real.size
        if (n <= 1) return

        var j = 0
        for (i in 0 until n) {
            if (i < j) {
                val tempR = real[i]
                real[i] = real[j]
                real[j] = tempR
                val tempI = imag[i]
                imag[i] = imag[j]
                imag[j] = tempI
            }
            var m = n shr 1
            while (m >= 1 && j >= m) {
                j -= m
                m = m shr 1
            }
            j += m
        }

        var size = 2
        while (size <= n) {
            val halfSize = size shr 1
            for (i in 0 until n step size) {
                for (k in 0 until halfSize) {
                    val angle = -2.0 * Math.PI * k / size
                    val cos = Math.cos(angle).toFloat()
                    val sin = Math.sin(angle).toFloat()

                    val tR = real[i + k + halfSize] * cos - imag[i + k + halfSize] * sin
                    val tI = real[i + k + halfSize] * sin + imag[i + k + halfSize] * cos

                    real[i + k + halfSize] = real[i + k] - tR
                    imag[i + k + halfSize] = imag[i + k] - tI
                    real[i + k] += tR
                    imag[i + k] += tI
                }
            }
            size = size shl 1
        }
    }
}
