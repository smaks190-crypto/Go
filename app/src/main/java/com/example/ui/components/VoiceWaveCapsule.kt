package com.example.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

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
