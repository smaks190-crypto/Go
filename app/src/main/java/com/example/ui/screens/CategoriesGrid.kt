package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.CategoryEntity
import com.example.ui.components.formatFullCurrency
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun CategoriesGrid(
    categoryExpenseTotals: Map<String, Double>,
    categoriesList: List<CategoryEntity>,
    onShowLimitsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultCategories = listOf(
        Triple("Гейминг", 22000.0, Emerald400 to Icons.Default.SportsEsports),
        Triple("Бары", 4800.0, Indigo500 to Icons.Default.LocalBar),
        Triple("Транспорт", 1420.0, Rose500 to Icons.Default.DirectionsCar),
        Triple("Продукты", 1150.0, Emerald400 to Icons.Default.Restaurant),
        Triple("Форс-мажор", 300.0, Indigo500 to Icons.Default.Warning)
    )

    val displayCategories = remember(categoryExpenseTotals, categoriesList) {
        if (categoryExpenseTotals.isNotEmpty()) {
            val colors = listOf(Emerald400, Indigo500, Rose500, Color(0xFFF59E0B), Color(0xFF06B6D4))
            val realSorted = categoryExpenseTotals.entries.sortedByDescending { it.value }.take(5)
            val realItems = realSorted.mapIndexed { idx, entry ->
                val (col, ic) = when {
                    entry.key.contains("игра", true) || entry.key.contains("гейм", true) || entry.key.contains("аниме", true) -> Emerald400 to Icons.Default.SportsEsports
                    entry.key.contains("бар", true) || entry.key.contains("тусов", true) || entry.key.contains("алко", true) -> Indigo500 to Icons.Default.LocalBar
                    entry.key.contains("такси", true) || entry.key.contains("транс", true) || entry.key.contains("авто", true) -> Rose500 to Icons.Default.DirectionsCar
                    entry.key.contains("продукт", true) || entry.key.contains("еда", true) || entry.key.contains("кафе", true) -> Emerald400 to Icons.Default.Restaurant
                    entry.key.contains("форс", true) || entry.key.contains("авар", true) -> Indigo500 to Icons.Default.Warning
                    else -> colors[idx % colors.size] to Icons.Default.ShoppingBag
                }
                Triple(entry.key, entry.value, col to ic)
            }

            if (realItems.size < 5) {
                val remainingDb = categoriesList
                    .filter { it.type == "expense" && realItems.none { item -> item.first.equals(it.name, true) } }
                    .mapIndexed { index, cat ->
                        val (col, ic) = when {
                            cat.name.contains("игра", true) || cat.name.contains("гейм", true) || cat.name.contains("аниме", true) -> Emerald400 to Icons.Default.SportsEsports
                            cat.name.contains("бар", true) || cat.name.contains("тусов", true) || cat.name.contains("алко", true) -> Indigo500 to Icons.Default.LocalBar
                            cat.name.contains("такси", true) || cat.name.contains("транс", true) || cat.name.contains("авто", true) -> Rose500 to Icons.Default.DirectionsCar
                            cat.name.contains("продукт", true) || cat.name.contains("еда", true) || cat.name.contains("кафе", true) -> Emerald400 to Icons.Default.Restaurant
                            cat.name.contains("форс", true) || cat.name.contains("авар", true) -> Indigo500 to Icons.Default.Warning
                            else -> colors[(realItems.size + index) % colors.size] to Icons.Default.ShoppingBag
                        }
                        Triple(cat.name, 0.0, col to ic)
                    }
                (realItems + remainingDb).take(5)
            } else {
                realItems
            }
        } else {
            val colors = listOf(Emerald400, Indigo500, Rose500, Color(0xFFF59E0B), Color(0xFF06B6D4))
            val expenseDbCats = categoriesList.filter { it.type == "expense" }
            if (expenseDbCats.isNotEmpty()) {
                expenseDbCats.mapIndexed { idx, cat ->
                    val (col, ic) = when {
                        cat.name.contains("игра", true) || cat.name.contains("гейм", true) || cat.name.contains("аниме", true) -> Emerald400 to Icons.Default.SportsEsports
                        cat.name.contains("бар", true) || cat.name.contains("тусов", true) || cat.name.contains("алко", true) -> Indigo500 to Icons.Default.LocalBar
                        cat.name.contains("такси", true) || cat.name.contains("транс", true) || cat.name.contains("авто", true) -> Rose500 to Icons.Default.DirectionsCar
                        cat.name.contains("продукт", true) || cat.name.contains("еда", true) || cat.name.contains("кафе", true) -> Emerald400 to Icons.Default.Restaurant
                        cat.name.contains("форс", true) || cat.name.contains("авар", true) -> Indigo500 to Icons.Default.Warning
                        else -> colors[idx % colors.size] to Icons.Default.ShoppingBag
                    }
                    Triple(cat.name, 0.0, col to ic)
                }.take(5)
            } else {
                defaultCategories.map { Triple(it.first, 0.0, it.third) }
            }
        }
    }

    val maxCategoryVal = remember(displayCategories) {
        displayCategories.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "КАТЕГОРИИ",
                color = Slate300,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Все категории",
                color = Indigo500,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onShowLimitsClick() }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            displayCategories.take(3).forEach { (name, amount, colorAndIcon) ->
                val (color, icon) = colorAndIcon
                val ratio = (amount / maxCategoryVal).toFloat().coerceIn(0.12f, 1f)

                Card(
                    onClick = { onShowLimitsClick() },
                    colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(118.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(color.copy(alpha = 0.12f))
                                .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = name,
                            color = Slate300,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 12.sp,
                            modifier = Modifier.height(26.dp)
                        )

                        Text(
                            text = formatFullCurrency(amount),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF020617))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = ratio)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            displayCategories.drop(3).take(2).forEach { (name, amount, colorAndIcon) ->
                val (color, icon) = colorAndIcon
                val ratio = (amount / maxCategoryVal).toFloat().coerceIn(0.12f, 1f)

                Card(
                    onClick = { onShowLimitsClick() },
                    colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(118.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(color.copy(alpha = 0.12f))
                                .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = name,
                            color = Slate300,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 12.sp,
                            modifier = Modifier.height(26.dp)
                        )

                        Text(
                            text = formatFullCurrency(amount),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF020617))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = ratio)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }

            Card(
                onClick = { onShowLimitsClick() },
                colors = CardDefaults.cardColors(containerColor = Indigo500.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Indigo500.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(118.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Indigo500),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Добавить",
                        color = Indigo500,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
