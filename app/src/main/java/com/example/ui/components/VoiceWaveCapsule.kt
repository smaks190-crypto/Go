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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
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
    audioLevel: Float = 0f,
    statusText: String = "Слушаю...",
    amplitudes: List<Float>? = null,
    onClose: () -> Unit = {}
) {
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

                    if (amplitudes != null) {
                        VoiceWaveCanvas(
                            amplitudes = amplitudes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                        )
                    } else {
                        SmoothWaveCanvas(
                            volume = audioLevel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                        )
                    }
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

    val isBelowThreshold = volume < 0.05f

    LaunchedEffect(isBelowThreshold) {
        if (isBelowThreshold) return@LaunchedEffect
        while (isActive) {
            withFrameNanos {
                phase += 0.08f // Скорость бега волны
            }
        }
    }

    // Animation for amplitude transitioning to 0f when below threshold
    val targetVolume = if (isBelowThreshold) 0f else volume
    val animatedVolume by animateFloatAsState(
        targetValue = targetVolume,
        animationSpec = tween(durationMillis = 150),
        label = "volume_animation"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        val wavePath = Path()
        val brush = Brush.horizontalGradient(BorderGradientColors)

        if (animatedVolume == 0f) {
            drawLine(
                brush = brush,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 2.5.dp.toPx()
            )
            return@Canvas
        }

        // Параметры синусоиды
        val waveCount = 3.5f // Всего 3.5 полных волн по всей ширине
        val maxAmplitude = (height / 2.5f) * animatedVolume.coerceIn(0.15f, 1.0f)

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
 * Canvas с отрисовкой живой неоновой волны с реакцией на спектр и громкость звука
 */
@Composable
fun VoiceWaveCanvas(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    sensitivity: Float = 2.2f,
    isActive: Boolean = true
) {
    var phase by remember { mutableFloatStateOf(0f) }

    val rawMaxVolume = if (amplitudes.isNotEmpty()) amplitudes.maxOrNull() ?: 0f else 0f
    val isBelowThreshold = rawMaxVolume < 0.05f

    LaunchedEffect(isBelowThreshold, isActive) {
        if (!isActive || isBelowThreshold) return@LaunchedEffect
        while (isActive) {
            withFrameNanos {
                phase += 0.10f
            }
        }
    }

    val lineGradient = remember {
        Brush.horizontalGradient(BorderGradientColors)
    }

    // Animation for amplitude transitioning to 0f when below threshold
    val targetAmplitudeScale = if (isBelowThreshold) 0f else 1f
    val amplitudeScale by animateFloatAsState(
        targetValue = targetAmplitudeScale,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "wave_amplitude_scale"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        if (amplitudes.isEmpty() || !isActive || amplitudeScale == 0f) {
            drawLine(
                brush = lineGradient,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 2.dp.toPx()
            )
            return@Canvas
        }

        val avgVolume = amplitudes.average().toFloat().coerceIn(0.08f, 1.0f)
        val maxVolume = (amplitudes.maxOrNull() ?: 0.1f).coerceIn(0.08f, 1.0f)

        // Амплитуда колебаний от минимального фонового гула до громкой речи
        val dynamicScale = ((avgVolume * 0.4f + maxVolume * 0.6f) * sensitivity).coerceIn(0.12f, 1.0f)
        val waveAmplitude = (height * 0.44f) * dynamicScale * amplitudeScale

        val path = Path()
        val steps = 64
        val stepX = width / steps

        var prevX = 0f
        var prevY = centerY

        for (i in 0..steps) {
            val x = i * stepX
            val progress = i.toFloat() / steps

            val bandIndex = ((progress * (amplitudes.size - 1)).toInt()).coerceIn(0, amplitudes.size - 1)
            val bandVal = amplitudes[bandIndex]

            // Окно Ханнинга для затухания по бокам
            val window = sin(progress * Math.PI.toFloat())

            val fundamentalAngle = progress * (3.5f * 2.0f * Math.PI.toFloat()) + phase
            val harmonicAngle = progress * (8.0f * 2.0f * Math.PI.toFloat()) - phase * 1.5f

            val fundamental = sin(fundamentalAngle)
            val harmonic = sin(harmonicAngle) * 0.35f * bandVal

            val y = centerY + (fundamental + harmonic) * waveAmplitude * window

            if (i == 0) {
                path.moveTo(x, y)
                prevX = x
                prevY = y
            } else {
                val controlX1 = prevX + (stepX / 2f)
                val controlY1 = prevY
                val controlX2 = prevX + (stepX / 2f)
                val controlY2 = y
                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                prevX = x
                prevY = y
            }
        }

        // 1. Неоновое свечение с эффектом размытия
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3.dp.toPx()
                maskFilter = BlurMaskFilter(7.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                color = android.graphics.Color.parseColor("#6366F1")
            }
            canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
        }

        // 2. Яркая многоцветная градиентная линия поверх
        drawPath(
            path = path,
            brush = lineGradient,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}
