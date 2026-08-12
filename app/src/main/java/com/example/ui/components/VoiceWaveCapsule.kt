package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BlurMaskFilter
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * Цветовые константы под дизайн приложения
 */
private val CapsuleBgColor = Color(0xF20B0F19) // #0B0F19 с прозрачностью 95%
private val StatusTextColor = Color(0xFFFB7185) // Rose 400
private val BorderGradientColors = listOf(
    Color(0xFF10B981), // Emerald
    Color(0xFF6366F1), // Indigo
    Color(0xFFA855F7), // Purple
    Color(0xFFF43F5E)  // Rose
)

/**
 * Основной Composable голосовой капсулы
 *
 * @param modifier Модификатор внешней разметки
 * @param isVisible Флаг видимости капсулы
 * @param sensitivity Чувствительность колебания волны
 * @param statusTextCustom Дополнительный текст статуса (например, при распознавании)
 * @param externalAmplitudes Внешние амплитуды (если запись ведет сторонний менеджер)
 * @param onClose Callback при нажатии на кнопку закрытия
 */
@Composable
fun VoiceWaveCapsule(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    sensitivity: Float = 1.8f,
    statusTextCustom: String? = null,
    externalAmplitudes: List<Float>? = null,
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    var internalAmplitudes by remember { mutableStateOf(List(64) { 0f }) }
    var statusText by remember { mutableStateOf("Слушаю...") }

    val amplitudesToDisplay = externalAmplitudes ?: internalAmplitudes

    // Проверяем разрешение на запись аудио и запускаем внутренний слушатель микрофона,
    // если не переданы внешние амплитуды
    LaunchedEffect(isVisible, externalAmplitudes) {
        if (!isVisible) return@LaunchedEffect

        if (externalAmplitudes != null) {
            statusText = statusTextCustom ?: "Слушаю..."
            return@LaunchedEffect
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            statusText = statusTextCustom ?: "Слушаю..."
            startAudioRecording { newAmplitudes ->
                internalAmplitudes = newAmplitudes
            }
        } else {
            statusText = "Нет доступа к микрофону"
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Главный капсульный блок с градиентной рамкой
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(BorderGradientColors),
                        shape = CircleShape
                    )
                    .padding(1.5.dp) // Толщина неоновой рамки
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(CapsuleBgColor)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Текст статуса
                    Text(
                        text = statusTextCustom ?: statusText,
                        color = StatusTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Отрисовка неоновой волны на Canvas
                    VoiceWaveCanvas(
                        amplitudes = amplitudesToDisplay,
                        sensitivity = sensitivity,
                        isActive = isVisible,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    )
                }
            }

            // Кнопка закрытия (крестик)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CapsuleBgColor)
                    .border(1.dp, Color(0xFF1E293B), CircleShape)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть капсулу",
                    tint = StatusTextColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Canvas с отрисовкой неоновой волны и плавных кривых Безье
 */
@Composable
fun VoiceWaveCanvas(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    sensitivity: Float = 1.8f,
    isActive: Boolean = true
) {
    // Градиент для линии волны
    val lineGradient = remember {
        Brush.horizontalGradient(BorderGradientColors)
    }

    // Paint для неонового свечения вокруг линии (Blur Effect)
    val glowPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 10f
            color = android.graphics.Color.parseColor("#6366F1")
            maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
        }
    }

    Canvas(modifier = modifier) {
        if (!isActive || amplitudes.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val bufferLength = amplitudes.size
        val sliceWidth = width / (bufferLength - 1).coerceAtLeast(1)

        val path = Path()
        var x = 0f

        for (i in 0 until bufferLength) {
            val value = amplitudes[i] * sensitivity
            val waveHeight = value * (height / 2.2f)
            val direction = if (i % 2 == 0) 1f else -1f
            val y = centerY + (waveHeight * direction)

            if (i == 0) {
                path.moveTo(x, centerY)
            } else {
                val prevX = x - sliceWidth
                val cpX = (prevX + x) / 2f
                // Сглаживание через кривые Безье
                path.quadraticTo(prevX, y, cpX, y)
            }

            x += sliceWidth
        }
        path.lineTo(width, centerY)

        // 1. Отрисовка неонового свечения (Glow)
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawPath(path.asAndroidPath(), glowPaint)
        }

        // 2. Отрисовка основной яркой градиентной линии
        drawPath(
            path = path,
            brush = lineGradient,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

/**
 * Вспомогательная функция для считывания громкости с микрофона с разбиением на 64 спектральные полосы
 */
private suspend fun startAudioRecording(
    onAmplitudeChange: (List<Float>) -> Unit
) = withContext(Dispatchers.IO) {
    val sampleRate = 44100
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    try {
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            minBufferSize
        )

        val audioBuffer = ShortArray(minBufferSize)
        audioRecord.startRecording()

        val binsCount = 64
        val chunkSize = (minBufferSize / binsCount).coerceAtLeast(1)

        while (isActive) {
            val readSize = audioRecord.read(audioBuffer, 0, minBufferSize)
            if (readSize > 0) {
                val newWaveData = MutableList(binsCount) { 0f }
                for (i in 0 until binsCount) {
                    var sum = 0f
                    val start = i * chunkSize
                    val end = (start + chunkSize).coerceAtMost(readSize)
                    for (j in start until end) {
                        sum += abs(audioBuffer[j].toFloat())
                    }
                    val avg = if (end > start) sum / (end - start) else 0f
                    newWaveData[i] = (avg / 32768f).coerceIn(0f, 1f)
                }

                withContext(Dispatchers.Main) {
                    onAmplitudeChange(newWaveData)
                }
            }
        }

        audioRecord.stop()
        audioRecord.release()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
