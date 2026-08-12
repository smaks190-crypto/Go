package com.example.ui.components.dialogs.transactions

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBg
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800

@Composable
fun TransactionPeriodSelector(
    selectedPeriod: String,
    onPeriodSelect: (String) -> Unit,
    chartViewMode: String,
    onChartViewModeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .width(180.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkBg)
                .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                .padding(2.dp)
        ) {
            val barWidth = maxWidth
            val tabCount = 3
            val tabWidth = barWidth / tabCount

            val periodsList = listOf("week", "month", "year")
            val selectedIndex = periodsList.indexOf(
                if (selectedPeriod == "all") "month" else selectedPeriod
            ).coerceAtLeast(0)

            val animatedFraction by animateFloatAsState(
                targetValue = selectedIndex.toFloat(),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "period_tab_fraction"
            )

            Box(
                modifier = Modifier
                    .width(tabWidth)
                    .fillMaxHeight()
                    .offset(x = tabWidth * animatedFraction)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(10.dp),
                        ambientColor = Indigo500,
                        spotColor = Indigo500
                    )
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Indigo500, Indigo500.copy(alpha = 0.8f))
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Indigo500.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp)
                    )
            )

            Row(modifier = Modifier.fillMaxSize()) {
                listOf("week" to "Нед", "month" to "Мес", "year" to "Год").forEach { (pKey, pText) ->
                    val isSelected = selectedPeriod == pKey || (selectedPeriod == "all" && pKey == "month")
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPeriodSelect(pKey) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pText,
                            color = if (isSelected) Color.White else Slate400,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DarkBg)
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (chartViewMode == "donut") Slate800 else Color.Transparent)
                    .clickable { onChartViewModeChange("donut") }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text("🔄", fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (chartViewMode == "bar") Slate800 else Color.Transparent)
                    .clickable { onChartViewModeChange("bar") }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text("📊", fontSize = 11.sp)
            }
        }
    }
}
