import sys

with open("app/src/main/java/com/example/ui/components/settings/VoiceAndAISettingsTab.kt", "r") as f:
    content = f.read()

# Replace imports
imports_target = """import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ModelEngineType
import com.example.data.SpeechModelManager"""

imports_replacement = """import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.example.data.ModelEngineType
import com.example.data.SpeechModelManager"""

content = content.replace(imports_target, imports_replacement)

# Replace the STT section
stt_start_marker = "        // 3. Движок распознавания речи"
stt_end_marker = "        // 4. Голосовой ввода"

start_idx = content.find(stt_start_marker)
end_idx = content.find(stt_end_marker)

if start_idx != -1 and end_idx != -1:
    new_stt_section = """        // 3. Движок распознавания речи и модели
        val modelManager = remember { SpeechModelManager.getInstance(context) }
        val modelStatuses by modelManager.modelStatuses.collectAsState()

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

                AnimatedVisibility(
                    visible = showEngineDropdown,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Slate800, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            com.example.utils.SpeechEngineType.entries.forEach { engine ->
                                val isSelected = activeEngine == engine
                                val borderAlpha = if (isSelected) 0.8f else 0.2f
                                val borderColor = if (isSelected) Emerald400 else Slate800
                                val containerBg = if (isSelected) Emerald400.copy(alpha = 0.08f) else DarkBg

                                val modelEngineType = when (engine) {
                                    com.example.utils.SpeechEngineType.VOSK -> ModelEngineType.VOSK
                                    com.example.utils.SpeechEngineType.SHERPA_ONNX -> ModelEngineType.SHERPA_ONNX
                                    com.example.utils.SpeechEngineType.WHISPER -> ModelEngineType.WHISPER
                                    com.example.utils.SpeechEngineType.NATIVE -> null
                                }
                                val status = modelEngineType?.let { modelStatuses[it] }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(containerBg)
                                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                        .clickable {
                                            activeEngine = engine
                                            onSpeechEngineChange(engine)
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
                                                text = status?.displayName ?: engine.displayName,
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
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = status?.description ?: engine.description,
                                            color = Slate400,
                                            fontSize = 11.sp
                                        )

                                        if (status != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                when {
                                                    status.isDownloading -> {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.weight(1f)
                                                        ) {
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
                                                                    .padding(horizontal = 8.dp)
                                                                    .height(4.dp)
                                                                    .clip(CircleShape),
                                                                color = Emerald400,
                                                                trackColor = Slate800
                                                            )
                                                        }
                                                    }
                                                    status.isDownloaded -> {
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
                                                    else -> {
                                                        Text(
                                                            text = "Статус: не скачана",
                                                            color = Slate500,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }

                                                Row(horizontalArrangement = Arrangement.End) {
                                                    if (status.isDownloaded) {
                                                        IconButton(
                                                            onClick = {
                                                                modelManager.deleteModel(modelEngineType)
                                                                Toast.makeText(context, "Модель удалена", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Удалить модель",
                                                                tint = Rose500,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    } else if (!status.isDownloading) {
                                                        IconButton(
                                                            onClick = {
                                                                modelManager.downloadModel(
                                                                    engineType = modelEngineType,
                                                                    onSuccess = {
                                                                        Toast.makeText(context, "Модель успешно скачана!", Toast.LENGTH_SHORT).show()
                                                                    },
                                                                    onError = { err ->
                                                                        Toast.makeText(context, "Ошибка загрузки: $err", Toast.LENGTH_LONG).show()
                                                                    }
                                                                )
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Download,
                                                                contentDescription = "Скачать модель",
                                                                tint = Indigo500,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            activeEngine = engine
                                            onSpeechEngineChange(engine)
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
        }

"""
    content = content[:start_idx] + new_stt_section + content[end_idx:]

with open("app/src/main/java/com/example/ui/components/settings/VoiceAndAISettingsTab.kt", "w") as f:
    f.write(content)

print("Done")
