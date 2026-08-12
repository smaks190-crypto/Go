package com.example.ui.components.dialogs.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.formatFullCurrency
import com.example.ui.theme.DarkBg
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionPillCategoryList(
    categoryTotalsMap: List<Pair<String, Double>>,
    totalCategorySum: Double,
    selectedCategoryFilter: String?,
    isDrilledDownToMixed: Boolean,
    shouldGroup: Boolean,
    remainingCategoryNames: List<String>,
    remainingSum: Double,
    getCategoryColor: (String) -> Color,
    onCategoryToggle: (String?) -> Unit,
    onDrillBack: () -> Unit,
    onOpenMixedDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 140.dp)
            .verticalScroll(rememberScrollState())
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isDrilledDownToMixed) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkBg)
                        .border(1.dp, Indigo500.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .clickable { onDrillBack() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "← Назад",
                        color = Indigo500,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val remainingCategories = categoryTotalsMap.drop(2)
                remainingCategories.forEach { (catName, remainingSumAmt) ->
                    val isCatSelected = selectedCategoryFilter == catName
                    val pillColor = getCategoryColor(catName)
                    val pct = if (totalCategorySum > 0) {
                        ((remainingSumAmt / totalCategorySum) * 100).let { kotlin.math.round(it).toInt() }
                    } else 0

                    Row(
                        modifier = Modifier
                            .then(
                                if (isCatSelected) {
                                    Modifier.shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        ambientColor = pillColor,
                                        spotColor = pillColor
                                    )
                                } else Modifier
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isCatSelected) pillColor else DarkBg)
                            .border(
                                1.dp,
                                if (isCatSelected) pillColor else Slate800,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                onCategoryToggle(if (isCatSelected) null else catName)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(pillColor)
                        )
                        Text(
                            text = if (pct > 0) "$catName $pct%" else catName,
                            color = if (isCatSelected) Color.White else Slate300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatFullCurrency(remainingSumAmt),
                            color = if (isCatSelected) Color.White else Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                val visibleCategories = if (shouldGroup) {
                    categoryTotalsMap.take(2)
                } else {
                    categoryTotalsMap
                }

                visibleCategories.forEach { (catName, sumAmt) ->
                    val isCatSelected = selectedCategoryFilter == catName
                    val pillColor = getCategoryColor(catName)
                    val pct = if (totalCategorySum > 0) {
                        ((sumAmt / totalCategorySum) * 100).let { kotlin.math.round(it).toInt() }
                    } else 0

                    Row(
                        modifier = Modifier
                            .then(
                                if (isCatSelected) {
                                    Modifier.shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        ambientColor = pillColor,
                                        spotColor = pillColor
                                    )
                                } else Modifier
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isCatSelected) pillColor else DarkBg)
                            .border(
                                1.dp,
                                if (isCatSelected) pillColor else Slate800,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                onCategoryToggle(if (isCatSelected) null else catName)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(pillColor)
                        )
                        Text(
                            text = if (pct > 0) "$catName $pct%" else catName,
                            color = if (isCatSelected) Color.White else Slate300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatFullCurrency(sumAmt),
                            color = if (isCatSelected) Color.White else Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (shouldGroup) {
                    val mixedLabel = if (remainingCategoryNames.size <= 1) {
                        "✨ ${remainingCategoryNames.firstOrNull() ?: "Прочие"}"
                    } else "✨ Смешанные (+${remainingCategoryNames.size})"

                    val isMixedActive = remainingCategoryNames.contains(selectedCategoryFilter)

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isMixedActive) Indigo500 else DarkBg)
                            .border(
                                1.dp,
                                if (isMixedActive) Indigo500 else Indigo500.copy(alpha = 0.5f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onOpenMixedDialog() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = mixedLabel,
                            color = if (isMixedActive) Color.White else Indigo500,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatFullCurrency(remainingSum),
                            color = if (isMixedActive) Color.White else Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
