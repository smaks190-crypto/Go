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
// 🌿 CLEAN WHITE & ELECTRIC MINT DESIGN TOKENS
// ==========================================

val CyberBg = CleanWhiteBg                  // Clean White Canvas
val CyberBgAlt = CleanWhiteSurfaceVariant   // Soft Slate-100 container
val CyberCardBg = CleanWhiteSurface         // Pure White Card Fill
val CyberCardBgSemi = CleanWhiteSurface     // Pure White Surface
val CyberCardBorder = CleanWhiteBorder      // Subtle Divider Border
val CyberCardBorderGlow = MintGlow          // Mint soft border accent

// Primary Mint Accents
val CyberEmerald = MintElectric             // Fresh Electric Mint #00DC82
val CyberIndigo = MintDark                  // Deep Emerald Green #059669
val CyberRose = NordicCoral                 // Crisp Coral Red #F43F5E
val CyberAmber = NordicAmber                // Warm Amber #F59E0B
val CyberSky = NordicSky                    // Crisp Ocean Blue #0284C7

// Smooth Nordic Modern Gradient
val CyberGradient135 = Brush.linearGradient(
    colors = listOf(MintElectric, MintDark, NordicBlue),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

val CyberCardGradient = Brush.linearGradient(
    colors = listOf(
        CleanWhiteSurface,
        CleanWhiteSurfaceVariant
    ),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

val CyberGlowIndigo = MintGlow
val CyberGlowEmerald = MintGlow
val CyberGlowRose = NordicCoral.copy(alpha = 0.15f)

// ==========================================
// 💡 MODIFIERS & SHADOW HELPERS
// ==========================================

/**
 * Renders a crisp soft shadow or mint accent glow
 */
fun Modifier.neonGlow(
    color: Color = MintElectric,
    radius: Dp = 12.dp,
    alpha: Float = 0.25f,
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
            2f,
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
// 🔲 CLEAN FINTECH CARD COMPONENT
// ==========================================

@Composable
fun NeonGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = CleanWhiteBorder,
    glowColor: Color? = null,
    backgroundColor: Color = CleanWhiteSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val glowMod = if (glowColor != null) {
        Modifier.neonGlow(color = glowColor, radius = 10.dp, alpha = 0.2f, shape = shape)
    } else Modifier

    Surface(
        modifier = modifier
            .then(glowMod)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        shape = shape,
        color = backgroundColor,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        content()
    }
}

// ==========================================
// 🔘 ELECTRIC MINT CTA BUTTON
// ==========================================

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.horizontalGradient(listOf(MintDark, MintElectric)),
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onClick() }
            .then(
                if (enabled) Modifier.neonGlow(MintElectric, radius = 12.dp, alpha = 0.25f) else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, if (enabled) MintElectric.copy(alpha = 0.6f) else CleanWhiteBorder)
    ) {
        Box(
            modifier = Modifier
                .background(if (enabled) gradient else Brush.linearGradient(listOf(CleanWhiteSurfaceVariant, CleanWhiteBorder)))
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
                    color = if (enabled) Color.White else TextMutedDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.3.sp
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
    color: Color = TextPrimaryDark,
    fontSize: androidx.compose.ui.unit.TextUnit = 18.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    glow: Boolean = false,
    glowColor: Color = MintElectric
) {
    Text(
        text = amountText,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = FontFamily.Monospace,
        letterSpacing = (-0.5).sp,
        modifier = modifier.then(
            if (glow) Modifier.neonGlow(color = glowColor, radius = 8.dp, alpha = 0.15f) else Modifier
        )
    )
}

