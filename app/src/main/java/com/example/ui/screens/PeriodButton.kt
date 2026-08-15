package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun PeriodButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .then(
                if (isSelected) {
                    Modifier.neonGlow(
                        color = MintElectric,
                        radius = 8.dp,
                        alpha = 0.25f,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Brush.horizontalGradient(listOf(MintDark, MintElectric))
                else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MintElectric.copy(alpha = 0.7f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else TextSecondaryDark,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}


