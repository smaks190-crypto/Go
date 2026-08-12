package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BlurMaskFilter
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sin

private val CapsuleBgColor = Color(0xF20B0F19)
private val StatusTextColor = Color(0xFFF43F5E)
private val BorderGradientColors = listOf(
    Color(0xFF10B981),
    Color(0xFF6366F1),
    Color(0xFFA855F7),
    Color(0xFFF43F5E)
)

@Composable
fun VoiceWaveCapsule(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentVolume by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Слушаю...") }

    LaunchedEffect(isVisible) {
        if (!isVisible) return@LaunchedEffect

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            statusText = "Слушаю..."
            startAudioRecording { volume ->
                currentVolume = volume
            }
        } else {
            statusText = "Нет разрешения на микрофон"
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(BorderGradientColors),
                        shape = CircleShape
                    )
                    .padding(1.5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(CapsuleBgColor)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = statusText,
                        color = StatusTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    SmoothWaveCanvas(
                        volume = currentVolume,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CapsuleBgColor)
                    .border(1.dp, Color(0xFF1E293B), CircleShape)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = StatusTextColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SmoothWaveCanvas(
    volume: Float,
    modifier: Modifier = Modifier
) {
    // Бесконечная фаза для плавного движения волны
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos {
                phase += 0.08f // Скорость бега волны
            }
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        val wavePath = Path()
        val brush = Brush.horizontalGradient(BorderGradientColors)

        // Параметры синусоиды
        val waveCount = 3.5f // Всего 3.5 полных волн по всей ширине (без мелкой расчески)
        val maxAmplitude = (height / 2.5f) * volume.coerceIn(0.15f, 1.0f)

        val steps = 60 // Достаточно точек для идеальной гладкости
        val stepX = width / steps

        wavePath.moveTo(0f, centerY)

        var prevX = 0f
        var prevY = centerY + sin(phase) * maxAmplitude

        for (i in 1..steps) {
            val x = i * stepX
            val angle = (i.toFloat() / steps) * (waveCount * 2 * Math.PI.toFloat()) + phase
            
            // Края волны приглушаем (окно Хэнна), чтобы по краям капсулы она сходила на нет
            val edgeFade = sin((i.toFloat() / steps) * Math.PI.toFloat())
            val y = centerY + sin(angle) * maxAmplitude * edgeFade

            // Кубическая интерполяция для идеальной гладкости
            val controlX1 = prevX + (stepX / 2f)
            val controlY1 = prevY
            val controlX2 = prevX + (stepX / 2f)
            val controlY2 = y

            wavePath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)

            prevX = x
            prevY = y
        }

        // 1. Отрисовка мягкого свечения (Glow)
        drawIntoCanvas { canvas ->
            val nativePaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 2.5.dp.toPx()
                setShadowLayer(
                    10.dp.toPx(),
                    0f,
                    0f,
                    android.graphics.Color.parseColor("#6366F1")
                )
            }
            canvas.nativeCanvas.drawPath(wavePath.asAndroidPath(), nativePaint)
        }

        // 2. Отрисовка основной линии волны
        drawPath(
            path = wavePath,
            brush = brush,
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}

private suspend fun startAudioRecording(
    onVolumeChange: (Float) -> Unit
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

        val buffer = ShortArray(minBufferSize / 2)
        audioRecord.startRecording()

        var smoothedVolume = 0.1f

        while (isActive) {
            val readSize = audioRecord.read(buffer, 0, buffer.size)
            if (readSize > 0) {
                var maxVal = 0
                for (i in 0 until readSize) {
                    val absVal = abs(buffer[i].toInt())
                    if (absVal > maxVal) maxVal = absVal
                }

                val rawVolume = (maxVal / 32768f) * 2.2f
                // Экспоненциальное сглаживание громкости (чтобы волна не прыгала дергано)
                smoothedVolume += (rawVolume - smoothedVolume) * 0.15f

                withContext(Dispatchers.Main) {
                    onVolumeChange(smoothedVolume)
                }
            }
        }

        audioRecord.stop()
        audioRecord.release()
    } catch (e: Exception) {
        e.printStackTrace()
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

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        if (amplitudes.isEmpty() || !isActive) {
            // Отрисовываем плоскую линию, если запись не активна
            drawLine(
                brush = lineGradient,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 2.dp.toPx()
            )
            return@Canvas
        }

        val path = Path()
        val count = amplitudes.size
        val stepX = width / (count - 1).coerceAtLeast(1)

        // Рисуем первую точку
        val firstY = centerY + (amplitudes[0] * centerY * sensitivity * 0.45f)
        path.moveTo(0f, firstY)

        // Строим плавные кривые Безье между точками амплитуд
        for (i in 0 until count - 1) {
            val x1 = i * stepX
            val y1 = centerY + (amplitudes[i] * centerY * sensitivity * 0.45f)
            val x2 = (i + 1) * stepX
            val y2 = centerY + (amplitudes[i + 1] * centerY * sensitivity * 0.45f)

            val controlX = (x1 + x2) / 2f
            path.quadraticTo(x1, y1, controlX, (y1 + y2) / 2f)
        }

        // 1. Рисуем неоновое свечение (Glow) за счет nativeCanvas и размытия
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3.dp.toPx()
                // Размытие краев для создания неонового свечения
                maskFilter = BlurMaskFilter(6.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                // Свечение цвета индиго/пурпурный
                color = android.graphics.Color.parseColor("#6366F1")
            }
            canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
        }

        // 2. Рисуем саму четкую линию волны сверху
        drawPath(
            path = path,
            brush = lineGradient,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}
