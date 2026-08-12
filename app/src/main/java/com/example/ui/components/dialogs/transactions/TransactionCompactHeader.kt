package com.example.ui.components.dialogs.transactions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.formatFullCurrency
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun TransactionCompactHeader(
    filterType: String,
    totalExpenseAmt: Double,
    totalIncomeAmt: Double,
    currentActiveCategoryTotals: List<Pair<String, Double>>,
    selectedCategoryFilter: String?,
    isDrilledDownToMixed: Boolean,
    remainingCategoryNames: List<String>,
    getCategoryColor: (String) -> Color,
    onResetFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = filterType == "expense"
    val accentColor = if (isExpense) Rose500 else Emerald400
    val activeAmount = if (isExpense) totalExpenseAmt else totalIncomeAmt
    val titleText = if (isExpense) "Траты" else "Доходы"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Slate900)
            .border(1.dp, Slate800, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Canvas(modifier = Modifier.size(36.dp)) {
                val strokeWidth = 5.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                if (currentActiveCategoryTotals.isEmpty() || currentActiveCategoryTotals.sumOf { it.second } <= 0) {
                    drawCircle(
                        color = Slate800,
                        radius = radius,
                        center = centerOffset,
                        style = Stroke(width = strokeWidth)
                    )
                } else {
                    val sumAll = currentActiveCategoryTotals.sumOf { it.second }
                    var startAngle = -90f

                    currentActiveCategoryTotals.forEachIndexed { _, (catName, amt) ->
                        val sweepAngle = ((amt / sumAll) * 360f).toFloat()
                        val col = getCategoryColor(catName)
                        drawArc(
                            color = col,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 2f,
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }
            }

            Column {
                Text(
                    text = titleText,
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatFullCurrency(activeAmount),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        val activeFilterText = when {
            !selectedCategoryFilter.isNullOrBlank() -> selectedCategoryFilter
            isDrilledDownToMixed -> {
                if (remainingCategoryNames.size <= 1) "✨ Прочие" else "✨ Смешанные"
            }
            else -> null
        }

        if (!activeFilterText.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = activeFilterText,
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        IconButton(
            onClick = onResetFilter,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Slate800)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "К всем",
                tint = Slate400,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
