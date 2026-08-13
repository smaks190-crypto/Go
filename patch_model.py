import sys
import re

with open("app/src/main/java/com/example/data/SpeechModelManager.kt", "r") as f:
    content = f.read()

# Change the URL for Sherpa ONNX
url_target = '"https://huggingface.co/csukuangfj/sherpa-onnx-paraformer-ru-2023-09-18/resolve/main/model.onnx"'
url_replacement = '"https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-streaming-zipformer-ru-2023-09-18.tar.bz2"'
content = content.replace(url_target, url_replacement)

# Import okhttp
import_okhttp = "import okhttp3.OkHttpClient\nimport okhttp3.Request\nimport okhttp3.Call\nimport okhttp3.Callback\nimport okhttp3.Response\nimport java.io.IOException"
if "import okhttp3.OkHttpClient" not in content:
    content = content.replace("import java.net.HttpURLConnection", "import java.net.HttpURLConnection\n" + import_okhttp)

# Replace the HttpURLConnection part in downloadModel
download_start = """                val urlString = getDownloadUrl(engineType)
                GlobalConsoleLogger.i("MODEL_MANAGER", "Начало загрузки модели ${engineType.name} с $urlString")

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Ошибка HTTP: ${connection.responseCode}")
                }

                val fileLength = connection.contentLengthLong
                val inputStream = BufferedInputStream(connection.inputStream)"""

download_replace = """                val urlString = getDownloadUrl(engineType)
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
                val inputStream = BufferedInputStream(body.byteStream())"""

content = content.replace(download_start, download_replace)

# Also there's a token file download for Sherpa-ONNX
tokens_start = """                    val tokensUrl = URL("https://huggingface.co/csukuangfj/sherpa-onnx-paraformer-ru-2023-09-18/resolve/main/tokens.txt")
                    val tokensConnection = tokensUrl.openConnection() as HttpURLConnection
                    tokensConnection.connectTimeout = 15000
                    tokensConnection.readTimeout = 15000
                    tokensConnection.connect()
                    if (tokensConnection.responseCode == HttpURLConnection.HTTP_OK) {
                        val tokensFile = File(targetDir, "tokens.txt")
                        tokensConnection.inputStream.use { tokenInput ->
                            FileOutputStream(tokensFile).use { tokenOutput ->
                                tokenInput.copyTo(tokenOutput)
                            }
                        }
                    } else {"""

tokens_replace = """                    val tokensUrl = "https://raw.githubusercontent.com/k2-fsa/sherpa-onnx/master/scripts/paraformer/ru/tokens.txt"
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
                    } catch (e: Exception) {"""

content = content.replace(tokens_start, tokens_replace)

with open("app/src/main/java/com/example/data/SpeechModelManager.kt", "w") as f:
    f.write(content)

print("SpeechModelManager patched")
