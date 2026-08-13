import sys

with open("app/src/main/java/com/example/data/SpeechModelManager.kt", "r") as f:
    content = f.read()

tokens_start = """                    val tokensUrl = "https://raw.githubusercontent.com/k2-fsa/sherpa-onnx/master/scripts/paraformer/ru/tokens.txt"
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
                    }"""

if "tokens.txt" in content:
    print("Tokens part is there.")
