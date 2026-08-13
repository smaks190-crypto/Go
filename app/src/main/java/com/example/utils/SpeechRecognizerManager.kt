package com.example.utils

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

/**
 * Перечисление поддерживаемых движков распознавания речи (STT)
 */
enum class SpeechEngineType(val displayName: String, val description: String) {
    GEMINI_LIVE("Gemini Live API", "Прямая потоковая передача PCM через WebSocket")
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
 * Реализация на базе Gemini Multimodal Live API via WebSocket
 */
class GeminiLiveSpeechRecognizer(
    private val context: Context,
    private val voiceInputManager: VoiceInputManager
) : SpeechRecognizerManager {

    override val engineType: SpeechEngineType = SpeechEngineType.GEMINI_LIVE
    override val isListening: StateFlow<Boolean> = voiceInputManager.isListening

    override fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        voiceInputManager.onChunkRecognized = { text -> onResult(text) }
        voiceInputManager.onErrorCallback = { onError("Ошибка Gemini Live Stream") }
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
 * Фабрика для создания активного менеджера распознавания речи
 */
class SpeechRecognizerFactory(
    private val context: Context,
    private val voiceInputManager: VoiceInputManager
) {
    fun createEngine(type: SpeechEngineType = SpeechEngineType.GEMINI_LIVE): SpeechRecognizerManager {
        return GeminiLiveSpeechRecognizer(context, voiceInputManager)
    }
}
