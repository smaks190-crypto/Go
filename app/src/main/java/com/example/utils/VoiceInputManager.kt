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
            _isListening.value = true
            if (nativeRecognizer == null) {
                nativeRecognizer = NativeSpeechRecognizer(callerContext)
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
            return
        }

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
