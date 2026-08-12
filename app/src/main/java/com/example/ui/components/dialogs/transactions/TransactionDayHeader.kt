package com.example.ui.components.dialogs.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.formatFullCurrency
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Slate400

@Composable
fun TransactionDayHeader(
    dateLabel: String,
    dayTotalExpense: Double,
    dayTotalIncome: Double,
    filterType: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateLabel,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        if (filterType == "income" && dayTotalIncome > 0) {
            Text(
                text = "+${formatFullCurrency(dayTotalIncome)}",
                color = Emerald400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        } else if (dayTotalExpense > 0) {
            Text(
                text = "-${formatFullCurrency(dayTotalExpense)}",
                color = Slate400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
