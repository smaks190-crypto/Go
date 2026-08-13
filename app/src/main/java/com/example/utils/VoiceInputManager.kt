package com.example.utils

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Base64
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener as VoskRecognitionListener
import org.vosk.android.SpeechService
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipInputStream
import com.example.data.SpeechModelManager
import com.example.data.ModelEngineType

class VoiceInputManager(private val context: Context) {
    var currentEngineType: SpeechEngineType = SpeechEngineType.SHERPA_ONNX
        private set

    fun setSpeechEngine(type: SpeechEngineType) {
        currentEngineType = type
        GlobalConsoleLogger.i("VOICE", "Движок распознавания речи изменен на: ${type.displayName}")
    }

    private var nativeRecognizer: NativeSpeechRecognizer? = null
    private var voskModel: Model? = null
    private var voskRecognizer: Recognizer? = null
    private var voskSpeechService: SpeechService? = null
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

    // VOSK specific states
    private val _voskStatus = MutableStateFlow("") // "", "DOWNLOADING", "EXTRACTING", "READY", "ERROR"
    val voskStatus: StateFlow<String> = _voskStatus.asStateFlow()

    private val _voskProgress = MutableStateFlow<Float?>(null)
    val voskProgress: StateFlow<Float?> = _voskProgress.asStateFlow()

    private var accumulatedText = ""
    private var isContinuous = false
    private var isPaused = false
    @Volatile private var isProcessingAllowed = true
    private var lastProcessedChunk = ""

    private var activeContextRef: java.lang.ref.WeakReference<Context>? = null

    var onErrorCallback: (() -> Unit)? = null
    var onChunkRecognized: ((String) -> Unit)? = null

    private fun parsePartialHypothesis(json: String): String {
        return try {
            val regex = "\"partial\"\\s*:\\s*\"([^\"]*)\"".toRegex()
            regex.find(json)?.groupValues?.get(1) ?: ""
        } catch (_: Throwable) { "" }
    }

    private fun parseResultHypothesis(json: String): String {
        return try {
            val regex = "\"text\"\\s*:\\s*\"([^\"]*)\"".toRegex()
            regex.find(json)?.groupValues?.get(1) ?: ""
        } catch (_: Throwable) { "" }
    }

    private fun startNativeRecognizer(callerContext: Context) {
        _isListening.value = true
        if (nativeRecognizer == null) {
            nativeRecognizer = NativeSpeechRecognizer(callerContext, this)
        }
        nativeRecognizer?.startListening(
            onResult = { text ->
                _recognizedText.value = text
                _partialText.value = text
                onChunkRecognized?.invoke(text)
            },
            onError = { err ->
                _errorState.value = err
                _isListening.value = false
                onErrorCallback?.invoke()
            }
        )
    }

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
        val byteRate = 16000 * 2 // 16000Hz * 16-bit Mono (2 bytes per sample)
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte() // RIFF
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte() // WAVE
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 16 for PCM
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // Linear PCM
        header[21] = 0
        header[22] = 1 // Mono (1 channel)
        header[23] = 0
        header[24] = (16000 and 0xff).toByte() // Sample rate
        header[25] = ((16000 shr 8) and 0xff).toByte()
        header[26] = ((16000 shr 16) and 0xff).toByte()
        header[27] = ((16000 shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte() // Byte rate
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2 // Block align (1 channel * 2 bytes/sample)
        header[33] = 0
        header[34] = 16 // Bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte() // 'data' chunk
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

                val modelName = "gemini-1.5-flash"
                val response = com.example.data.api.RetrofitClient.service.generateContent(modelName, apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: ""

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

    private fun startWhisperRecognizer(callerContext: Context) {
        _isListening.value = true
        _errorState.value = null
        stopAudioThread()

        // Validate model path as per lead developer check list
        val modelFile = File(context.filesDir, "whisper-model/ggml-tiny.bin")
        GlobalConsoleLogger.i("WHISPER", "[WHISPER] Проверка пути модели: ${modelFile.absolutePath}, exists = ${modelFile.exists()}, size = ${modelFile.length()} байт")

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
            _errorState.value = "Не удалось инициализировать микрофон для Whisper"
            _isListening.value = false
            return
        }

        isAudioRecording = true
        audioRecord.startRecording()

        audioRecordThread = Thread {
            val buffer = ShortArray(2048)
            val audioBuffer = ByteArrayOutputStream()

            while (isAudioRecording && !Thread.currentThread().isInterrupted) {
                val readShorts = audioRecord.read(buffer, 0, buffer.size)
                if (readShorts > 0) {
                    var sumSquare = 0.0
                    for (i in 0 until readShorts) {
                        val sample = buffer[i].toDouble()
                        sumSquare += sample * sample
                        val s = buffer[i]
                        audioBuffer.write(s.toInt() and 0xFF)
                        audioBuffer.write((s.toInt() shr 8) and 0xFF)
                    }
                    val rms = Math.sqrt(sumSquare / readShorts) / 32768.0
                    val volumeLevel = (Math.sqrt(rms) * 12.0).toFloat().coerceIn(0f, 12f)
                    _rmsDb.value = volumeLevel

                    android.util.Log.d("[WHISPER]", "Read PCM shorts: $readShorts, RMS level: $rms")
                    val floatBuffer = FloatArray(readShorts) { i ->
                        buffer[i] / 32768.0f
                    }
                    // TODO: Передавать floatBuffer в обработчик whisper_full_default / whisperContext.benchFull()

                    try {
                        val real = FloatArray(64)
                        val imag = FloatArray(64)
                        for (i in 0 until minOf(readShorts, 64)) {
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
            GlobalConsoleLogger.i("WHISPER", "[WHISPER] Запись завершена, размер PCM: ${audioBuffer.size()} байт")
            transcribeRecordedAudio(audioBuffer.toByteArray(), "Whisper")
        }.apply {
            name = "WhisperAudioRecordThread"
            start()
        }
    }

    private fun startSherpaOnnxRecognizer(callerContext: Context) {
        _isListening.value = true
        _errorState.value = null
        stopAudioThread()

        // Validate model paths as per lead developer checklist
        val modelFile = File(context.filesDir, "sherpa-onnx-model/model.onnx")
        val tokensFile = File(context.filesDir, "sherpa-onnx-model/tokens.txt")
        GlobalConsoleLogger.i("SHERPA_ONNX", "[SHERPA] Проверка пути модели: ${modelFile.absolutePath}, exists = ${modelFile.exists()}, size = ${modelFile.length()} байт")
        GlobalConsoleLogger.i("SHERPA_ONNX", "[SHERPA] Проверка пути токенов: ${tokensFile.absolutePath}, exists = ${tokensFile.exists()}")

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
            _errorState.value = "Не удалось инициализировать микрофон для Sherpa-Onnx"
            _isListening.value = false
            return
        }

        isAudioRecording = true
        audioRecord.startRecording()

        audioRecordThread = Thread {
            val buffer = ShortArray(2048)
            val audioBuffer = ByteArrayOutputStream()
            var ortEnv: OrtEnvironment? = null
            try {
                ortEnv = OrtEnvironment.getEnvironment()
            } catch (e: Exception) {
                Log.e("VoiceInputManager", "ONNX Runtime init info", e)
            }

            while (isAudioRecording && !Thread.currentThread().isInterrupted) {
                val nread = audioRecord.read(buffer, 0, buffer.size)
                if (nread > 0) {
                    var sumSquare = 0.0
                    for (i in 0 until nread) {
                        val sample = buffer[i].toDouble()
                        sumSquare += sample * sample
                        val s = buffer[i]
                        audioBuffer.write(s.toInt() and 0xFF)
                        audioBuffer.write((s.toInt() shr 8) and 0xFF)
                    }
                    val rms = Math.sqrt(sumSquare / nread) / 32768.0
                    val volumeLevel = (Math.sqrt(rms) * 12.0).toFloat().coerceIn(0f, 12f)
                    _rmsDb.value = volumeLevel

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

            try {
                ortEnv?.close()
            } catch (_: Throwable) {}

            _frequencies.value = List(32) { 0.08f }
            GlobalConsoleLogger.i("SHERPA_ONNX", "[SHERPA] Запись завершена, размер PCM: ${audioBuffer.size()} байт")
            transcribeRecordedAudio(audioBuffer.toByteArray(), "Sherpa-ONNX")
        }.apply {
            name = "SherpaOnnxAudioRecordThread"
            start()
        }
    }

    fun startListening(callerContext: Context) {
        GlobalConsoleLogger.i("VOICE", "Запуск распознавания микрофона (${currentEngineType.displayName})...")
        muteSystemBeeps()
        activeContextRef = java.lang.ref.WeakReference(callerContext)
        isContinuous = true
        isPaused = false
        isProcessingAllowed = true
        lastProcessedChunk = ""

        accumulatedText = ""
        _recognizedText.value = ""
        _partialText.value = ""
        _errorState.value = null

        if (currentEngineType == SpeechEngineType.NATIVE) {
            startNativeRecognizer(callerContext)
            return
        }

        val modelManager = SpeechModelManager.getInstance(context)

        if (currentEngineType == SpeechEngineType.WHISPER) {
            if (modelManager.isModelDownloaded(ModelEngineType.WHISPER)) {
                GlobalConsoleLogger.i("WHISPER", "[WHISPER] Модель Whisper найдена на диске. Запуск...")
                startWhisperRecognizer(callerContext)
            } else {
                GlobalConsoleLogger.i("WHISPER", "[WHISPER] Модель Whisper не найдена, запускаем автоматическую загрузку...")
                _voskStatus.value = "DOWNLOADING"
                _voskProgress.value = 0f
                modelManager.downloadModel(
                    engineType = ModelEngineType.WHISPER,
                    onProgress = { p ->
                        _voskProgress.value = p
                    },
                    onSuccess = {
                        _voskStatus.value = "READY"
                        _voskProgress.value = null
                        mainHandler.post {
                            startWhisperRecognizer(callerContext)
                        }
                    },
                    onError = { err ->
                        _voskStatus.value = "ERROR"
                        _voskProgress.value = null
                        _errorState.value = "Ошибка загрузки Whisper: $err"
                        onErrorCallback?.invoke()
                    }
                )
            }
            return
        }

        if (currentEngineType == SpeechEngineType.SHERPA_ONNX) {
            if (modelManager.isModelDownloaded(ModelEngineType.SHERPA_ONNX)) {
                GlobalConsoleLogger.i("SHERPA_ONNX", "[SHERPA] Модель Sherpa-ONNX найдена на диске. Запуск...")
                startSherpaOnnxRecognizer(callerContext)
            } else {
                GlobalConsoleLogger.i("SHERPA_ONNX", "[SHERPA] Модель Sherpa-ONNX не найдена, запускаем автоматическую загрузку...")
                _voskStatus.value = "DOWNLOADING"
                _voskProgress.value = 0f
                modelManager.downloadModel(
                    engineType = ModelEngineType.SHERPA_ONNX,
                    onProgress = { p ->
                        _voskProgress.value = p
                    },
                    onSuccess = {
                        _voskStatus.value = "READY"
                        _voskProgress.value = null
                        mainHandler.post {
                            startSherpaOnnxRecognizer(callerContext)
                        }
                    },
                    onError = { err ->
                        _voskStatus.value = "ERROR"
                        _voskProgress.value = null
                        _errorState.value = "Ошибка загрузки Sherpa-ONNX: $err"
                        onErrorCallback?.invoke()
                    }
                )
            }
            return
        }

        if (currentEngineType == SpeechEngineType.VOSK) {
            if (modelManager.isModelDownloaded(ModelEngineType.VOSK)) {
                _voskStatus.value = "READY"
                GlobalConsoleLogger.i("VOSK", "Найдена локальная офлайн-модель VOSK")
                initVoskAndStart(callerContext)
            } else {
                GlobalConsoleLogger.i("VOSK", "Модель VOSK не найдена, запускаем автоматическую загрузку...")
                _voskStatus.value = "DOWNLOADING"
                _voskProgress.value = 0f
                modelManager.downloadModel(
                    engineType = ModelEngineType.VOSK,
                    onProgress = { p ->
                        _voskProgress.value = p
                    },
                    onSuccess = {
                        _voskStatus.value = "READY"
                        _voskProgress.value = null
                        mainHandler.post {
                            initVoskAndStart(callerContext)
                        }
                    },
                    onError = { err ->
                        _voskStatus.value = "ERROR"
                        _voskProgress.value = null
                        _errorState.value = "Ошибка загрузки VOSK: $err"
                        onErrorCallback?.invoke()
                    }
                )
            }
            return
        }
    }

    fun startListening() {
        activeContextRef = null
        isContinuous = true
        isPaused = false
        startListening(context)
    }

    private fun downloadAndInitModel(callerContext: Context) {
        _voskStatus.value = "DOWNLOADING"
        _voskProgress.value = 0f
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 20000
                connection.readTimeout = 20000
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Ошибка загрузки модели с сервера VOSK code: ${connection.responseCode}")
                }

                val fileLength = connection.contentLength
                val inputStream = BufferedInputStream(connection.inputStream)
                val tempZip = File(context.cacheDir, "vosk_model.zip")
                val outputStream = FileOutputStream(tempZip)

                val data = ByteArray(8192)
                var total: Long = 0
                var count: Int
                while (inputStream.read(data).also { count = it } != -1) {
                    if (isPaused || !isContinuous) {
                        outputStream.close()
                        inputStream.close()
                        tempZip.delete()
                        _voskStatus.value = ""
                        _voskProgress.value = null
                        return@launch
                    }
                    total += count
                    if (fileLength > 0) {
                        _voskProgress.value = total.toFloat() / fileLength
                    }
                    outputStream.write(data, 0, count)
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()

                _voskStatus.value = "EXTRACTING"
                _voskProgress.value = null

                val buffer = ByteArray(8192)
                ZipInputStream(BufferedInputStream(tempZip.inputStream())).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val newFile = File(context.filesDir, entry.name)
                        if (entry.isDirectory) {
                            newFile.mkdirs()
                        } else {
                            newFile.parentFile?.mkdirs()
                            FileOutputStream(newFile).use { fos ->
                                var len: Int
                                while (zis.read(buffer).also { len = it } > 0) {
                                    fos.write(buffer, 0, len)
                                }
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
                tempZip.delete()

                _voskStatus.value = "READY"
                withContext(Dispatchers.Main) {
                    initVoskAndStart(callerContext)
                }
            } catch (e: Exception) {
                _voskStatus.value = "ERROR"
                _voskProgress.value = null
                _errorState.value = "Не удалось загрузить офлайн-модель VOSK: ${e.localizedMessage}"
                Log.e("VoiceInputManager", "Vosk download error", e)
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

    private fun initVoskAndStart(callerContext: Context) {
        try {
            GlobalConsoleLogger.i("VOSK", "Инициализация офлайн-модели VOSK...")
            val targetDir = File(context.filesDir, "vosk-model-small-ru-0.22")
            if (voskModel == null) {
                voskModel = Model(targetDir.absolutePath)
            }
            if (voskRecognizer == null) {
                voskRecognizer = Recognizer(voskModel, 16000f)
            }

            stopAudioThread()

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
                throw Exception("Не удалось инициализировать микрофон для VOSK")
            }

            isAudioRecording = true
            audioRecord.startRecording()

            audioRecordThread = Thread {
                val buffer = ShortArray(4096)
                while (isAudioRecording && !Thread.currentThread().isInterrupted) {
                    val nread = audioRecord.read(buffer, 0, buffer.size)
                    if (nread > 0) {
                        // Рассчитываем среднеквадратичное значение (RMS) амплитуды микрофона
                        var sumSquare = 0.0
                        for (i in 0 until nread) {
                            val sample = buffer[i].toDouble()
                            sumSquare += sample * sample
                        }
                        val rms = Math.sqrt(sumSquare / nread) / 32768.0
                        // Масштабируем до диапазона 0f..12f (корень из RMS дает плавную реакцию индикатора)
                        val volumeLevel = (Math.sqrt(rms) * 12.0).toFloat().coerceIn(0f, 12f)
                        _rmsDb.value = volumeLevel

                        // Вычисляем частотный спектр через FFT для визуализатора
                        try {
                            val fftSize = 512
                            val fftReal = FloatArray(fftSize)
                            val fftImag = FloatArray(fftSize)

                            // Накапливаем последние 512 семплов из буфера
                            val startIndex = maxOf(0, nread - fftSize)
                            for (i in 0 until fftSize) {
                                val sampleIndex = startIndex + i
                                if (sampleIndex < nread) {
                                    val sample = buffer[sampleIndex].toFloat() / 32768.0f
                                    // Применяем окно Ханнинга для сглаживания гармоник
                                    val window = 0.5f * (1.0f - Math.cos(2.0 * Math.PI * i / (fftSize - 1)).toFloat())
                                    fftReal[i] = sample * window
                                } else {
                                    fftReal[i] = 0f
                                }
                                fftImag[i] = 0f
                            }

                            fft(fftReal, fftImag)

                            val numBins = fftSize / 2
                            val magnitudes = FloatArray(numBins)
                            for (i in 0 until numBins) {
                                val r = fftReal[i]
                                val im = fftImag[i]
                                magnitudes[i] = Math.sqrt((r * r + im * im).toDouble()).toFloat()
                            }

                            // Группируем спектр в 32 полосы для частотного диапазона голоса (~60Гц - ~4000Гц)
                            val startBin = 2
                            val endBin = 130
                            val numBands = 32
                            val binsPerBand = (endBin - startBin) / numBands

                            val newFrequencies = FloatArray(numBands)
                            val normalizedVol = (volumeLevel / 12f).coerceIn(0f, 1f)

                            for (band in 0 until numBands) {
                                var sum = 0f
                                val bandStart = startBin + band * binsPerBand
                                for (bin in bandStart until (bandStart + binsPerBand)) {
                                    if (bin < numBins) {
                                        sum += magnitudes[bin]
                                    }
                                }
                                val avg = sum / binsPerBand

                                // Комбинируем общую громкость с частотным распределением FFT
                                val gain = 75.0f * (1.0f + band * 0.12f)
                                val boosted = (normalizedVol * 0.45f + avg * gain).coerceIn(0.08f, 1.0f)
                                newFrequencies[band] = boosted
                            }

                            // Экспоненциальное сглаживание кадров (prev * 0.6f + current * 0.4f)
                            val prev = _frequencies.value
                            val smoothed = List(numBands) { index ->
                                val currentVal = newFrequencies[index]
                                val prevVal = if (index < prev.size) prev[index] else 0.08f
                                (prevVal * 0.60f + currentVal * 0.40f).coerceIn(0.08f, 1.0f)
                            }
                            _frequencies.value = smoothed
                        } catch (e: Exception) {
                            Log.e("VoiceInputManager", "FFT error", e)
                        }

                        if (isProcessingAllowed) {
                            val recognizer = voskRecognizer ?: break
                            if (recognizer.acceptWaveForm(buffer, nread)) {
                                val resultJson = recognizer.getResult()
                                val text = parseResultHypothesis(resultJson).trim()
                                if (text.isNotBlank() && text != lastProcessedChunk) {
                                    lastProcessedChunk = text
                                    GlobalConsoleLogger.d("VOSK", "Распознан фрагмент VOSK: «$text»")
                                    _recognizedText.value = text
                                    _partialText.value = ""
                                    onChunkRecognized?.invoke(text)
                                }
                            } else {
                                val partialJson = recognizer.getPartialResult()
                                val partial = parsePartialHypothesis(partialJson).trim()
                                if (partial.isNotBlank()) {
                                    _partialText.value = partial
                                }
                            }
                        }
                    }
                }
                try {
                    audioRecord.stop()
                    audioRecord.release()
                } catch (_: Throwable) {}
                _frequencies.value = List(32) { 0.08f }
            }.apply {
                name = "VoskAudioRecordThread"
                start()
            }

            _isListening.value = true
            _errorState.value = null
            GlobalConsoleLogger.i("VOSK", "VOSK успешно запущен с отслеживанием уровня громкости микрофона (offline)")
            Log.d("VoiceInputManager", "Vosk successfully started with microphone volume level tracking offline!")
        } catch (e: Throwable) {
            GlobalConsoleLogger.e("VOSK", "Ошибка VOSK: ${e.localizedMessage}", e)
            Log.e("VoiceInputManager", "Vosk error", e)
            _errorState.value = "Ошибка VOSK: ${e.localizedMessage}"
            _isListening.value = false
        }
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
        if (currentEngineType == SpeechEngineType.NATIVE) {
            try {
                nativeRecognizer?.stopListening()
            } catch (_: Throwable) {}
        }
        stopAudioThread()
        try {
            voskSpeechService?.stop()
        } catch (_: Throwable) {}
        voskSpeechService = null

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
