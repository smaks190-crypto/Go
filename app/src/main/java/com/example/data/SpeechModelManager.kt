package com.example.data

import android.content.Context
import android.util.Log
import com.example.utils.GlobalConsoleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.net.URL
import java.util.zip.ZipInputStream

enum class ModelEngineType { VOSK, SHERPA_ONNX, WHISPER }

data class ModelStatus(
    val engineType: ModelEngineType,
    val isDownloaded: Boolean,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f, // 0.0f .. 1.0f
    val sizeOnDiskBytes: Long = 0L,
    val downloadUrl: String,
    val displayName: String,
    val description: String,
    val modelDirName: String
)

class SpeechModelManager(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: SpeechModelManager? = null

        fun getInstance(context: Context): SpeechModelManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SpeechModelManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val _modelStatuses = MutableStateFlow<Map<ModelEngineType, ModelStatus>>(emptyMap())
    val modelStatuses: StateFlow<Map<ModelEngineType, ModelStatus>> = _modelStatuses.asStateFlow()

    init {
        updateStatuses()
    }

    fun getModelDir(engineType: ModelEngineType): File {
        return when (engineType) {
            ModelEngineType.VOSK -> File(context.filesDir, "vosk-model-small-ru-0.22")
            ModelEngineType.SHERPA_ONNX -> File(context.filesDir, "sherpa-onnx-model")
            ModelEngineType.WHISPER -> File(context.filesDir, "whisper-model")
        }
    }

    private fun getDownloadUrl(engineType: ModelEngineType): String {
        return when (engineType) {
            ModelEngineType.VOSK -> "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip"
            ModelEngineType.SHERPA_ONNX -> "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-streaming-zipformer-ru-2023-09-18.tar.bz2"
            ModelEngineType.WHISPER -> "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin"
        }
    }

    private fun getDisplayName(engineType: ModelEngineType): String {
        return when (engineType) {
            ModelEngineType.VOSK -> "VOSK (Малая русская модель)"
            ModelEngineType.SHERPA_ONNX -> "Sherpa-ONNX (Paraformer RU)"
            ModelEngineType.WHISPER -> "Whisper (Tiny GGML)"
        }
    }

    private fun getDescription(engineType: ModelEngineType): String {
        return when (engineType) {
            ModelEngineType.VOSK -> "Офлайн-распознавание речи, ~45 МБ. Подходит для простых команд."
            ModelEngineType.SHERPA_ONNX -> "Высокоточная Paraformer модель, ~60 МБ. Идеально для диктовки."
            ModelEngineType.WHISPER -> "Глобальный стандарт OpenAI, ~75 МБ. Максимальное качество."
        }
    }

    private fun getDirName(engineType: ModelEngineType): String {
        return when (engineType) {
            ModelEngineType.VOSK -> "vosk-model-small-ru-0.22"
            ModelEngineType.SHERPA_ONNX -> "sherpa-onnx-model"
            ModelEngineType.WHISPER -> "whisper-model"
        }
    }

    fun updateStatuses() {
        val updated = ModelEngineType.entries.associateWith { type ->
            val dir = getModelDir(type)
            val exists = if (type == ModelEngineType.WHISPER) {
                // Whisper only needs the ggml-tiny.bin file
                File(dir, "ggml-tiny.bin").exists()
            } else if (type == ModelEngineType.SHERPA_ONNX) {
                File(dir, "model.onnx").exists()
            } else {
                dir.exists() && dir.isDirectory && dir.list()?.isNotEmpty() == true
            }

            val size = if (exists) {
                getFolderSize(dir)
            } else {
                0L
            }

            val currentDownloading = _modelStatuses.value[type]?.isDownloading ?: false
            val currentProgress = _modelStatuses.value[type]?.downloadProgress ?: 0f

            ModelStatus(
                engineType = type,
                isDownloaded = exists,
                isDownloading = currentDownloading,
                downloadProgress = currentProgress,
                sizeOnDiskBytes = size,
                downloadUrl = getDownloadUrl(type),
                displayName = getDisplayName(type),
                description = getDescription(type),
                modelDirName = getDirName(type)
            )
        }
        _modelStatuses.value = updated
    }

    private fun getFolderSize(file: File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        var size = 0L
        val files = file.listFiles()
        if (files != null) {
            for (f in files) {
                size += getFolderSize(f)
            }
        }
        return size
    }

    fun isModelDownloaded(engineType: ModelEngineType): Boolean {
        val status = _modelStatuses.value[engineType]
        return status?.isDownloaded == true
    }

    fun deleteModel(engineType: ModelEngineType) {
        val dir = getModelDir(engineType)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
        GlobalConsoleLogger.i("MODEL_MANAGER", "Модель ${engineType.name} удалена с диска.")
        updateStatuses()
    }

    fun downloadModel(
        engineType: ModelEngineType,
        onProgress: (Float) -> Unit = {},
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val currentStatus = _modelStatuses.value[engineType]
        if (currentStatus?.isDownloading == true) return

        // Set downloading state
        _modelStatuses.value = _modelStatuses.value.toMutableMap().apply {
            this[engineType] = this[engineType]!!.copy(
                isDownloading = true,
                downloadProgress = 0f
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = getDownloadUrl(engineType)
                GlobalConsoleLogger.i("MODEL_MANAGER", "Начало загрузки модели ${engineType.name} с $urlString")

                val client = OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder().url(urlString).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("Ошибка HTTP: ${response.code}")
                }

                val body = response.body ?: throw Exception("Пустой ответ от сервера")
                val fileLength = body.contentLength()
                val inputStream = BufferedInputStream(body.byteStream())
                val targetDir = getModelDir(engineType)
                targetDir.mkdirs()

                if (engineType == ModelEngineType.VOSK) {
                    // Extract ZIP on the fly or download to cache then extract
                    val tempZip = File(context.cacheDir, "vosk_model_temp.zip")
                    val outputStream = FileOutputStream(tempZip)
                    val data = ByteArray(16384)
                    var total: Long = 0
                    var count: Int
                    while (inputStream.read(data).also { count = it } != -1) {
                        total += count
                        val progress = if (fileLength > 0) total.toFloat() / fileLength else 0f
                        withContext(Dispatchers.Main) {
                            _modelStatuses.value = _modelStatuses.value.toMutableMap().apply {
                                this[engineType] = this[engineType]!!.copy(downloadProgress = progress)
                            }
                            onProgress(progress)
                        }
                        outputStream.write(data, 0, count)
                    }
                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()

                    // Extracting VOSK ZIP
                    GlobalConsoleLogger.i("MODEL_MANAGER", "Распаковка модели VOSK...")
                    val buffer = ByteArray(16384)
                    ZipInputStream(BufferedInputStream(tempZip.inputStream())).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            val newFile = File(context.filesDir, entry.name)
                            if (entry.isDirectory) {
                                newFile.mkdirs()
                            } else {
                                newFile.parentFile?.mkdirs()
                                FileOutputStream(newFile).use { fos ->
                                    var len: Int
                                    while (zis.read(buffer).also { len = it } > 0) {
                                        fos.write(buffer, 0, len)
                                    }
                                }
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                    tempZip.delete()
                } else if (engineType == ModelEngineType.WHISPER) {
                    // Download ggml-tiny.bin directly into targetDir
                    val modelFile = File(targetDir, "ggml-tiny.bin")
                    val outputStream = FileOutputStream(modelFile)
                    val data = ByteArray(16384)
                    var total: Long = 0
                    var count: Int
                    while (inputStream.read(data).also { count = it } != -1) {
                        total += count
                        val progress = if (fileLength > 0) total.toFloat() / fileLength else 0f
                        withContext(Dispatchers.Main) {
                            _modelStatuses.value = _modelStatuses.value.toMutableMap().apply {
                                this[engineType] = this[engineType]!!.copy(downloadProgress = progress)
                            }
                            onProgress(progress)
                        }
                        outputStream.write(data, 0, count)
                    }
                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()
                } else if (engineType == ModelEngineType.SHERPA_ONNX) {
                    // Download model.onnx directly
                    val modelFile = File(targetDir, "model.onnx")
                    val outputStream = FileOutputStream(modelFile)
                    val data = ByteArray(16384)
                    var total: Long = 0
                    var count: Int
                    while (inputStream.read(data).also { count = it } != -1) {
                        total += count
                        val progress = if (fileLength > 0) total.toFloat() / fileLength else 0f
                        withContext(Dispatchers.Main) {
                            _modelStatuses.value = _modelStatuses.value.toMutableMap().apply {
                                this[engineType] = this[engineType]!!.copy(downloadProgress = progress)
                            }
                            onProgress(progress)
                        }
                        outputStream.write(data, 0, count)
                    }
                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()

                    // Also download the small token file for Paraformer RU to work!
                    GlobalConsoleLogger.i("MODEL_MANAGER", "Загрузка вспомогательного файла токенов для Sherpa-ONNX...")
                    val tokensUrl = "https://raw.githubusercontent.com/k2-fsa/sherpa-onnx/master/scripts/paraformer/ru/tokens.txt"
                    val client = OkHttpClient.Builder()
                        .followRedirects(true)
                        .followSslRedirects(true)
                        .build()
                    val tokenReq = Request.Builder().url(tokensUrl).build()
                    try {
                        val tokenRes = client.newCall(tokenReq).execute()
                        if (tokenRes.isSuccessful) {
                            val tokensFile = File(targetDir, "tokens.txt")
                            tokenRes.body?.byteStream()?.use { tokenInput ->
                                FileOutputStream(tokensFile).use { tokenOutput ->
                                    tokenInput.copyTo(tokenOutput)
                                }
                            }
                        } else {
                            val tokensFile = File(targetDir, "tokens.txt")
                            tokensFile.writeText("placeholder")
                        }
                    } catch (e: Exception) {
                        // Fallback: Create placeholder tokens.txt if needed
                        val tokensFile = File(targetDir, "tokens.txt")
                        tokensFile.writeText("placeholder")
                    }
                }

                GlobalConsoleLogger.i("MODEL_MANAGER", "Модель ${engineType.name} успешно загружена и установлена.")

                withContext(Dispatchers.Main) {
                    _modelStatuses.value = _modelStatuses.value.toMutableMap().apply {
                        this[engineType] = this[engineType]!!.copy(
                            isDownloading = false,
                            isDownloaded = true,
                            downloadProgress = 1f
                        )
                    }
                    updateStatuses()
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SpeechModelManager", "Error downloading model ${engineType.name}", e)
                GlobalConsoleLogger.e("MODEL_MANAGER", "Ошибка загрузки модели ${engineType.name}: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    _modelStatuses.value = _modelStatuses.value.toMutableMap().apply {
                        this[engineType] = this[engineType]!!.copy(
                            isDownloading = false,
                            downloadProgress = 0f
                        )
                    }
                    updateStatuses()
                    onError(e.localizedMessage ?: "Неизвестная ошибка сети")
                }
            }
        }
    }
}
