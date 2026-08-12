package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TransactionEntity
import com.example.ui.components.SwipeDirection
import com.example.ui.components.SwipeToRevealBox
import com.example.ui.theme.DarkBg
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import java.util.Locale

fun getCategoryColorAndIcon(category: String, subcategory: String): Pair<Color, androidx.compose.ui.graphics.vector.ImageVector> {
    val text = "$category $subcategory".lowercase()
    return when {
        text.contains("кредит") || text.contains("займ") || text.contains("ипотек") || text.contains("долг") || text.contains("банк") -> Pair(Rose500, Icons.Default.AccountBalance)
        text.contains("аптек") || text.contains("лекарст") || text.contains("здоровье") || text.contains("больниц") || text.contains("врач") || text.contains("медицин") -> Pair(Rose500, Icons.Default.MedicalServices)
        text.contains("сбережен") || text.contains("копилк") || text.contains("накоплен") || text.contains("вклад") || text.contains("инвест") || text.contains("фонд") || text.contains("цель") -> Pair(Indigo500, Icons.Default.Savings)
        text.contains("янндекс") || text.contains("yandex") -> Pair(Color(0xFFFC3F1D), Icons.Default.ShoppingBag)
        text.contains("сбер") || text.contains("перевод") || text.contains("тинькофф") || text.contains("карта") || text.contains("спб") -> Pair(Emerald400, Icons.Default.Refresh)
        text.contains("продукт") || text.contains("супермаркет") || text.contains("еда") || text.contains("магнит") || text.contains("пятерочк") || text.contains("ашан") || text.contains("магазин") -> Pair(Color(0xFFF59E0B), Icons.Default.ShoppingBag)
        text.contains("кафе") || text.contains("ресторан") || text.contains("фастфуд") || text.contains("доставк") || text.contains("кофе") || text.contains("столовая") -> Pair(Color(0xFFEC4899), Icons.Default.ShoppingBag)
        text.contains("транспорт") || text.contains("такси") || text.contains("авто") || text.contains("бензин") || text.contains("заправк") || text.contains("метро") || text.contains("автобус") -> Pair(Indigo500, Icons.Default.DirectionsCar)
        text.contains("развлечени") || text.contains("кино") || text.contains("игры") || text.contains("подписк") || text.contains("музык") || text.contains("театр") || text.contains("спорт") -> Pair(Color(0xFFA855F7), Icons.Default.Theaters)
        text.contains("жилье") || text.contains("коммунал") || text.contains("дом") || text.contains("жкх") || text.contains("аренд") || text.contains("квартир") -> Pair(Color(0xFF06B6D4), Icons.Default.Home)
        text.contains("связь") || text.contains("интернет") || text.contains("телефон") || text.contains("мобильн") || text.contains("техник") -> Pair(Color(0xFF3B82F6), Icons.Default.Call)
        text.contains("зарплат") || text.contains("доход") || text.contains("преми") || text.contains("аванс") || text.contains("кэшбэк") -> Pair(Emerald400, Icons.Default.Payments)
        text.contains("одежд") || text.contains("обувь") || text.contains("гардероб") || text.contains("сумка") || text.contains("красот") || text.contains("салон") -> Pair(Color(0xFFEC4899), Icons.Default.Checkroom)
        text.contains("подарок") || text.contains("подарк") || text.contains("праздник") || text.contains("цветы") -> Pair(Rose500, Icons.Default.CardGiftcard)
        text.contains("книг") || text.contains("литератур") || text.contains("чтени") || text.contains("литрес") -> Pair(Color(0xFF8B5CF6), Icons.Default.List)
        text.contains("обучени") || text.contains("образовани") || text.contains("курсы") || text.contains("школа") -> Pair(Color(0xFF60A5FA), Icons.Default.School)
        else -> Pair(Slate400, Icons.Default.List)
    }
}

@Composable
fun TransactionRowItem(
    item: TransactionEntity,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    canDelete: Boolean = true
) {
    val numberFormat = remember {
        val symbols = java.text.DecimalFormatSymbols(Locale("ru", "RU")).apply {
            groupingSeparator = ' '
            decimalSeparator = ','
        }
        java.text.DecimalFormat("#,##0.##", symbols).apply {
            isGroupingUsed = true
        }
    }
    val isExpense = item.type == "expense"
    val prefix = if (isExpense) "-" else "+"
    val (catColor, catIcon) = getCategoryColorAndIcon(item.category, item.subcategory)
    val hasSubcategory = item.subcategory.isNotBlank() && !item.subcategory.equals(item.category, ignoreCase = true)
    val topTitle = if (hasSubcategory) item.subcategory else item.category
    val bottomTitle = if (hasSubcategory) item.category else ""

    val content = @Composable {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkBg.copy(alpha = 0.6f))
                .border(androidx.compose.foundation.BorderStroke(1.dp, Slate800.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(catColor.copy(alpha = 0.1f))
                        .border(0.5.dp, catColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = catIcon,
                        contentDescription = item.category,
                        tint = catColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topTitle,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (hasSubcategory) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (bottomTitle.isNotEmpty()) {
                        Text(
                            text = bottomTitle,
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$prefix${numberFormat.format(item.amount)} ₽",
                color = if (isExpense) Color.White else Emerald400,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (canDelete) {
        SwipeToRevealBox(
            swipeDirection = SwipeDirection.Both,
            onDelete = { onDelete(item.id) },
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
        ) {
            content()
        }
    } else {
        content()
    }
}
