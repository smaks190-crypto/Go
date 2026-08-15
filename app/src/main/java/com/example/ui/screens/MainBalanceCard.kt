package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.formatFullCurrency
import com.example.ui.theme.*

@Composable
fun MainBalanceCard(
    monthSavingsRate: Int,
    monthTotalAccumulatedBalance: Double,
    monthTotalIncome: Double,
    monthTotalExpense: Double,
    onIncomesClick: () -> Unit,
    onExpensesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeonGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        borderColor = CleanWhiteBorder,
        glowColor = MintGlow,
        backgroundColor = CleanWhiteSurface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CleanWhiteSurface,
                            CleanWhiteSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ТЕКУЩИЙ БАЛАНС",
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Surface(
                        shape = CircleShape,
                        color = MintLight,
                        border = BorderStroke(1.dp, MintElectric.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MintDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Норма ${monthSavingsRate}%",
                                color = MintDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                NeonFinancialText(
                    amountText = formatFullCurrency(monthTotalAccumulatedBalance),
                    color = TextPrimaryDark,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    glow = false
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Income Pill Button
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onIncomesClick() }
                            .background(MintLight, RoundedCornerShape(16.dp))
                            .border(1.dp, MintElectric.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MintElectric.copy(alpha = 0.2f))
                                .border(1.dp, MintElectric.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SouthWest,
                                contentDescription = null,
                                tint = MintDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Доходы", color = TextSecondaryDark, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = "+\u00A0${formatFullCurrency(monthTotalIncome)}",
                                color = MintDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Expense Pill Button
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onExpensesClick() }
                            .background(NordicCoralLight, RoundedCornerShape(16.dp))
                            .border(1.dp, NordicCoral.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(NordicCoral.copy(alpha = 0.15f))
                                .border(1.dp, NordicCoral.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NorthEast,
                                contentDescription = null,
                                tint = NordicCoral,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Расходы", color = TextSecondaryDark, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = "-\u00A0${formatFullCurrency(monthTotalExpense)}",
                                color = NordicCoral,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}


