package com.example.ui.components.dialogs

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.CategoryEntity
import com.example.data.db.TransactionEntity
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonCard
import com.example.ui.components.NeonOutlinedButton
import com.example.ui.components.SwipeToDismissDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.PeriodType
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoryLimitsDialog(
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    onUpdateLimit: (categoryName: String, limit: Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var limitInputText by remember { mutableStateOf("") }
    var expandedCategoryName by remember { mutableStateOf<String?>(null) }

    val expenseTransactions = remember(transactions) {
        transactions.filter { it.type == "expense" }
    }

    val categoryTotals = remember(expenseTransactions) {
        expenseTransactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    val allCategoryNames = remember(categories, categoryTotals) {
        (categories.filter { it.type == "expense" }.map { it.name } + categoryTotals.keys)
            .distinct()
            .sortedByDescending { categoryTotals[it] ?: 0.0 }
    }

    val totalSpent = remember(categoryTotals) { categoryTotals.values.sum() }

    SwipeToDismissDialog(
        onDismissRequest = onDismiss,
        contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 12.dp, bottom = 0.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(TextMuted)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonIndigo.copy(alpha = 0.15f))
                                .border(1.dp, NeonIndigo.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = NeonIndigo,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "КАТЕГОРИИ И ЛИМИТЫ",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Траты: ${formatLimitCurrency(totalSpent)}",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(DarkSurfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Inline editor if editing category limit
                editingCategory?.let { cat ->
                    NeonCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        borderColor = NeonIndigo
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "ЛИМИТ ДЛЯ: ${cat.name.uppercase(Locale.getDefault())}",
                                color = NeonIndigo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = limitInputText,
                                onValueChange = { limitInputText = it.replace(',', '.') },
                                label = { Text("Лимит трат (₽)", color = TextSecondary) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground,
                                    focusedBorderColor = NeonIndigo,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        onUpdateLimit(cat.name, null)
                                        editingCategory = null
                                    }
                                ) {
                                    Text("Сбросить", color = NeonRose, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                NeonOutlinedButton(
                                    text = "Отмена",
                                    onClick = { editingCategory = null }
                                )

                                NeonButton(
                                    text = "Сохранить",
                                    onClick = {
                                        val valLimit = limitInputText.toDoubleOrNull()
                                        onUpdateLimit(cat.name, valLimit)
                                        editingCategory = null
                                    },
                                    containerColor = NeonIndigo
                                )
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allCategoryNames) { catName ->
                        val catEntity = categories.find { it.name == catName }
                            ?: CategoryEntity(id = java.util.UUID.randomUUID().toString(), name = catName, type = "expense")
                        val spent = categoryTotals[catName] ?: 0.0
                        val limit = catEntity.monthlyLimit
                        val isExpanded = expandedCategoryName == catName
                        val categoryTxs = expenseTransactions.filter { it.category == catName }

                        CategoryLimitItemCard(
                            categoryName = catName,
                            spent = spent,
                            limit = limit,
                            isExpanded = isExpanded,
                            transactions = categoryTxs,
                            onToggleExpand = {
                                expandedCategoryName = if (isExpanded) null else catName
                            },
                            onEditLimitClick = {
                                editingCategory = catEntity
                                limitInputText = limit?.toString() ?: ""
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryLimitItemCard(
    categoryName: String,
    spent: Double,
    limit: Double?,
    isExpanded: Boolean,
    transactions: List<TransactionEntity>,
    onToggleExpand: () -> Unit,
    onEditLimitClick: () -> Unit
) {
    val isOverLimit = limit != null && limit > 0 && spent > limit
    val progress = if (limit != null && limit > 0) (spent / limit).coerceIn(0.0, 1.0).toFloat() else 0f

    val categoryColor = when {
        isOverLimit -> NeonRose
        progress > 0.8f -> NeonIndigo
        else -> NeonGreen
    }

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "arrowRotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(
            1.dp,
            if (isOverLimit) NeonRose.copy(alpha = 0.5f) else DarkBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleExpand() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, categoryColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = categoryName,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isOverLimit) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Превышение",
                                    tint = NeonRose,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = if (limit != null && limit > 0) {
                                "из ${formatLimitCurrency(limit)}"
                            } else {
                                "Лимит не задан"
                            },
                            color = if (isOverLimit) NeonRose else TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatLimitCurrency(spent),
                        color = if (isOverLimit) NeonRose else NeonGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onEditLimitClick,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Задать лимит",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Развернуть",
                            tint = NeonIndigo,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(rotationState)
                        )
                    }
                }
            }

            if (limit != null && limit > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(DarkBackground)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barWidth = size.width * animatedProgress
                        val corner = 3.dp.toPx()

                        drawRoundRect(
                            color = categoryColor.copy(alpha = 0.4f),
                            size = Size(barWidth, size.height),
                            cornerRadius = CornerRadius(corner, corner)
                        )
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(NeonGreen, categoryColor)
                            ),
                            size = Size(barWidth, size.height),
                            cornerRadius = CornerRadius(corner, corner)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(250)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(DarkBackground, RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Операции (${transactions.size})",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (transactions.isEmpty()) {
                        Text(
                            text = "Нет операций в выбранном периоде",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    } else {
                        transactions.take(5).forEach { tx ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tx.subcategory.ifBlank { tx.category },
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = formatLimitCurrency(tx.amount),
                                    color = NeonRose,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetCategoryLimitDialog(
    categoryName: String,
    currentLimit: Double?,
    onSaveLimit: (Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var limitInput by remember { mutableStateOf(currentLimit?.toString() ?: "") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "ЛИМИТ: ${categoryName.uppercase(Locale.getDefault())}",
                    color = NeonIndigo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it.replace(',', '.') },
                    label = { Text("Сумма лимита (₽)", color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentLimit != null) {
                        TextButton(
                            onClick = {
                                onSaveLimit(null)
                                onDismiss()
                            }
                        ) {
                            Text("Удалить лимит", color = NeonRose, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    NeonOutlinedButton(
                        text = "Отмена",
                        onClick = onDismiss
                    )

                    NeonButton(
                        text = "Сохранить",
                        onClick = {
                            val newLim = limitInput.toDoubleOrNull()
                            if (newLim != null && newLim <= 0) {
                                Toast.makeText(context, "Укажите значение > 0", Toast.LENGTH_SHORT).show()
                                return@NeonButton
                            }
                            onSaveLimit(newLim)
                            onDismiss()
                        },
                        containerColor = NeonIndigo
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetPeriodDialog(
    currentPeriodType: PeriodType,
    onSelectPeriod: (PeriodType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "БЮДЖЕТНЫЙ ПЕРИОД",
                    color = NeonIndigo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                val periods = listOf(
                    PeriodType.MONTH to "Месячный период",
                    PeriodType.ALL to "За всё время",
                    PeriodType.DAY to "За выбранный день"
                )

                periods.forEach { (type, title) ->
                    val isSelected = currentPeriodType == type
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectPeriod(type)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) NeonIndigo.copy(alpha = 0.15f) else DarkBackground
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) NeonIndigo else DarkBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) NeonIndigo else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeonIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatLimitCurrency(amount: Double): String {
    return String.format(Locale("ru", "RU"), "%,.0f ₽", amount).replace(',', ' ')
}
