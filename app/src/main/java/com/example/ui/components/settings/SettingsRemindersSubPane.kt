package com.example.ui.components.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notifications.ReminderManager
import com.example.ui.components.WheelPicker
import com.example.ui.theme.DarkBg
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

private const val REQUEST_CODE_POST_NOTIFICATIONS = 101

@Composable
fun SettingsRemindersSubContent(
    onBack: () -> Unit,
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(ReminderManager.isReminderEnabled(context)) }
    val initialTime = remember { ReminderManager.getReminderTime(context) }
    var hour by remember { mutableIntStateOf(initialTime.first) }
    var minute by remember { mutableIntStateOf(initialTime.second) }

    fun checkAndRequestPermission(onGranted: () -> Unit = {}) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                if (context is Activity) {
                    androidx.core.app.ActivityCompat.requestPermissions(context, arrayOf(permission), REQUEST_CODE_POST_NOTIFICATIONS)
                }
            } else {
                onGranted()
            }
        } else {
            onGranted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Напоминания",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = Slate400
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ежедневное напоминание",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Уведомление о внесении расходов за день",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        isEnabled = checked
                        if (checked) {
                            checkAndRequestPermission {
                                ReminderManager.setReminderEnabled(context, true, hour, minute)
                                ReminderManager.scheduleDailyReminder(context, hour, minute)
                            }
                        } else {
                            ReminderManager.setReminderEnabled(context, false)
                            ReminderManager.cancelDailyReminder(context)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Indigo500
                    )
                )
            }
        }

        if (isEnabled) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Slate900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Время напоминания",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        WheelPicker(
                            items = (0..23).map { String.format("%02d", it) },
                            initialIndex = hour,
                            onItemSelected = { selectedHour ->
                                hour = selectedHour
                                ReminderManager.setReminderEnabled(context, true, hour, minute)
                                ReminderManager.scheduleDailyReminder(context, hour, minute)
                            }
                        )

                        Text(
                            text = ":",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        WheelPicker(
                            items = (0..59).map { String.format("%02d", it) },
                            initialIndex = minute,
                            onItemSelected = { selectedMinute ->
                                minute = selectedMinute
                                ReminderManager.setReminderEnabled(context, true, hour, minute)
                                ReminderManager.scheduleDailyReminder(context, hour, minute)
                            }
                        )
                    }
                }
            }
        }
    }
}
