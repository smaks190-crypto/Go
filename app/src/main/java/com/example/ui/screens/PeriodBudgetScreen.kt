package com.example.ui.screens

import com.example.ui.components.dialogs.*

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.ReportDetailsDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TransactionEntity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.dialogs.CategoryLimitsDialog
import com.example.ui.components.SwipeDirection
import com.example.ui.components.SwipeToRevealBox
import com.example.ui.components.IncomeExpenseSummaryDialog
import com.example.ui.components.dialogs.AllTransactionsDialog
import com.example.ui.components.DatePickerField
import com.example.ui.components.MarkdownFormattedText
import com.example.ui.components.formatFullCurrency
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700

import com.example.ui.components.RollingCurrencyText
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.DarkBg
import com.example.ui.viewmodel.PeriodType
import com.example.ui.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning

val MonthsRu = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
)

@Composable
fun PeriodBudgetScreen(
    periodType: PeriodType,
    selectedDateDay: String,
    selectedMonthIdx: Int,
    selectedYear: Int = 2026,
    allPeriodStart: String,
    allPeriodEnd: String,
    filteredTransactions: List<TransactionEntity>,
    allTransactions: List<TransactionEntity> = emptyList(),
    activeSubTab: String,
    expandedExpense: Boolean,
    expandedIncome: Boolean,
    aiAuditResult: String?,
    aiAuditLoading: Boolean,
    savedAiAudit: com.example.data.db.AiAuditEntity? = null,
    isAppLocked: Boolean = false,
    viewModel: BudgetViewModel? = null,
    onSetPeriodType: (PeriodType) -> Unit,

    onChangeSelectedDay: (String) -> Unit,
    onChangeSelectedMonthIdx: (Int) -> Unit,
    onChangeSelectedAnnualYear: (Int) -> Unit = {},
    onChangeAllPeriodStart: (String) -> Unit,
    onChangeAllPeriodEnd: (String) -> Unit,
    onChangeActiveSubTab: (String) -> Unit,
    onToggleExpandExpense: () -> Unit,
    onToggleExpandIncome: () -> Unit,
    onRequestAiAudit: () -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onEditTransaction: ((TransactionEntity) -> Unit)? = null
) {
    // Исключаем дочерние позиции чека из основного списка
    val mainFilteredTransactions = remember(filteredTransactions) {
        filteredTransactions.filter { it.parentId.isNullOrBlank() }
    }

    // Состояние для выбранного чека
    var selectedReceiptTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // Monthly Calculations
    val monthStart = remember(selectedMonthIdx, selectedYear) {
        val monthFormatted = String.format(Locale.US, "%02d", selectedMonthIdx + 1)
        "$selectedYear-$monthFormatted-01"
    }

    val monthEnd = remember(selectedMonthIdx, selectedYear) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonthIdx)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    val mainAllTransactions = remember(allTransactions) {
        allTransactions.filter { it.parentId.isNullOrBlank() }
    }

    val monthTransactions = remember(mainAllTransactions, monthStart, monthEnd) {
        mainAllTransactions.filter { it.date >= monthStart && it.date <= monthEnd }
    }

    val monthIncomes = remember(monthTransactions) { monthTransactions.filter { it.type == "income" } }
    val monthExpenses = remember(monthTransactions) { monthTransactions.filter { it.type == "expense" } }

    val monthTotalIncome = remember(monthIncomes) { monthIncomes.sumOf { it.amount } }
    val monthTotalExpense = remember(monthExpenses) { monthExpenses.sumOf { it.amount } }
    val monthNetBalance = monthTotalIncome - monthTotalExpense

    val previousCarryover = remember(mainAllTransactions, monthStart) {
        val prevIncomes = mainAllTransactions
            .filter { it.type == "income" && it.date < monthStart }
            .sumOf { it.amount }
        val prevExpenses = mainAllTransactions
            .filter { it.type == "expense" && it.date < monthStart }
            .sumOf { it.amount }

        prevIncomes - prevExpenses
    }

    val monthTotalAccumulatedBalance = previousCarryover + monthNetBalance
    val monthSavingsRate = if (monthTotalIncome > 0) Math.max(0, Math.round((monthNetBalance / monthTotalIncome) * 100)).toInt() else 0

    val incomes = remember(mainFilteredTransactions) { mainFilteredTransactions.filter { it.type == "income" } }
    val expenses = remember(mainFilteredTransactions) { mainFilteredTransactions.filter { it.type == "expense" } }

    val categoryExpenseTotals = remember(expenses) {
        expenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    var showCategoryLimitsDialog by remember { mutableStateOf(false) }
    var showAllTransactionsDialog by remember { mutableStateOf(false) }
    var initialAllTransactionsFilter by remember { mutableStateOf("all") }

    val categoriesListState = viewModel?.categories?.collectAsStateWithLifecycle()
    val categoriesList = categoriesListState?.value ?: remember { emptyList() }

    val pagerState = rememberPagerState(initialPage = if (activeSubTab == "expense") 0 else 1, pageCount = { 2 })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            val targetSubTab = if (page == 0) "expense" else "income"
            if (targetSubTab != activeSubTab) {
                onChangeActiveSubTab(targetSubTab)
            }
        }
    }

    LaunchedEffect(activeSubTab) {
        val targetPage = if (activeSubTab == "expense") 0 else 1
        if (pagerState.currentPage != targetPage && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                com.example.ui.components.SwipeToRevealController.requestCollapseAll()
            }
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- MAIN NEON BALANCE CARD ---
        MainBalanceCard(
            monthSavingsRate = monthSavingsRate,
            monthTotalAccumulatedBalance = monthTotalAccumulatedBalance,
            monthTotalIncome = monthTotalIncome,
            monthTotalExpense = monthTotalExpense,
            onIncomesClick = {
                initialAllTransactionsFilter = "income"
                showAllTransactionsDialog = true
            },
            onExpensesClick = {
                initialAllTransactionsFilter = "expense"
                showAllTransactionsDialog = true
            }
        )

        com.example.ui.components.charts.ExpenseDynamicsAreaChartCard(
            transactions = monthTransactions,
            title = "ДИНАМИКА РАСХОДОВ",
            onClick = null
        )

        // --- CATEGORIES GRID ---
        CategoriesGrid(
            categoryExpenseTotals = categoryExpenseTotals,
            categoriesList = categoriesList,
            onShowLimitsClick = { showCategoryLimitsDialog = true }
        )

        if (showCategoryLimitsDialog && !isAppLocked) {
            CategoryLimitsDialog(
                categories = categoriesList,
                transactions = monthExpenses,
                onUpdateLimit = { catName, limit ->
                    viewModel?.updateCategoryLimit(catName, "expense", limit)
                },
                onDismiss = { showCategoryLimitsDialog = false }
            )
        }

        if (showAllTransactionsDialog && !isAppLocked) {
            val currentPeriodInitialDate = if (periodType == PeriodType.DAY || periodType == PeriodType.WEEK) selectedDateDay else monthStart
            AllTransactionsDialog(
                transactions = if (allTransactions.isNotEmpty()) allTransactions.filter { it.parentId.isNullOrBlank() } else mainFilteredTransactions,
                allTransactions = allTransactions,
                onDeleteTransaction = onDeleteTransaction,
                onEditTransaction = onEditTransaction,
                initialFilterType = initialAllTransactionsFilter,
                initialDate = currentPeriodInitialDate,
                onDismiss = { showAllTransactionsDialog = false }
            )
        }

        // --- RECENT TRANSACTIONS SECTION ---
        val recentTxList = remember(mainFilteredTransactions) {
            mainFilteredTransactions.take(10)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.6f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ПОСЛЕДНИЕ ОПЕРАЦИИ",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Всего ${mainFilteredTransactions.size} транзакций",
                            color = Slate500,
                            fontSize = 10.sp
                        )
                    }

                    if (mainFilteredTransactions.isNotEmpty()) {
                        Text(
                            text = "Все ›",
                            color = Indigo500,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    initialAllTransactionsFilter = "all"
                                    showAllTransactionsDialog = true
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (recentTxList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет операций за выбранный период",
                            color = Slate500,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentTxList.forEach { tx ->
                            val receiptItems = remember(allTransactions, tx.id) {
                                allTransactions.filter { it.parentId.equals(tx.id, ignoreCase = true) }
                            }

                            TransactionRowItem(
                                item = tx,
                                onDelete = onDeleteTransaction,
                                onClick = {
                                    if (receiptItems.isNotEmpty()) {
                                        selectedReceiptTransaction = tx
                                    } else if (onEditTransaction != null) {
                                        onEditTransaction(tx)
                                    }
                                },
                                canDelete = false
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Всплывающее окно состава чека
    selectedReceiptTransaction?.let { parentTx ->
        val childItems = remember(allTransactions, parentTx.id) {
            allTransactions.filter { it.parentId.equals(parentTx.id, ignoreCase = true) }
        }
        ReceiptDetailsDialog(
            parentTransaction = parentTx,
            receiptItems = childItems,
            onDismiss = { selectedReceiptTransaction = null }
        )
    }
}
