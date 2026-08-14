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
        shape = RoundedCornerShape(22.dp),
        borderColor = CyberIndigo.copy(alpha = 0.4f),
        glowColor = CyberIndigo.copy(alpha = 0.25f),
        backgroundColor = CyberCardBg
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CyberCardBg,
                            CyberBgAlt.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ТЕКУЩИЙ БАЛАНС",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Surface(
                        shape = CircleShape,
                        color = CyberEmerald.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = CyberEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Норма ${monthSavingsRate}%",
                                color = CyberEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                NeonFinancialText(
                    amountText = formatFullCurrency(monthTotalAccumulatedBalance),
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    glow = true,
                    glowColor = if (monthTotalAccumulatedBalance >= 0) CyberIndigo else CyberRose
                )

                Spacer(modifier = Modifier.height(18.dp))

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
                            .background(CyberEmerald.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .border(1.dp, CyberEmerald.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .neonGlow(CyberEmerald, radius = 8.dp, alpha = 0.3f, shape = CircleShape)
                                .clip(CircleShape)
                                .background(CyberEmerald.copy(alpha = 0.15f))
                                .border(1.dp, CyberEmerald.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SouthWest,
                                contentDescription = null,
                                tint = CyberEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Доходы", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = "+\u00A0${formatFullCurrency(monthTotalIncome)}",
                                color = CyberEmerald,
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
                            .background(CyberRose.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .border(1.dp, CyberRose.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .neonGlow(CyberRose, radius = 8.dp, alpha = 0.3f, shape = CircleShape)
                                .clip(CircleShape)
                                .background(CyberRose.copy(alpha = 0.15f))
                                .border(1.dp, CyberRose.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NorthEast,
                                contentDescription = null,
                                tint = CyberRose,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Расходы", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = "-\u00A0${formatFullCurrency(monthTotalExpense)}",
                                color = CyberRose,
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

