package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==========================================
// 🌌 CYBERPUNK NEON PALETTE TOKENS
// ==========================================

val CyberBg = Color(0xFF090D16)          // Deepest void background
val CyberBgAlt = Color(0xFF0F172A)       // Secondary page background
val CyberCardBg = Color(0xFF1E293B)      // Glass card fill
val CyberCardBgSemi = Color(0xD91E293B)  // 85% opacity glass fill
val CyberCardBorder = Color(0xFF334155)  // Slate-700 glass border
val CyberCardBorderGlow = Color(0x666366F1) // Soft indigo border glow

// Neon Accents
val CyberEmerald = Color(0xFF34D399)     // Growth, Income, Success
val CyberIndigo = Color(0xFF6366F1)      // Main Balance, Primary Accent
val CyberRose = Color(0xFFF43F5E)        // Expenses, Danger, Alerts
val CyberAmber = Color(0xFFFBBF24)       // Warnings, Limit highlights
val CyberSky = Color(0xFF38BDF8)         // Secondary telemetry & hints

// 135° Master Gradient
val CyberGradient135 = Brush.linearGradient(
    colors = listOf(CyberEmerald, CyberIndigo, CyberRose),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

val CyberCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1E293B),
        Color(0xFF0F172A)
    ),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

val CyberGlowIndigo = CyberIndigo.copy(alpha = 0.35f)
val CyberGlowEmerald = CyberEmerald.copy(alpha = 0.35f)
val CyberGlowRose = CyberRose.copy(alpha = 0.35f)

// ==========================================
// 💡 MODIFIERS & GLOW HELPERS
// ==========================================

/**
 * Renders a soft cyberpunk blurred glow behind the composable
 */
fun Modifier.neonGlow(
    color: Color = CyberIndigo,
    radius: Dp = 16.dp,
    alpha: Float = 0.4f,
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier = this.drawBehind {
    val transparentColor = color.copy(alpha = 0f).toArgb()
    val shadowColor = color.copy(alpha = alpha).toArgb()
    this.drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(
            radius.toPx(),
            0f,
            0f,
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            16.dp.toPx(),
            16.dp.toPx(),
            paint
        )
    }
}

// ==========================================
// 🔲 NEON GLASS CARD COMPONENT
// ==========================================

@Composable
fun NeonGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    borderColor: Color = CyberCardBorder,
    glowColor: Color? = null,
    backgroundColor: Color = CyberCardBgSemi,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val glowMod = if (glowColor != null) {
        Modifier.neonGlow(color = glowColor, radius = 12.dp, alpha = 0.25f, shape = shape)
    } else Modifier

    Surface(
        modifier = modifier
            .then(glowMod)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        content()
    }
}

// ==========================================
// 🔘 NEON CTA BUTTON
// ==========================================

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.horizontalGradient(listOf(CyberIndigo, CyberEmerald)),
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onClick() }
            .then(
                if (enabled) Modifier.neonGlow(CyberIndigo, radius = 14.dp, alpha = 0.35f) else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, if (enabled) CyberEmerald.copy(alpha = 0.8f) else DarkBorder)
    ) {
        Box(
            modifier = Modifier
                .background(if (enabled) gradient else Brush.linearGradient(listOf(DarkSurfaceVariant, DarkSurface)))
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Box(modifier = Modifier.padding(start = 8.dp))
                }
                Text(
                    text = text,
                    color = if (enabled) Color.White else TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ==========================================
// 🔢 MONOSPACE FINANCIAL TEXT COMPONENT
// ==========================================

@Composable
fun NeonFinancialText(
    amountText: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: androidx.compose.ui.unit.TextUnit = 18.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    glow: Boolean = false,
    glowColor: Color = color
) {
    Text(
        text = amountText,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = FontFamily.Monospace,
        letterSpacing = (-0.5).sp,
        modifier = modifier.then(
            if (glow) Modifier.neonGlow(color = glowColor, radius = 10.dp, alpha = 0.3f) else Modifier
        )
    )
}
