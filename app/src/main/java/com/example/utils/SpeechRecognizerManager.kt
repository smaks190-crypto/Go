package com.example.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Перечисление поддерживаемых движков распознавания речи (STT)
 */
enum class SpeechEngineType(val displayName: String, val description: String) {
    SHERPA_ONNX("Sherpa-Onnx", "Рекомендуемый, быстрый"),
    VOSK("VOSK", "Автономный"),
    WHISPER("Whisper", "Высокое качество"),
    NATIVE("Системный Android", "Без загрузки моделей")
}

/**
 * Унифицированный интерфейс управления распознаванием речи
 */
interface SpeechRecognizerManager {
    val engineType: SpeechEngineType
    val isListening: StateFlow<Boolean>

    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit)
    fun stopListening()
    fun release()
}

/**
 * Реализация на базе автономного движка VOSK
 */
class VoskSpeechRecognizer(
    private val context: Context,
    private val voiceInputManager: VoiceInputManager
) : SpeechRecognizerManager {

    override val engineType: SpeechEngineType = SpeechEngineType.VOSK
    override val isListening: StateFlow<Boolean> = voiceInputManager.isListening

    override fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        voiceInputManager.onChunkRecognized = { text -> onResult(text) }
        voiceInputManager.onErrorCallback = { onError("Ошибка VOSK") }
        voiceInputManager.startListening(context)
    }

    override fun stopListening() {
        voiceInputManager.stopListening()
    }

    override fun release() {
        voiceInputManager.stopListening()
    }
}

/**
 * Реализация на базе локального движка Sherpa-Onnx (ONNX Runtime)
 */
class SherpaOnnxSpeechRecognizer(
    private val context: Context,
    private val voiceInputManager: VoiceInputManager
) : SpeechRecognizerManager {

    override val engineType: SpeechEngineType = SpeechEngineType.SHERPA_ONNX
    override val isListening: StateFlow<Boolean> = voiceInputManager.isListening

    override fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        voiceInputManager.onChunkRecognized = { text -> onResult(text) }
        voiceInputManager.onErrorCallback = { onError("Ошибка Sherpa-Onnx") }
        voiceInputManager.startListening(context)
    }

    override fun stopListening() {
        voiceInputManager.stopListening()
    }

    override fun release() {
        voiceInputManager.stopListening()
    }
}

/**
 * Реализация на базе нейросетевого движка Whisper
 */
class WhisperSpeechRecognizer(
    private val context: Context,
    private val voiceInputManager: VoiceInputManager
) : SpeechRecognizerManager {

    override val engineType: SpeechEngineType = SpeechEngineType.WHISPER
    override val isListening: StateFlow<Boolean> = voiceInputManager.isListening

    override fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        voiceInputManager.onChunkRecognized = { text -> onResult(text) }
        voiceInputManager.onErrorCallback = { onError("Ошибка Whisper") }
        voiceInputManager.startListening(context)
    }

    override fun stopListening() {
        voiceInputManager.stopListening()
    }

    override fun release() {
        voiceInputManager.stopListening()
    }
}

/**
 * Реализация на базе системного Android SpeechRecognizer
 */
class NativeSpeechRecognizer(
    private val context: Context,
    private val voiceInputManager: VoiceInputManager? = null
) : SpeechRecognizerManager {

    override val engineType: SpeechEngineType = SpeechEngineType.NATIVE

    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    override fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Системное распознавание речи недоступно на этом устройстве")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 10f).coerceIn(0f, 1f)
                        voiceInputManager?.updateSimulatedFrequencies(normalized)
                    }
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        val errorMessage = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "Речь не распознана"
                            SpeechRecognizer.ERROR_NETWORK -> "Ошибка сети"
                            SpeechRecognizer.ERROR_AUDIO -> "Ошибка записи аудио"
                            else -> "Ошибка распознавания (код $error)"
                        }
                        onError(errorMessage)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onResult(matches[0])
                        } else {
                            onError("Пустой результат")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onResult(matches[0])
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            Log.e("NativeSpeechRecognizer", "Error starting speech recognizer", e)
            onError(e.localizedMessage ?: "Ошибка инициализации системного распознавания")
        }
    }

    override fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("NativeSpeechRecognizer", "Error stopping speech recognizer", e)
        } finally {
            _isListening.value = false
        }
    }

    override fun release() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e("NativeSpeechRecognizer", "Error destroying speech recognizer", e)
        } finally {
            _isListening.value = false
        }
    }
}

/**
 * Фабрика для создания активного менеджера распознавания речи
 */
class SpeechRecognizerFactory(
    private val context: Context,
    private val voiceInputManager: VoiceInputManager
) {
    fun createEngine(type: SpeechEngineType): SpeechRecognizerManager {
        return when (type) {
            SpeechEngineType.VOSK -> VoskSpeechRecognizer(context, voiceInputManager)
            SpeechEngineType.SHERPA_ONNX -> SherpaOnnxSpeechRecognizer(context, voiceInputManager)
            SpeechEngineType.WHISPER -> WhisperSpeechRecognizer(context, voiceInputManager)
            SpeechEngineType.NATIVE -> NativeSpeechRecognizer(context, voiceInputManager)
        }
    }
}
