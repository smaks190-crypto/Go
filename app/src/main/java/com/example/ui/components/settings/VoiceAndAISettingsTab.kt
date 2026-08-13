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
import com.example.data.ModelEngineType
import com.example.data.SpeechModelManager
import com.example.ui.theme.*

@Composable
fun VoiceAndAISettingsTab(
    initialKey: String = "",
    currentPromptMode: String = "Финансовый эксперт (Стандарт)",
    selectedSpeechEngine: com.example.utils.SpeechEngineType = com.example.utils.SpeechEngineType.SHERPA_ONNX,
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
    var activeEngine by remember(selectedSpeechEngine) { mutableStateOf(selectedSpeechEngine) }
    var voiceSensitivity by remember { mutableFloatStateOf(0.8f) }
    var autoRecognizeVoice by remember { mutableStateOf(true) }

    var showPromptDropdown by remember { mutableStateOf(false) }
    var showEngineDropdown by remember { mutableStateOf(false) }

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
                        .background(Indigo500.copy(alpha = 0.15f))
                        .border(1.dp, Indigo500.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Indigo500,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "ГОЛОС И ИИ-ПОМОЩНИК",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Gemini API, промпты и голосовой ассистент",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
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
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Divider(color = Slate800, thickness = 1.dp)

        // 1. Gemini API Key Section
        Text(
            text = "GEMINI API КЛЮЧ",
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
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Как бесплатно получить API ключ:",
                    color = Indigo500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. Перейдите на aistudio.google.com/app/apikey\n" +
                            "2. Войдите в Google аккаунт\n" +
                            "3. Нажмите «Create API key»",
                    color = Slate300,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
                ) {
                    Text(
                        text = "Получить API ключ в Google AI Studio ↗",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "ВАШ КЛЮЧ API",
                    color = Slate400,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    placeholder = { Text("AIzaSy...", color = Slate400) },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBg,
                        unfocusedContainerColor = DarkBg,
                        focusedBorderColor = Indigo500,
                        unfocusedBorderColor = Slate800,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onSaveApiKey(apiKeyText)
                        Toast.makeText(
                            context,
                            if (apiKeyText.isNotBlank()) "API ключ сохранен" else "Ключ очищен",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald400)
                ) {
                    Text(
                        text = "Сохранить API ключ",
                        color = DarkBg,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. ИИ-Промпт и Роль Ассистента
        Text(
            text = "РОЛЬ И ПРОМПТ ИИ-ПОМОЩНИКА",
            color = Emerald400,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPromptDropdown = !showPromptDropdown },
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
                                .background(Emerald400.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Характер и промпт ассистента",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedPromptMode,
                                color = Emerald400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Icon(
                        imageVector = if (showPromptDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (showPromptDropdown) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Slate800, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    val promptModes = listOf(
                        "Финансовый эксперт (Стандарт)",
                        "Строгий аудит накоплений",
                        "Киберпанк финансовый советник",
                        "Лаконичный формат (Короткие ответы)"
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
                                .padding(vertical = 8.dp, horizontal = 10.dp),
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

        // 3. Движок распознавания речи
        Text(
            text = "ДВИЖОК РАСПОЗНАВАНИЯ РЕЧИ (STT)",
            color = Indigo500,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showEngineDropdown = !showEngineDropdown },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(Indigo500.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Indigo500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Выбор алгоритма STT",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Активный: ${activeEngine.displayName}",
                                color = Indigo500,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Icon(
                        imageVector = if (showEngineDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (showEngineDropdown) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Slate800, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        com.example.utils.SpeechEngineType.entries.forEach { engine ->
                            val isSelected = activeEngine == engine
                            val borderAlpha = if (isSelected) 0.8f else 0.2f
                            val borderColor = if (isSelected) Emerald400 else Slate800
                            val containerBg = if (isSelected) Emerald400.copy(alpha = 0.08f) else DarkBg

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(containerBg)
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .clickable {
                                        activeEngine = engine
                                        onSpeechEngineChange(engine)
                                        showEngineDropdown = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = engine.displayName,
                                            color = if (isSelected) Emerald400 else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (engine == com.example.utils.SpeechEngineType.SHERPA_ONNX) {
                                            Surface(
                                                color = Indigo500.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "ТОР",
                                                    color = Indigo500,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = engine.description,
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        activeEngine = engine
                                        onSpeechEngineChange(engine)
                                        showEngineDropdown = false
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Emerald400,
                                        unselectedColor = Slate800
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- ОФЛАЙН-МОДЕЛИ РАСПОЗНАВАНИЯ ---
        val modelManager = remember { SpeechModelManager.getInstance(context) }
        val modelStatuses by modelManager.modelStatuses.collectAsState()

        Text(
            text = "УПРАВЛЕНИЕ ОФЛАЙН-МОДЕЛЯМИ",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModelEngineType.entries.forEach { engineType ->
                    val status = modelStatuses[engineType]
                    if (status != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkBg)
                                .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = status.displayName,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = status.description,
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))

                                when {
                                    status.isDownloading -> {
                                        CircularProgressIndicator(
                                            progress = { status.downloadProgress },
                                            modifier = Modifier.size(24.dp),
                                            color = Emerald400,
                                            trackColor = Slate800,
                                            strokeWidth = 2.5.dp
                                        )
                                    }
                                    status.isDownloaded -> {
                                        IconButton(
                                            onClick = {
                                                modelManager.deleteModel(engineType)
                                                Toast.makeText(context, "Модель удалена", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Удалить модель",
                                                tint = Rose500,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    else -> {
                                        IconButton(
                                            onClick = {
                                                modelManager.downloadModel(
                                                    engineType = engineType,
                                                    onSuccess = {
                                                        Toast.makeText(context, "Модель успешно скачана!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    onError = { err ->
                                                        Toast.makeText(context, "Ошибка загрузки: $err", Toast.LENGTH_LONG).show()
                                                    }
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = "Скачать модель",
                                                tint = Indigo500,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (status.isDownloading || status.isDownloaded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (status.isDownloading) {
                                        Text(
                                            text = "Загрузка: ${(status.downloadProgress * 100).toInt()}%",
                                            color = Emerald400,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        LinearProgressIndicator(
                                            progress = { status.downloadProgress },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 10.dp)
                                                .height(4.dp)
                                                .clip(CircleShape),
                                            color = Emerald400,
                                            trackColor = Slate800
                                        )
                                    } else {
                                        val formattedSize = String.format(java.util.Locale.US, "%.1f MB", status.sizeOnDiskBytes.toDouble() / (1024 * 1024))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Emerald400,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Скачано • $formattedSize",
                                                color = Emerald400,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Статус: не скачана",
                                    color = Slate500,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Голосовой ввода
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
