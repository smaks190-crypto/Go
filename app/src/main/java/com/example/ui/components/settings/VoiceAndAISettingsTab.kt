package com.example.ui.components.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun VoiceAndAISettingsTab(
    initialKey: String = "",
    currentPromptMode: String = "Финансовый эксперт (Стандарт)",
    selectedSpeechEngine: com.example.utils.SpeechEngineType = com.example.utils.SpeechEngineType.GEMINI_LIVE,
    onSaveApiKey: (String) -> Unit = {},
    onPromptModeChange: (String) -> Unit = {},
    onSpeechEngineChange: (com.example.utils.SpeechEngineType) -> Unit = {},
    onVoiceSensitivityChange: (Float) -> Unit = {},
    onBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var apiKeyText by remember { mutableStateOf(initialKey) }
    var isKeyVisible by remember { mutableStateOf(false) }

    var selectedPromptMode by remember { mutableStateOf(currentPromptMode) }
    var voiceSensitivity by remember { mutableFloatStateOf(0.8f) }
    var autoRecognizeVoice by remember { mutableStateOf(true) }

    var showPromptDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Slate800.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = Slate200,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Indigo500.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Indigo500,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Голос & AI Настройки",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gemini Live API & Ключ доступа",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }
            }

            if (onClose != null) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Slate800.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = Slate200,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 1. Gemini API Key Section
        Text(
            text = "КЛЮЧ GEMINI API",
            color = Emerald400,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Персональный API-ключ Google AI Studio",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Используется для прямого потокового распознавания речи в реальном времени через WebSocket.",
                    color = Slate400,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_input"),
                    label = { Text("API Key", color = Slate400) },
                    singleLine = true,
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isKeyVisible) "Скрыть ключ" else "Показать ключ",
                                tint = Slate400
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald400,
                        unfocusedBorderColor = Slate800,
                        focusedLabelColor = Emerald400,
                        cursorColor = Emerald400,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                            context.startActivity(intent)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = Indigo500,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Получить ключ бесплатно",
                                color = Indigo500,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onSaveApiKey(apiKeyText)
                            Toast.makeText(context, "API ключ сохранен!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Сохранить",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 2. Режим Промпта AI
        Text(
            text = "РЕЖИМ АНАЛИЗА AI",
            color = Indigo500,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPromptDropdown = !showPromptDropdown },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Профиль финансового ассистента",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedPromptMode,
                            color = Emerald400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = if (showPromptDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (showPromptDropdown) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Slate800, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    val promptModes = listOf(
                        "Финансовый эксперт (Стандарт)",
                        "Строгий учет расходов",
                        "Семейный бюджет",
                        "Бизнес & ИП"
                    )

                    promptModes.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedPromptMode = mode
                                    onPromptModeChange(mode)
                                    showPromptDropdown = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mode,
                                color = if (mode == selectedPromptMode) Emerald400 else Slate200,
                                fontSize = 13.sp,
                                fontWeight = if (mode == selectedPromptMode) FontWeight.Bold else FontWeight.Normal
                            )
                            if (mode == selectedPromptMode) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Emerald400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Движок потокового распознавания речи (Gemini Live API)
        Text(
            text = "ДВИЖОК РАСПОЗНАВАНИЯ РЕЧИ (STT)",
            color = Indigo500,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Emerald400.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Gemini Multimodal Live API",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Direct PCM Streaming over WebSocket",
                            color = Emerald400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "Сырой PCM-поток с микрофона (16kHz MONO) передается напрямую в Gemini по каналу WebSocket. Локальные файлы и сторонние библиотеки не требуются.",
                    color = Slate400,
                    fontSize = 12.sp
                )
            }
        }

        // 4. Голосовой ввод
        Text(
            text = "ГОЛОСОВОЙ ВВОД ОПЕРАЦИЙ",
            color = Rose500,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Rose500.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Rose500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Автораспределение речи",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (autoRecognizeVoice) "Распознавание и разделение включено" else "Отключено",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = autoRecognizeVoice,
                        onCheckedChange = { autoRecognizeVoice = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Indigo500,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = Slate800
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Slate800, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Чувствительность микрофона",
                    color = Slate300,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = voiceSensitivity,
                    onValueChange = {
                        voiceSensitivity = it
                        onVoiceSensitivityChange(it)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Indigo500,
                        activeTrackColor = Indigo500,
                        inactiveTrackColor = Slate800
                    )
                )
            }
        }
    }
}
