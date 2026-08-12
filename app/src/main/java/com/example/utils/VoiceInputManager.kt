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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener as VoskRecognitionListener
import org.vosk.android.SpeechService
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class VoiceInputManager(private val context: Context) {
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

    fun startListening(callerContext: Context) {
        GlobalConsoleLogger.i("VOICE", "Запуск непрерывного прослушивания микрофона...")
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

        val targetDir = File(context.filesDir, "vosk-model-small-ru-0.22")
        if (targetDir.exists() && targetDir.isDirectory && targetDir.list()?.isNotEmpty() == true) {
            _voskStatus.value = "READY"
            GlobalConsoleLogger.i("VOSK", "Найдена локальная офлайн-модель VOSK")
            initVoskAndStart(callerContext)
        } else {
            GlobalConsoleLogger.i("VOSK", "Модель VOSK не найдена локально, запускаем загрузку")
            downloadAndInitModel(callerContext)
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
}
