package com.example.ui.components.dialogs

import com.example.ui.components.dialogs.transactions.*

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.CategoryEntity
import com.example.data.db.TransactionEntity
import com.example.ui.components.*
import com.example.ui.screens.TransactionRowItem
import com.example.ui.screens.getCategoryColorAndIcon
import com.example.ui.theme.*
import com.example.ui.theme.DarkBg
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllTransactionsDialog(
    transactions: List<TransactionEntity>,
    allTransactions: List<TransactionEntity> = emptyList(),
    onDeleteTransaction: ((String) -> Unit)? = null,
    onEditTransaction: ((TransactionEntity) -> Unit)? = null,
    initialFilterType: String = "all",
    initialDate: String? = null,
    onDismiss: () -> Unit
) {
    var selectedReceiptTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    val fullTxList = remember(transactions, allTransactions) {
        if (allTransactions.isNotEmpty()) allTransactions else transactions
    }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isScrolled by remember {
        androidx.compose.runtime.derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 40
        }
    }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var sortOption by remember { mutableStateOf("date") }
    var filterType by remember { mutableStateOf(initialFilterType) } // "all", "income", "expense"
    var selectedPeriod by remember { mutableStateOf("all") } // "all", "week", "month", "year", "custom"
    var chartViewMode by remember { mutableStateOf("donut") } // "donut", "bar"
    var selectedAccountFilter by remember { mutableStateOf<String?>(null) } // null = all, "Black" etc.
    var excludeTransfers by remember { mutableStateOf(false) }
    var showDatePickerModal by remember { mutableStateOf(false) }
    var isCategoriesExpanded by remember { mutableStateOf(false) }
    var isDrilledDownToMixed by remember { mutableStateOf(false) }
    var isSearchExpandedInPlace by remember { mutableStateOf(false) }
    var showMixedCategoriesDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isScrolled) {
        if (!isScrolled) {
            isSearchExpandedInPlace = false
        }
    }

    LaunchedEffect(isSearchExpandedInPlace) {
        if (isSearchExpandedInPlace) {
            focusRequester.requestFocus()
        }
    }

    // Custom date range bounds
    var customStartStr by remember { mutableStateOf<String?>(null) }
    var customEndStr by remember { mutableStateOf<String?>(null) }
    var customLabelStr by remember { mutableStateOf<String?>(null) }

    // Synchronized current month name from first visible item on scroll
    val visibleDateStr = remember(transactions) {
        androidx.compose.runtime.derivedStateOf {
            if (transactions.isNotEmpty()) {
                val idx = (lazyListState.firstVisibleItemIndex - 4).coerceIn(0, transactions.size - 1)
                transactions.getOrNull(idx)?.date ?: transactions.first().date
            } else "2026-08-01"
        }
    }

    val defaultAnchor = remember(initialDate, transactions) {
        if (!initialDate.isNullOrBlank()) {
            initialDate
        } else if (transactions.isNotEmpty()) {
            transactions.first().date
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }

    var stableAnchorDateStr by remember(defaultAnchor) { mutableStateOf(defaultAnchor) }
    var swipeWeekOffset by remember { mutableStateOf(0) }
    var swipeMonthOffset by remember { mutableStateOf(0) }
    var swipeYearOffset by remember { mutableStateOf(0) }
    var headerDragOffsetY by remember { mutableFloatStateOf(0f) }

    val currentOffsetInt = when (selectedPeriod) {
        "week" -> swipeWeekOffset
        "month" -> swipeMonthOffset
        "year" -> swipeYearOffset
        else -> swipeMonthOffset
    }

    LaunchedEffect(selectedPeriod, filterType) {
        if (stableAnchorDateStr.isBlank()) {
            stableAnchorDateStr = defaultAnchor
        }
        swipeWeekOffset = 0
        swipeMonthOffset = 0
        swipeYearOffset = 0
        isDrilledDownToMixed = false
        selectedCategoryFilter = null
    }

    val dynamicMonthLabel = remember(visibleDateStr.value, stableAnchorDateStr, customLabelStr, selectedPeriod, swipeMonthOffset, swipeWeekOffset, swipeYearOffset) {
        if (!customLabelStr.isNullOrBlank()) {
            customLabelStr!!
        } else {
            val baseDateStr = if (selectedPeriod == "all" || selectedPeriod == "custom") visibleDateStr.value else stableAnchorDateStr
            val anchorDateStr = baseDateStr.ifBlank { "2026-08-01" }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val anchorDate = try { sdf.parse(anchorDateStr) } catch (e: Exception) { null } ?: Date()
            val cal = Calendar.getInstance().apply { time = anchorDate }

            if (selectedPeriod == "week") {
                cal.add(Calendar.WEEK_OF_YEAR, swipeWeekOffset)
                val startCal = (cal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                }
                val endCal = (cal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    add(Calendar.DAY_OF_WEEK, 6)
                }
                val df = SimpleDateFormat("dd.MM", Locale.getDefault())
                "Неделя ${df.format(startCal.time)} - ${df.format(endCal.time)}"
            } else if (selectedPeriod == "year") {
                cal.add(Calendar.YEAR, swipeYearOffset)
                "${cal.get(Calendar.YEAR)} год"
            } else if (selectedPeriod == "month") {
                cal.add(Calendar.MONTH, swipeMonthOffset)
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val monthYear = cal.get(Calendar.YEAR)
                val formatPattern = if (monthYear != currentYear) "LLLL yyyy" else "LLLL"
                val rawStr = SimpleDateFormat(formatPattern, Locale("ru", "RU")).format(cal.time)
                rawStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru", "RU")) else it.toString() }
            } else {
                try {
                    val d = sdf.parse(anchorDateStr)
                    if (d != null) {
                        val parsedCal = Calendar.getInstance().apply { time = d }
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        val parsedYear = parsedCal.get(Calendar.YEAR)
                        val formatPattern = if (parsedYear != currentYear) "LLLL yyyy" else "LLLL"
                        val rawStr = SimpleDateFormat(formatPattern, Locale("ru", "RU")).format(d)
                        rawStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru", "RU")) else it.toString() }
                    } else "Август"
                } catch (e: Exception) {
                    "Август"
                }
            }
        }
    }

    // Filter transactions by period ("week", "month", "year", "custom") relative to visible/selected month
    val periodFilteredList = remember(transactions, selectedPeriod, customStartStr, customEndStr, visibleDateStr.value, stableAnchorDateStr, swipeWeekOffset, swipeMonthOffset, swipeYearOffset) {
        if (selectedPeriod == "custom" && !customStartStr.isNullOrBlank() && !customEndStr.isNullOrBlank()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = try { sdf.parse(customStartStr!!) } catch (e: Exception) { null }
            val endDate = try { sdf.parse(customEndStr!!) } catch (e: Exception) { null }
            transactions.filter { tx ->
                val txDate = try { sdf.parse(tx.date) } catch (e: Exception) { null }
                if (txDate == null) true
                else if (startDate != null && endDate != null) {
                    !txDate.before(startDate) && !txDate.after(endDate)
                } else true
            }
        } else if (selectedPeriod == "week") {
            val anchorDateStr = stableAnchorDateStr.ifBlank { "2026-08-01" }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val anchorDate = try { sdf.parse(anchorDateStr) } catch(e: Exception) { null } ?: Date()
            val cal = Calendar.getInstance().apply { time = anchorDate }
            cal.add(Calendar.WEEK_OF_YEAR, swipeWeekOffset)
            val adjustedDate = cal.time
            transactions.filter { tx ->
                val txDate = try { sdf.parse(tx.date) } catch(e: Exception) { null }
                if (txDate == null) true
                else {
                    val diffMs = kotlin.math.abs(adjustedDate.time - txDate.time)
                    val diffDays = diffMs / (1000 * 60 * 60 * 24)
                    diffDays <= 7
                }
            }
        } else if (selectedPeriod == "month") {
            val anchorDateStr = stableAnchorDateStr.ifBlank { "2026-08-01" }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val anchorDate = try { sdf.parse(anchorDateStr) } catch(e: Exception) { null } ?: Date()
            val cal = Calendar.getInstance().apply { time = anchorDate }
            cal.add(Calendar.MONTH, swipeMonthOffset)
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)
            transactions.filter { tx ->
                val txDate = try { sdf.parse(tx.date) } catch(e: Exception) { null }
                if (txDate == null) true
                else {
                    val txCal = Calendar.getInstance().apply { time = txDate }
                    txCal.get(Calendar.YEAR) == currentYear && txCal.get(Calendar.MONTH) == currentMonth
                }
            }
        } else if (selectedPeriod == "year") {
            val anchorDateStr = stableAnchorDateStr.ifBlank { "2026-08-01" }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val anchorDate = try { sdf.parse(anchorDateStr) } catch(e: Exception) { null } ?: Date()
            val cal = Calendar.getInstance().apply { time = anchorDate }
            cal.add(Calendar.YEAR, swipeYearOffset)
            val currentYear = cal.get(Calendar.YEAR)
            transactions.filter { tx ->
                val txDate = try { sdf.parse(tx.date) } catch(e: Exception) { null }
                if (txDate == null) true
                else {
                    val txCal = Calendar.getInstance().apply { time = txDate }
                    txCal.get(Calendar.YEAR) == currentYear
                }
            }
        } else {
            // "all" - show ALL transactions!
            transactions
        }
    }

    fun getFilteredListForOffset(offset: Int): List<TransactionEntity> {
        val effectivePeriod = if (selectedPeriod == "all" || selectedPeriod == "custom") "month" else selectedPeriod

        val anchorDateStr = if (selectedPeriod == "all" || selectedPeriod == "custom") {
            visibleDateStr.value.ifBlank { "2026-08-01" }
        } else {
            stableAnchorDateStr.ifBlank { "2026-08-01" }
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val anchorDate = try { sdf.parse(anchorDateStr) } catch(e: Exception) { null } ?: Date()
        val cal = Calendar.getInstance().apply { time = anchorDate }

        val baseList = if (selectedPeriod == "custom") periodFilteredList else transactions

        return when (effectivePeriod) {
            "week" -> {
                cal.add(Calendar.WEEK_OF_YEAR, offset)
                val adjustedDate = cal.time
                baseList.filter { tx ->
                    val txDate = try { sdf.parse(tx.date) } catch(e: Exception) { null }
                    if (txDate == null) true
                    else {
                        val diffMs = kotlin.math.abs(adjustedDate.time - txDate.time)
                        val diffDays = diffMs / (1000 * 60 * 60 * 24)
                        diffDays <= 7
                    }
                }
            }
            "month" -> {
                cal.add(Calendar.MONTH, offset)
                val currentYear = cal.get(Calendar.YEAR)
                val currentMonth = cal.get(Calendar.MONTH)
                baseList.filter { tx ->
                    val txDate = try { sdf.parse(tx.date) } catch(e: Exception) { null }
                    if (txDate == null) true
                    else {
                        val txCal = Calendar.getInstance().apply { time = txDate }
                        txCal.get(Calendar.YEAR) == currentYear && txCal.get(Calendar.MONTH) == currentMonth
                    }
                }
            }
            "year" -> {
                cal.add(Calendar.YEAR, offset)
                val currentYear = cal.get(Calendar.YEAR)
                baseList.filter { tx ->
                    val txDate = try { sdf.parse(tx.date) } catch(e: Exception) { null }
                    if (txDate == null) true
                    else {
                        val txCal = Calendar.getInstance().apply { time = txDate }
                        txCal.get(Calendar.YEAR) == currentYear
                    }
                }
            }
            else -> baseList
        }
    }

    fun getCategoryTotalsForOffset(offset: Int): List<Pair<String, Double>> {
        val rawList = getFilteredListForOffset(offset)
        val typeFiltered = when (filterType) {
            "income" -> rawList.filter { it.type.equals("income", ignoreCase = true) }
            "expense" -> rawList.filter { it.type.equals("expense", ignoreCase = true) }
            else -> rawList
        }
        val fullMap = typeFiltered
            .groupBy { it.category.ifBlank { "Прочее" } }
            .map { (cat, list) -> cat to list.sumOf { kotlin.math.abs(it.amount) } }
            .sortedByDescending { it.second }

        val threshold = 3
        return if (fullMap.size > threshold) {
            if (isDrilledDownToMixed) {
                fullMap.drop(2)
            } else {
                val top2 = fullMap.take(2)
                val remaining = fullMap.drop(2)
                val remainingSum = remaining.sumOf { it.second }
                val remainingCount = remaining.size
                val mixedName = if (remainingCount <= 1) "✨ Прочие" else "✨ Смешанные (+$remainingCount)"
                (top2 + (mixedName to remainingSum)).sortedByDescending { it.second }
            }
        } else {
            fullMap
        }
    }

    // Filtered by type ("expense", "income", "all")
    val typeFilteredList = remember(periodFilteredList, filterType) {
        when (filterType) {
            "income" -> periodFilteredList.filter { it.type.equals("income", ignoreCase = true) }
            "expense" -> periodFilteredList.filter { it.type.equals("expense", ignoreCase = true) }
            else -> periodFilteredList
        }
    }

    // Category sums map for current filter
    val categoryTotalsMap = remember(typeFilteredList, visibleDateStr.value, selectedPeriod) {
        val listForCategory = if (selectedPeriod == "all" || selectedPeriod == "custom") {
            val dateParts = visibleDateStr.value.split("-")
            if (dateParts.size >= 2) {
                val prefix = "${dateParts[0]}-${dateParts[1]}-"
                typeFilteredList.filter { it.date.startsWith(prefix) }
            } else {
                typeFilteredList
            }
        } else {
            typeFilteredList
        }
        listForCategory
            .groupBy { it.category.ifBlank { "Прочее" } }
            .mapValues { entry -> entry.value.sumOf { kotlin.math.abs(it.amount) } }
            .filterValues { it > 0 }
            .toList()
            .sortedByDescending { it.second }
    }

    val categoryThreshold = 3
    val shouldGroup = categoryTotalsMap.size > categoryThreshold

    val accentColor = remember(filterType) {
        if (filterType == "expense") Rose500 else Emerald400
    }

    val remainingCategoryNames = remember(categoryTotalsMap) {
        if (categoryTotalsMap.size > categoryThreshold) {
            categoryTotalsMap.drop(2).map { it.first }
        } else {
            emptyList()
        }
    }

    val categoryColorMap = remember(categoryTotalsMap, filterType) {
        val map = mutableMapOf<String, Color>()
        categoryTotalsMap.forEachIndexed { index, (catName, _) ->
            val (systemColor, _) = getCategoryColorAndIcon(catName, "")
            if (systemColor == Slate400) {
                val baseColors = if (filterType == "income") {
                    listOf(
                        Color(0xFF10B981), // Emerald
                        Color(0xFF6366F1), // Indigo
                        Color(0xFF3B82F6), // Blue
                        Color(0xFFF59E0B), // Amber
                        Color(0xFF8B5CF6), // Purple
                        Color(0xFF14B8A6)  // Teal
                    )
                } else {
                    listOf(
                        Color(0xFFF43F5E), // Rose
                        Color(0xFF3B82F6), // Blue
                        Color(0xFFF59E0B), // Amber
                        Color(0xFF8B5CF6), // Purple
                        Color(0xFF10B981), // Emerald
                        Color(0xFF14B8A6)  // Teal
                    )
                }
                map[catName] = baseColors.getOrElse(index % baseColors.size) { if (filterType == "expense") Rose500 else Emerald400 }
            } else {
                map[catName] = systemColor
            }
        }
        map["✨ Смешанные"] = Indigo500
        map["✨ Прочие"] = Indigo500
        map
    }

    fun getCategoryColor(name: String): Color {
        val cleanName = if (name.startsWith("✨")) {
            if (name.contains("Прочие")) "✨ Прочие" else "✨ Смешанные"
        } else {
            name
        }
        return categoryColorMap.get(cleanName) ?: run {
            val (systemColor, _) = getCategoryColorAndIcon(cleanName, "")
            if (systemColor == Slate400) {
                if (filterType == "expense") Rose500 else Emerald400
            } else {
                systemColor
            }
        }
    }

    val currentActiveCategoryTotals = remember(categoryTotalsMap, isDrilledDownToMixed) {
        if (categoryTotalsMap.size > categoryThreshold) {
            if (isDrilledDownToMixed) {
                categoryTotalsMap.drop(2)
            } else {
                val top2 = categoryTotalsMap.take(2)
                val remaining = categoryTotalsMap.drop(2)
                val remainingSum = remaining.sumOf { it.second }
                val remainingCount = remaining.size
                val mixedName = if (remainingCount <= 1) "✨ Прочие" else "✨ Смешанные (+$remainingCount)"
                (top2 + (mixedName to remainingSum)).sortedByDescending { it.second }
            }
        } else {
            categoryTotalsMap
        }
    }

    // Search and category filtered
    val searchFilteredList = remember(typeFilteredList, searchQuery, selectedCategoryFilter, isDrilledDownToMixed, remainingCategoryNames) {
        typeFilteredList.filter { tx ->
            val txCat = tx.category.ifBlank { "Прочее" }
            val matchesCategory = when {
                selectedCategoryFilter == "✨ Смешанные" || selectedCategoryFilter == "✨ Прочие" -> {
                    remainingCategoryNames.any { it.equals(txCat, ignoreCase = true) }
                }
                isDrilledDownToMixed && selectedCategoryFilter == null -> {
                    remainingCategoryNames.any { it.equals(txCat, ignoreCase = true) }
                }
                selectedCategoryFilter.isNullOrBlank() -> true
                else -> txCat.equals(selectedCategoryFilter, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() ||
                    tx.category.contains(searchQuery, ignoreCase = true) ||
                    tx.subcategory.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    // Sorted
    val sortedList = remember(searchFilteredList, sortOption) {
        when (sortOption) {
            "desc" -> searchFilteredList.sortedByDescending { kotlin.math.abs(it.amount) }
            "asc" -> searchFilteredList.sortedBy { kotlin.math.abs(it.amount) }
            "name" -> searchFilteredList.sortedBy { it.category.ifBlank { it.subcategory } }
            "name_desc" -> searchFilteredList.sortedByDescending { it.category.ifBlank { it.subcategory } }
            else -> searchFilteredList.sortedByDescending { it.date }
        }
    }

    val groupedByDate = remember(sortedList) {
        sortedList.groupBy { it.date }
    }

    val totalExpenseAmt = remember(periodFilteredList, visibleDateStr.value, selectedPeriod) {
        if (selectedPeriod == "all" || selectedPeriod == "custom") {
            val dateParts = visibleDateStr.value.split("-")
            if (dateParts.size >= 2) {
                val prefix = "${dateParts[0]}-${dateParts[1]}-"
                periodFilteredList.filter { it.type == "expense" && it.date.startsWith(prefix) }
                    .sumOf { kotlin.math.abs(it.amount) }
            } else {
                periodFilteredList.filter { it.type == "expense" }.sumOf { kotlin.math.abs(it.amount) }
            }
        } else {
            periodFilteredList.filter { it.type == "expense" }.sumOf { kotlin.math.abs(it.amount) }
        }
    }
    val totalIncomeAmt = remember(periodFilteredList, visibleDateStr.value, selectedPeriod) {
        if (selectedPeriod == "all" || selectedPeriod == "custom") {
            val dateParts = visibleDateStr.value.split("-")
            if (dateParts.size >= 2) {
                val prefix = "${dateParts[0]}-${dateParts[1]}-"
                periodFilteredList.filter { it.type == "income" && it.date.startsWith(prefix) }
                    .sumOf { kotlin.math.abs(it.amount) }
            } else {
                periodFilteredList.filter { it.type == "income" }.sumOf { kotlin.math.abs(it.amount) }
            }
        } else {
            periodFilteredList.filter { it.type == "income" }.sumOf { kotlin.math.abs(it.amount) }
        }
    }

    val animatedHeaderDragOffsetY by animateFloatAsState(
        targetValue = headerDragOffsetY,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "header_drag_offset"
    )

    if (selectedReceiptTransaction != null) {
        val receipt = selectedReceiptTransaction!!
        val receiptItems = fullTxList.filter { it.parentId == receipt.id }
        
        ReceiptDetailsDialog(
            parentTransaction = receipt,
            receiptItems = receiptItems,
            onDismiss = { selectedReceiptTransaction = null }
        )
    }

    SwipeToDismissDialog(
        onDismissRequest = onDismiss,
        isAtTop = { lazyListState.firstVisibleItemIndex == 0 },
        contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 12.dp, bottom = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.96f)
                    .offset { IntOffset(x = 0, y = animatedHeaderDragOffsetY.roundToInt()) },
                color = DarkBg,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Vertical drag handle for swipe dismissal
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragStart = { headerDragOffsetY = 0f },
                                    onDragEnd = {
                                        if (headerDragOffsetY > 80f) {
                                            onDismiss()
                                        } else {
                                            headerDragOffsetY = 0f
                                        }
                                    },
                                    onDragCancel = { headerDragOffsetY = 0f },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        if (dragAmount > 0f || headerDragOffsetY > 0f) {
                                            headerDragOffsetY = (headerDragOffsetY + dragAmount).coerceAtLeast(0f)
                                        }
                                    }
                                )
                            }
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Slate700)
                        )
                    }

                    // Header Bar
                    TransactionHeaderBar(
                        totalCount = transactions.size,
                        filteredCount = sortedList.size
                    )

                    // Pinned Collapsible / Morphing Header or Full-Size Components
                    val showCompactHeader = isScrolled && filterType != "all"

                    AnimatedContent(
                        targetState = showCompactHeader,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(180, delayMillis = 60)) + androidx.compose.animation.scaleIn(initialScale = 0.96f, animationSpec = tween(180, delayMillis = 60)))
                                .togetherWith(fadeOut(animationSpec = tween(100)) + androidx.compose.animation.scaleOut(targetScale = 0.98f, animationSpec = tween(100)))
                        },
                        label = "header_morph_animation"
                    ) { targetCompact ->
                        if (targetCompact) {
                            TransactionCompactHeader(
                                filterType = filterType,
                                totalExpenseAmt = totalExpenseAmt,
                                totalIncomeAmt = totalIncomeAmt,
                                currentActiveCategoryTotals = currentActiveCategoryTotals,
                                selectedCategoryFilter = selectedCategoryFilter,
                                isDrilledDownToMixed = isDrilledDownToMixed,
                                remainingCategoryNames = remainingCategoryNames,
                                getCategoryColor = { getCategoryColor(it) },
                                onResetFilter = {
                                    filterType = "all"
                                    selectedCategoryFilter = null
                                    isDrilledDownToMixed = false
                                }
                            )
                        } else {
                            // Full-Size UI Header
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (filterType == "all") {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(Slate900)
                                                    .border(1.dp, Slate800, RoundedCornerShape(20.dp))
                                                    .clickable { 
                                                        if (filterType == "expense") {
                                                            selectedPeriod = "month"
                                                        } else {
                                                            filterType = "expense"
                                                        }
                                                    }
                                                    .padding(14.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(
                                                        text = formatFullCurrency(totalExpenseAmt),
                                                        color = Color.White,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                    Text("Траты", color = Slate400, fontSize = 11.sp)
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(4.dp)
                                                            .clip(CircleShape)
                                                            .background(Rose500)
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(Slate900)
                                                    .border(1.dp, Slate800, RoundedCornerShape(20.dp))
                                                    .clickable { 
                                                        if (filterType == "income") {
                                                            selectedPeriod = "month"
                                                        } else {
                                                            filterType = "income"
                                                        }
                                                    }
                                                    .padding(14.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(
                                                        text = formatFullCurrency(totalIncomeAmt),
                                                        color = Color.White,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                    Text("Доходы", color = Slate400, fontSize = 11.sp)
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(4.dp)
                                                            .clip(CircleShape)
                                                            .background(Emerald400)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    val isExpense = filterType == "expense"
                                    val accentColor = if (isExpense) Rose500 else Emerald400
                                    val activeAmount = if (filterType == "expense") totalExpenseAmt else totalIncomeAmt
                                    val prevAmount = remember(selectedPeriod, currentOffsetInt, transactions, filterType, visibleDateStr.value) {
                                        if (selectedPeriod == "all" || selectedPeriod == "custom") {
                                            val dateParts = visibleDateStr.value.split("-")
                                            if (dateParts.size >= 2) {
                                                val year = dateParts[0].toIntOrNull() ?: 2026
                                                val month = dateParts[1].toIntOrNull() ?: 8
                                                val prevMonth = if (month == 1) 12 else month - 1
                                                val prevYear = if (month == 1) year - 1 else year
                                                val prefix = String.format("%04d-%02d-", prevYear, prevMonth)
                                                val tType = if (filterType == "expense") "expense" else "income"
                                                transactions.filter { it.type.equals(tType, ignoreCase = true) && it.date.startsWith(prefix) }
                                                    .sumOf { kotlin.math.abs(it.amount) }
                                            } else {
                                                0.0
                                            }
                                        } else {
                                            val prevOffset = currentOffsetInt - 1
                                            val prevList = getFilteredListForOffset(prevOffset)
                                            val tType = if (filterType == "expense") "expense" else "income"
                                            prevList.filter { it.type.equals(tType, ignoreCase = true) }.sumOf { kotlin.math.abs(it.amount) }
                                        }
                                    }
                                    val amountDiff = activeAmount - prevAmount
                                    val diffText = if (amountDiff >= 0) {
                                        "↑ ${formatFullCurrency(amountDiff)}"
                                    } else {
                                        "↓ ${formatFullCurrency(kotlin.math.abs(amountDiff))}"
                                    }
                                    val titleText = if (isExpense) "Траты" else "Доходы"

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Slate900.copy(alpha = 0.9f))
                                            .border(1.dp, Slate800, RoundedCornerShape(24.dp))
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = formatFullCurrency(activeAmount),
                                                        color = Color.White,
                                                        fontSize = 32.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(16.dp))
                                                            .background(Color.White.copy(alpha = 0.08f))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = diffText,
                                                            color = Slate300,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = titleText,
                                                    color = Slate400,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }

                                            IconButton(
                                                onClick = { filterType = "all" },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Slate800)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "К всем",
                                                    tint = Slate400,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        var totalDragX by remember { mutableFloatStateOf(0f) }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .pointerInput(selectedPeriod) {
                                                    detectHorizontalDragGestures(
                                                        onDragStart = { totalDragX = 0f },
                                                        onDragEnd = {
                                                            val threshold = 120f
                                                            if (totalDragX < -threshold) {
                                                                when (selectedPeriod) {
                                                                    "week" -> swipeWeekOffset += 1
                                                                    "month" -> swipeMonthOffset += 1
                                                                    "year" -> swipeYearOffset += 1
                                                                    else -> swipeMonthOffset += 1
                                                                }
                                                            } else if (totalDragX > threshold) {
                                                                when (selectedPeriod) {
                                                                    "week" -> swipeWeekOffset -= 1
                                                                    "month" -> swipeMonthOffset -= 1
                                                                    "year" -> swipeYearOffset -= 1
                                                                    else -> swipeMonthOffset -= 1
                                                                }
                                                            }
                                                        },
                                                        onHorizontalDrag = { change, dragAmount ->
                                                            change.consume()
                                                            totalDragX += dragAmount
                                                        }
                                                    )
                                                }
                                        ) {
                                            BoxWithConstraints(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(250.dp)
                                                    .clipToBounds()
                                            ) {
                                                val widthPx = constraints.maxWidth
                                                val widthDp = with(androidx.compose.ui.platform.LocalDensity.current) { widthPx.toDp() }
                                                
                                                val pageWidthDp = widthDp * 0.8f
                                                val centerOffsetDp = widthDp * 0.1f
                                                
                                                val animatedOffsetFloat by animateFloatAsState(
                                                    targetValue = currentOffsetInt.toFloat(),
                                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                                                    label = "diagram_slide_offset"
                                                )
                                                
                                                listOf(currentOffsetInt - 1, currentOffsetInt, currentOffsetInt + 1).forEach { offsetValue ->
                                                    val translationX = centerOffsetDp + pageWidthDp * (offsetValue - animatedOffsetFloat)
                                                    val relativeDiff = kotlin.math.abs(offsetValue - animatedOffsetFloat)
                                                    val alphaValue = if (offsetValue == currentOffsetInt) {
                                                        1f - kotlin.math.min(1f, relativeDiff) * 0.4f
                                                    } else {
                                                        0.3f + (1f - kotlin.math.min(1f, relativeDiff)) * 0.3f
                                                    }
                                                    
                                                    val scaleValue = if (offsetValue == currentOffsetInt) {
                                                        1f - kotlin.math.min(1f, relativeDiff) * 0.12f
                                                    } else {
                                                        0.88f + (1f - kotlin.math.min(1f, relativeDiff)) * 0.12f
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .width(pageWidthDp)
                                                            .fillMaxHeight()
                                                            .offset(x = translationX)
                                                            .graphicsLayer {
                                                                alpha = alphaValue
                                                                scaleX = scaleValue
                                                                scaleY = scaleValue
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        val offsetTotalsMap = getCategoryTotalsForOffset(offsetValue)
                                                        
                                                        if (chartViewMode == "donut") {
                                                            Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                val sumAll = offsetTotalsMap.sumOf { it.second }
                                                                
                                                                Box(
                                                                    modifier = Modifier.size(240.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Canvas(modifier = Modifier.size(170.dp)) {
                                                                        val strokeWidth = 20.dp.toPx()
                                                                        val radius = (size.minDimension - strokeWidth) / 2
                                                                        val centerOffset = Offset(size.width / 2, size.height / 2)
                                                                        
                                                                        if (offsetTotalsMap.isEmpty() || sumAll <= 0) {
                                                                            drawCircle(
                                                                                color = Slate800,
                                                                                radius = radius,
                                                                                center = centerOffset,
                                                                                style = Stroke(width = strokeWidth)
                                                                            )
                                                                        } else {
                                                                            var startAngle = -90f
                                                                            
                                                                            offsetTotalsMap.forEach { (catName, amt) ->
                                                                                val sweepAngle = ((amt / sumAll) * 360f).toFloat()
                                                                                val col = getCategoryColor(catName)
                                                                                
                                                                                if (sweepAngle > 0f) {
                                                                                    drawArc(
                                                                                        color = col.copy(alpha = 0.18f),
                                                                                        startAngle = startAngle,
                                                                                        sweepAngle = sweepAngle - 3f,
                                                                                        useCenter = false,
                                                                                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                                                                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                                                                        style = Stroke(width = strokeWidth * 1.4f, cap = StrokeCap.Round)
                                                                                    )
                                                                                    
                                                                                    drawArc(
                                                                                        color = col,
                                                                                        startAngle = startAngle,
                                                                                        sweepAngle = sweepAngle - 3f,
                                                                                        useCenter = false,
                                                                                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                                                                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                                                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                                                                    )
                                                                                }
                                                                                startAngle += sweepAngle
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            val effectivePeriod = if (selectedPeriod == "all") "month" else selectedPeriod
                                                            val anchorDateStr = stableAnchorDateStr.ifBlank { "2026-08-01" }
                                                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                            val anchorDate = try { sdf.parse(anchorDateStr) } catch(e: Exception) { null } ?: Date()
                                                            val cal = Calendar.getInstance().apply { time = anchorDate }
                                                            
                                                            val (barValues, barLabels, currentHighlightIdx) = when (effectivePeriod) {
                                                                "year" -> {
                                                                    cal.add(Calendar.YEAR, offsetValue)
                                                                    val currentYear = cal.get(Calendar.YEAR)
                                                                    
                                                                    val monthNamesList = listOf("Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек")
                                                                    val monthValues = DoubleArray(12) { 0.0 }
                                                                    
                                                                    val offsetTransactions = getFilteredListForOffset(offsetValue)
                                                                    val typeFiltered = offsetTransactions.filter { it.type.equals(filterType, ignoreCase = true) }
                                                                    
                                                                    typeFiltered.forEach { tx ->
                                                                        val txDate = try { sdf.parse(tx.date) } catch(e: Exception) { null }
                                                                        if (txDate != null) {
                                                                            val txCal = Calendar.getInstance().apply { time = txDate }
                                                                            if (txCal.get(Calendar.YEAR) == currentYear) {
                                                                                val monthIdx = txCal.get(Calendar.MONTH)
                                                                                if (monthIdx in 0..11) {
                                                                                    monthValues[monthIdx] += kotlin.math.abs(tx.amount)
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    
                                                                    val highlightIdx = if (currentYear == Calendar.getInstance().get(Calendar.YEAR)) {
                                                                        Calendar.getInstance().get(Calendar.MONTH)
                                                                    } else {
                                                                        -1
                                                                    }
                                                                    Triple(monthValues.toList(), monthNamesList, highlightIdx)
                                                                }
                                                                "week" -> {
                                                                    cal.add(Calendar.WEEK_OF_YEAR, offsetValue)
                                                                    val firstDayOfWeek = Calendar.MONDAY
                                                                    cal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                                                                    
                                                                    val weekDayLabels = mutableListOf<String>()
                                                                    val weekDayValues = DoubleArray(7) { 0.0 }
                                                                    val dayFormats = SimpleDateFormat("dd", Locale.getDefault())
                                                                    
                                                                    val weekDates = (0..6).map { i ->
                                                                        val dCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_WEEK, i) }
                                                                        val dayNum = dayFormats.format(dCal.time).toIntOrNull()?.toString() ?: dayFormats.format(dCal.time)
                                                                        weekDayLabels.add(dayNum)
                                                                        dCal.time
                                                                    }
                                                                    
                                                                    val offsetTransactions = getFilteredListForOffset(offsetValue)
                                                                    val typeFiltered = offsetTransactions.filter { it.type.equals(filterType, ignoreCase = true) }
                                                                    
                                                                    typeFiltered.forEach { tx ->
                                                                        val txDate = try { sdf.parse(tx.date) } catch(e: Exception) { null }
                                                                        if (txDate != null) {
                                                                            val txCal = Calendar.getInstance().apply { time = txDate }
                                                                            weekDates.forEachIndexed { idx, wDate ->
                                                                                val wCal = Calendar.getInstance().apply { time = wDate }
                                                                                if (txCal.get(Calendar.YEAR) == wCal.get(Calendar.YEAR) &&
                                                                                    txCal.get(Calendar.DAY_OF_YEAR) == wCal.get(Calendar.DAY_OF_YEAR)) {
                                                                                    weekDayValues[idx] += kotlin.math.abs(tx.amount)
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    
                                                                    val nowCal = Calendar.getInstance()
                                                                    val highlightIdx = weekDates.indexOfFirst { wDate ->
                                                                        val wCal = Calendar.getInstance().apply { time = wDate }
                                                                        nowCal.get(Calendar.YEAR) == wCal.get(Calendar.YEAR) &&
                                                                        nowCal.get(Calendar.DAY_OF_YEAR) == wCal.get(Calendar.DAY_OF_YEAR)
                                                                    }
                                                                    Triple(weekDayValues.toList(), weekDayLabels, highlightIdx)
                                                                }
                                                                else -> {
                                                                    cal.add(Calendar.MONTH, offsetValue)
                                                                    val currentYear = cal.get(Calendar.YEAR)
                                                                    val currentMonth = cal.get(Calendar.MONTH)
                                                                    
                                                                    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                                                                    val weekRanges = mutableListOf<Pair<Int, Int>>()
                                                                    var startDay = 1
                                                                    while (startDay <= maxDays) {
                                                                        val tempCal = Calendar.getInstance().apply {
                                                                            set(Calendar.YEAR, currentYear)
                                                                            set(Calendar.MONTH, currentMonth)
                                                                            set(Calendar.DAY_OF_MONTH, startDay)
                                                                        }
                                                                        val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
                                                                        val daysToSunday = if (dayOfWeek == Calendar.SUNDAY) 0 else (8 - dayOfWeek)
                                                                        val endDay = kotlin.math.min(startDay + daysToSunday, maxDays)
                                                                        weekRanges.add(startDay to endDay)
                                                                        startDay = endDay + 1
                                                                    }
                                                                    
                                                                    val weekLabels = weekRanges.map { (s, e) ->
                                                                        if (s == e) "$s" else "$s-$e"
                                                                    }
                                                                    val weekValues = DoubleArray(weekRanges.size) { 0.0 }
                                                                    
                                                                    val offsetTransactions = getFilteredListForOffset(offsetValue)
                                                                    val typeFiltered = offsetTransactions.filter { it.type.equals(filterType, ignoreCase = true) }
                                                                    
                                                                    typeFiltered.forEach { tx ->
                                                                        val txDate = try { sdf.parse(tx.date) } catch(e: Exception) { null }
                                                                        if (txDate != null) {
                                                                            val txCal = Calendar.getInstance().apply { time = txDate }
                                                                            if (txCal.get(Calendar.YEAR) == currentYear && txCal.get(Calendar.MONTH) == currentMonth) {
                                                                                val day = txCal.get(Calendar.DAY_OF_MONTH)
                                                                                val weekIdx = weekRanges.indexOfFirst { day in it.first..it.second }
                                                                                if (weekIdx != -1) {
                                                                                    weekValues[weekIdx] += kotlin.math.abs(tx.amount)
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    
                                                                    val nowCal = Calendar.getInstance()
                                                                    val highlightIdx = if (nowCal.get(Calendar.YEAR) == currentYear && nowCal.get(Calendar.MONTH) == currentMonth) {
                                                                        val day = nowCal.get(Calendar.DAY_OF_MONTH)
                                                                        weekRanges.indexOfFirst { day in it.first..it.second }
                                                                    } else {
                                                                        -1
                                                                    }
                                                                    Triple(weekValues.toList(), weekLabels, highlightIdx)
                                                                }
                                                            }
                                                            
                                                            val maxVal = barValues.maxOrNull() ?: 0.0
                                                            val averageVal = if (barValues.any { it > 0.0 }) barValues.filter { it > 0.0 }.average() else 0.0
                                                            val averageRatio = if (maxVal > 0.0) (averageVal / maxVal).toFloat() else 0f
                                                            
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(130.dp)
                                                            ) {
                                                                if (averageVal > 0.0) {
                                                                    Canvas(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .height(90.dp)
                                                                            .align(Alignment.TopCenter)
                                                                    ) {
                                                                        val y = size.height - (size.height * averageRatio)
                                                                        drawLine(
                                                                            color = Slate500.copy(alpha = 0.7f),
                                                                            start = Offset(0f, y),
                                                                            end = Offset(size.width, y),
                                                                            strokeWidth = 1.dp.toPx(),
                                                                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                                                        )
                                                                    }
                                                                    
                                                                    Text(
                                                                        text = "Ср: ${formatFullCurrency(averageVal)}",
                                                                        color = Slate400,
                                                                        fontSize = 8.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        modifier = Modifier
                                                                            .align(Alignment.TopStart)
                                                                            .offset(x = 4.dp, y = (90 * (1 - averageRatio) - 12).coerceAtLeast(0f).dp)
                                                                    )
                                                                }
                                                                
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxSize()
                                                                        .padding(vertical = 8.dp),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.Bottom
                                                                ) {
                                                                    barLabels.forEachIndexed { i, labelText ->
                                                                        val amountVal = barValues.getOrElse(i) { 0.0 }
                                                                        val barRatio = if (maxVal > 0.0) (amountVal / maxVal).toFloat() else 0f
                                                                        val displayRatio = if (amountVal > 0.0) barRatio.coerceAtLeast(0.06f) else 0f
                                                                        val isHighlighted = (i == currentHighlightIdx) || (currentHighlightIdx == -1 && barRatio == 1f && amountVal > 0.0)
                                                                        
                                                                        val barWidth = when (barLabels.size) {
                                                                            in 1..5 -> 18.dp
                                                                            in 6..8 -> 14.dp
                                                                            else -> 10.dp
                                                                        }
                                                                        
                                                                        Column(
                                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                                        ) {
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .width(barWidth)
                                                                                    .height((90 * displayRatio).dp)
                                                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                                                    .background(
                                                                                        if (isHighlighted) accentColor 
                                                                                        else Indigo500.copy(alpha = 0.5f)
                                                                                    )
                                                                            )
                                                                            Text(
                                                                                text = labelText,
                                                                                color = if (isHighlighted) Color.White else Slate400,
                                                                                fontSize = 8.sp,
                                                                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
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

                                        TransactionPeriodSelector(
                                            selectedPeriod = selectedPeriod,
                                            onPeriodSelect = { pKey -> selectedPeriod = pKey },
                                            chartViewMode = chartViewMode,
                                            onChartViewModeChange = { mode -> chartViewMode = mode }
                                        )

                                        if (categoryTotalsMap.isNotEmpty()) {
                                            val totalCategorySum = categoryTotalsMap.sumOf { it.second }
                                            val remainingSum = categoryTotalsMap.drop(2).sumOf { it.second }

                                            TransactionPillCategoryList(
                                                categoryTotalsMap = categoryTotalsMap,
                                                totalCategorySum = totalCategorySum,
                                                selectedCategoryFilter = selectedCategoryFilter,
                                                isDrilledDownToMixed = isDrilledDownToMixed,
                                                shouldGroup = shouldGroup,
                                                remainingCategoryNames = remainingCategoryNames,
                                                remainingSum = remainingSum,
                                                getCategoryColor = { getCategoryColor(it) },
                                                onCategoryToggle = { newFilter -> selectedCategoryFilter = newFilter },
                                                onDrillBack = {
                                                    isDrilledDownToMixed = false
                                                    selectedCategoryFilter = null
                                                },
                                                onOpenMixedDialog = { showMixedCategoriesDialog = true }
                                            )
                                        }

                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    androidx.compose.animation.AnimatedContent(
                        targetState = isSearchExpandedInPlace,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(150)) + androidx.compose.animation.scaleIn(initialScale = 0.98f))
                                .togetherWith(fadeOut(animationSpec = tween(100)) + androidx.compose.animation.scaleOut(targetScale = 0.98f))
                        },
                        label = "search_row_in_sorting_morph"
                    ) { expanded ->
                        if (expanded) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .padding(vertical = 2.dp),
                                color = Slate900,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (searchQuery.isNotEmpty()) Indigo500 else Slate800
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = if (searchQuery.isNotEmpty()) Indigo500 else Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(focusRequester),
                                        singleLine = true,
                                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                        decorationBox = { innerTextField ->
                                            if (searchQuery.isEmpty()) {
                                                Text(
                                                    text = "Быстрый поиск...",
                                                    color = Slate500,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Скрыть поиск",
                                        tint = Slate400,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { 
                                                isSearchExpandedInPlace = false
                                            }
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Сортировка:",
                                        color = Slate400,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    val isCatActive = !selectedCategoryFilter.isNullOrBlank() || isDrilledDownToMixed
                                    if (isCatActive) {
                                        val catLabel = selectedCategoryFilter ?: if (remainingCategoryNames.size <= 1) "Прочие" else "Смешанные"
                                        val catColor = if (!selectedCategoryFilter.isNullOrBlank()) getCategoryColor(selectedCategoryFilter!!) else Indigo500
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(catColor.copy(alpha = 0.2f))
                                                .border(1.dp, catColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .clickable {
                                                    selectedCategoryFilter = null
                                                    isDrilledDownToMixed = false
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = catLabel,
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "✕",
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isPeriodActive = selectedPeriod != "all" || !customStartStr.isNullOrBlank()
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isPeriodActive) Indigo500 else Slate900)
                                            .border(1.dp, if (isPeriodActive) Indigo500 else Slate800, RoundedCornerShape(8.dp))
                                            .clickable { showDatePickerModal = true }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = null,
                                                tint = if (isPeriodActive) Color.White else Slate400,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = dynamicMonthLabel,
                                                color = if (isPeriodActive) Color.White else Slate400,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isPeriodActive) {
                                                Text(
                                                    text = "✕",
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.clickable {
                                                        selectedPeriod = "all"
                                                        customStartStr = null
                                                        customEndStr = null
                                                        customLabelStr = null
                                                    }
                                                )
                                            } else {
                                                Text("▼", color = Slate400, fontSize = 8.sp)
                                            }
                                        }
                                    }

                                    val isAmountActive = sortOption == "desc" || sortOption == "asc"
                                    val arrowRotation by animateFloatAsState(
                                        targetValue = if (sortOption == "asc") 180f else 0f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                        label = "arrowRotation"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isAmountActive) Indigo500 else Slate900)
                                            .border(1.dp, if (isAmountActive) Indigo500 else Slate800, RoundedCornerShape(8.dp))
                                            .clickable {
                                                sortOption = when (sortOption) {
                                                    "desc" -> "asc"
                                                    "asc" -> "date"
                                                    else -> "desc"
                                                 }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "↓",
                                                color = if (isAmountActive) Color.White else Slate400,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.graphicsLayer { rotationZ = arrowRotation }
                                            )
                                            Text(
                                                text = "Сумма",
                                                color = if (isAmountActive) Color.White else Slate400,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    val isNameActive = sortOption == "name" || sortOption == "name_desc"
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isNameActive) Indigo500 else Slate900)
                                            .border(1.dp, if (isNameActive) Indigo500 else Slate800, RoundedCornerShape(8.dp))
                                            .clickable {
                                                sortOption = when (sortOption) {
                                                    "name" -> "name_desc"
                                                    "name_desc" -> "date"
                                                    else -> "name"
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = if (sortOption == "name_desc") "Z-A" else "A-Z",
                                            color = if (isNameActive) Color.White else Slate400,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (isScrolled) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (searchQuery.isNotEmpty()) Indigo500.copy(alpha = 0.2f) else Slate900)
                                                .border(
                                                    1.dp,
                                                    if (searchQuery.isNotEmpty()) Indigo500 else Slate800,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    isSearchExpandedInPlace = true
                                                }
                                                .size(26.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Поиск",
                                                tint = if (searchQuery.isNotEmpty()) Indigo500 else Slate400,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 0.dp,
                            start = 0.dp,
                            end = 0.dp,
                            bottom = 24.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item(key = "search_bar_unpinned") {
                            TransactionSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it }
                            )
                        }

                        if (sortedList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Операции не найдены",
                                        color = Slate500,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            groupedByDate.forEach { (dateStr, itemsForDay) ->
                                item(key = "header_$dateStr") {
                                    val dayTotalExpense = itemsForDay.filter { it.type == "expense" }.sumOf { kotlin.math.abs(it.amount) }
                                    val dayTotalIncome = itemsForDay.filter { it.type == "income" }.sumOf { kotlin.math.abs(it.amount) }

                                    TransactionDayHeader(
                                        dateLabel = formatDayHeaderLabel(dateStr),
                                        dayTotalExpense = dayTotalExpense,
                                        dayTotalIncome = dayTotalIncome,
                                        filterType = filterType
                                    )
                                }

                                items(itemsForDay, key = { it.id }) { tx ->
                                    TransactionRowItem(
                                        item = tx,
                                        onDelete = { txId -> onDeleteTransaction?.invoke(txId) },
                                        onClick = {
                                            val childItems = fullTxList.filter { it.parentId == tx.id }
                                            if (childItems.isNotEmpty()) {
                                                selectedReceiptTransaction = tx
                                            } else {
                                                onEditTransaction?.invoke(tx)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDatePickerModal) {
            DateRangePickerDialog(
                initialStart = customStartStr ?: visibleDateStr.value.ifBlank { "2026-07-01" },
                initialEnd = customEndStr ?: "2026-08-31",
                onDismiss = { showDatePickerModal = false },
                onConfirm = { s, e ->
                    customStartStr = s
                    customEndStr = e
                    selectedPeriod = "custom"
                    customLabelStr = try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val d1 = sdf.parse(s)
                        val d2 = sdf.parse(e)
                        if (d1 != null && d2 != null) {
                            val outSdf = SimpleDateFormat("d MMM", Locale("ru", "RU"))
                            "${outSdf.format(d1)} - ${outSdf.format(d2)}"
                        } else "$s - $e"
                    } catch (ex: Exception) { "$s - $e" }
                    showDatePickerModal = false
                }
            )
        }

        if (showMixedCategoriesDialog) {
            MixedCategoriesDialog(
                categoryTotalsMap = categoryTotalsMap,
                onDismiss = { showMixedCategoriesDialog = false },
                onCategorySelected = { catName ->
                    selectedCategoryFilter = catName
                    isDrilledDownToMixed = true
                    showMixedCategoriesDialog = false
                },
                getCategoryColor = { catName -> getCategoryColor(catName) }
            )
        }
    }
}
