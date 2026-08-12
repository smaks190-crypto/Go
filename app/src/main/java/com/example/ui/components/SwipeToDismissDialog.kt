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


val LocalDialogSwipeEnabled = androidx.compose.runtime.compositionLocalOf {
    androidx.compose.runtime.mutableStateOf(true)
}


fun formatDayHeaderLabel(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return dateStr

        val todayCal = Calendar.getInstance()
        val targetCal = Calendar.getInstance().apply { time = date }

        val isSameYear = todayCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR)
        val todayDayOfYear = todayCal.get(Calendar.DAY_OF_YEAR)
        val targetDayOfYear = targetCal.get(Calendar.DAY_OF_YEAR)

        if (isSameYear && todayDayOfYear == targetDayOfYear) {
            "Сегодня"
        } else if (isSameYear && todayDayOfYear - targetDayOfYear == 1) {
            "Вчера"
        } else {
            val pattern = if (isSameYear) "d MMMM" else "d MMMM yyyy"
            val rawStr = SimpleDateFormat(pattern, Locale("ru", "RU")).format(date)
            rawStr.split(" ").mapIndexed { idx, word ->
                if (idx == 1) word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru", "RU")) else it.toString() }
                else word
            }.joinToString(" ")
        }
    } catch (e: Exception) {
        dateStr
    }
}

private const val REQUEST_CODE_POST_NOTIFICATIONS = 101

@Composable
fun SwipeToDismissDialog(
    onDismissRequest: () -> Unit,
    isAtTop: () -> Boolean = { true },
    properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false,
        decorFitsSystemWindows = false
    ),
    contentPadding: PaddingValues = PaddingValues(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
    content: @Composable () -> Unit
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    var dragStartedAtTop by remember { mutableStateOf(false) }
    val swipeEnabledState = remember { mutableStateOf(true) }

    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dialog_swipe_offset"
    )

    val nestedScrollConnection = remember(isAtTop, swipeEnabledState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!swipeEnabledState.value) return Offset.Zero

                if (source == NestedScrollSource.Drag && offsetY == 0f) {
                    dragStartedAtTop = isAtTop()
                }

                if (offsetY > 0f && available.y < 0f) {
                    val newOffset = (offsetY + available.y).coerceAtLeast(0f)
                    val consumedY = newOffset - offsetY
                    offsetY = newOffset
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!swipeEnabledState.value) return Offset.Zero

                if (source == NestedScrollSource.Drag && dragStartedAtTop && isAtTop() && available.y > 0f) {
                    offsetY += available.y
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (!swipeEnabledState.value) return Velocity.Zero
                dragStartedAtTop = false
                if (offsetY > 120f) {
                    offsetY = 0f
                    onDismissRequest()
                } else {
                    offsetY = 0f
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (!swipeEnabledState.value) return Velocity.Zero
                dragStartedAtTop = false
                if (offsetY > 120f) {
                    offsetY = 0f
                    onDismissRequest()
                } else {
                    offsetY = 0f
                }
                return Velocity.Zero
            }
        }
    }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        androidx.activity.compose.BackHandler(onBack = onDismissRequest)

        androidx.compose.runtime.CompositionLocalProvider(LocalDialogSwipeEnabled provides swipeEnabledState) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismissRequest() }
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .imePadding()
                    .padding(contentPadding)
                    .nestedScroll(nestedScrollConnection),
                contentAlignment = Alignment.BottomCenter
            ) {
                val maxAllowedHeight = maxHeight - 8.dp
                androidx.compose.animation.AnimatedVisibility(
                    visible = isVisible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    enter = androidx.compose.animation.slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)
                    ) + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 250)
                    ) + androidx.compose.animation.fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = maxAllowedHeight)
                            .animateContentSize(
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessMediumLow,
                                    dampingRatio = Spring.DampingRatioNoBouncy
                                )
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { 
                                focusManager.clearFocus()
                                com.example.ui.components.SwipeToRevealController.requestCollapseAll()
                            }
                            .offset { IntOffset(0, animatedOffsetY.roundToInt().coerceAtLeast(0)) },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        content()
                    }
                }
            }
        }
    }
}
