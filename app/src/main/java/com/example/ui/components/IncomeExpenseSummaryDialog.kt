package com.example.ui.components

import com.example.ui.components.dialogs.*

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.key
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import com.example.ui.screens.TransactionRowItem
import com.example.ui.screens.getCategoryColorAndIcon
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.lerp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.CategoryEntity
import com.example.data.db.TransactionEntity
import com.example.ui.theme.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.DarkBg
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncomeExpenseSummaryDialog(
    transactions: List<TransactionEntity>,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Calculate totals and category breakdown
    val incomeTransactions = transactions.filter { it.type.lowercase() == "income" }
    val expenseTransactions = transactions.filter { it.type.lowercase() == "expense" }

    val totalIncome = incomeTransactions.sumOf { kotlin.math.abs(it.amount) }
    val totalExpense = expenseTransactions.sumOf { kotlin.math.abs(it.amount) }
    val netBalance = totalIncome - totalExpense

    // Categories Breakdown
    val expenseCategoryTotals = expenseTransactions
        .groupBy { it.category.ifBlank { "Прочее" } }
        .mapValues { entry -> entry.value.sumOf { kotlin.math.abs(it.amount) } }
        .filterValues { it > 0 }

    val incomeCategoryTotals = incomeTransactions
        .groupBy { it.category.ifBlank { "Доходы" } }
        .mapValues { entry -> entry.value.sumOf { kotlin.math.abs(it.amount) } }
        .filterValues { it > 0 }

    val sliceColors = listOf(
        Rose500, Emerald400, Indigo500, Amber400, Sky400,
        Rose500, Emerald400, Indigo500, Slate400, Color(0xFFC084FC)
    )

    data class DoughnutSegment(
        val name: String,
        val amount: Double,
        val isIncome: Boolean,
        val color: Color,
        val percentage: Float
    )

    val grandTotal = totalIncome + totalExpense
    val segments = mutableListOf<DoughnutSegment>()

    var colorIdx = 0
    expenseCategoryTotals.toList().sortedByDescending { it.second }.forEach { (cat, amt) ->
        val pct = if (grandTotal > 0) (amt / grandTotal).toFloat() else 0f
        val color = if (colorIdx == 0) Rose500 else sliceColors[colorIdx % sliceColors.size]
        segments.add(DoughnutSegment(cat, amt, isIncome = false, color = color, percentage = pct))
        colorIdx++
    }

    incomeCategoryTotals.toList().sortedByDescending { it.second }.forEach { (cat, amt) ->
        val pct = if (grandTotal > 0) (amt / grandTotal).toFloat() else 0f
        val color = if (colorIdx == 1) Emerald400 else sliceColors[(colorIdx + 2) % sliceColors.size]
        segments.add(DoughnutSegment(cat, amt, isIncome = true, color = color, percentage = pct))
        colorIdx++
    }

    SwipeToDismissDialog(
        onDismissRequest = onDismiss,
        isAtTop = { scrollState.value == 0 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 12.dp),
                color = DarkBg,
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(20.dp)
                ) {
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Indigo500.copy(alpha = 0.15f))
                                    .border(1.dp, Indigo500.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Indigo500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Сводка доходов и расходов",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Доли категорий и общий расчёт",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Slate900)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Summary Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Slate900.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text("Доходы", color = Slate400, fontSize = 11.sp)
                                    Text(
                                        "+ ${formatFullCurrency(totalIncome)}",
                                        color = Emerald400,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .width(1.dp)
                                        .background(Slate800)
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Баланс", color = Slate400, fontSize = 11.sp)
                                    Text(
                                        "${if (netBalance >= 0) "+" else ""}${formatFullCurrency(netBalance)}",
                                        color = if (netBalance >= 0) Emerald400 else Rose500,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .width(1.dp)
                                        .background(Slate800)
                                )

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Расходы", color = Slate400, fontSize = 11.sp)
                                    Text(
                                        "- ${formatFullCurrency(totalExpense)}",
                                        color = Rose500,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Doughnut Chart Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Slate900.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "КРУГОВАЯ ДИАГРАММА ДОЛЕЙ",
                                    color = Slate300,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.align(Alignment.Start)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Box(
                                    modifier = Modifier.size(190.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        if (segments.isEmpty() || grandTotal <= 0) {
                                            drawArc(
                                                color = Slate800,
                                                startAngle = 0f,
                                                sweepAngle = 360f,
                                                useCenter = false,
                                                style = Stroke(width = 28.dp.toPx())
                                            )
                                        } else {
                                            var currentAngle = -90f
                                            val strokeWidthPx = 28.dp.toPx()
                                            val gapAngle = if (segments.size > 1) 2.5f else 0f

                                            segments.forEach { seg ->
                                                val sweep = (seg.percentage * 360f) - gapAngle
                                                if (sweep > 0f) {
                                                    drawArc(
                                                        color = seg.color,
                                                        startAngle = currentAngle,
                                                        sweepAngle = sweep,
                                                        useCenter = false,
                                                        style = Stroke(
                                                            width = strokeWidthPx,
                                                            cap = StrokeCap.Round
                                                        )
                                                    )
                                                    currentAngle += sweep + gapAngle
                                                }
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Всего средств",
                                            color = Slate400,
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = formatFullCurrency(grandTotal),
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Chart Legend
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    segments.forEach { seg ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier
                                                .background(DarkBg, RoundedCornerShape(8.dp))
                                                .border(1.dp, seg.color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(seg.color)
                                            )
                                            Text(
                                                text = "${seg.name} (${(seg.percentage * 100).toInt()}%)",
                                                color = Slate200,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Detailed Category Lists
                        if (expenseCategoryTotals.isNotEmpty()) {
                            Text(
                                text = "РАСХОДЫ ПО КАТЕГОРИЯМ",
                                color = Rose500,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            expenseCategoryTotals.toList().sortedByDescending { it.second }.forEach { (cat, amt) ->
                                val pct = if (totalExpense > 0) (amt / totalExpense * 100).toInt() else 0
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Slate900.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Rose500)
                                            )
                                            Text(
                                                text = cat,
                                                color = Slate200,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "$pct%",
                                                color = Slate500,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "- ${formatFullCurrency(amt)}",
                                                color = Rose500,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (incomeCategoryTotals.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ДОХОДЫ ПО КАТЕГОРИЯМ",
                                color = Emerald400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            incomeCategoryTotals.toList().sortedByDescending { it.second }.forEach { (cat, amt) ->
                                val pct = if (totalIncome > 0) (amt / totalIncome * 100).toInt() else 0
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Slate900.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Emerald400)
                                            )
                                            Text(
                                                text = cat,
                                                color = Slate200,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "$pct%",
                                                color = Slate500,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "+ ${formatFullCurrency(amt)}",
                                                color = Emerald400,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
