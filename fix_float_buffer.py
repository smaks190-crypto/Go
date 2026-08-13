import sys

with open("app/src/main/java/com/example/utils/VoiceInputManager.kt", "r") as f:
    content = f.read()

duplicate = """                    android.util.Log.d("[WHISPER]", "Read PCM shorts: $readShorts, RMS level: $rms")
                    val floatBuffer = FloatArray(readShorts) { i ->
                        buffer[i] / 32768.0f
                    }
                    // TODO: Передавать floatBuffer в обработчик whisper_full_default / whisperContext.benchFull()

                    android.util.Log.d("[WHISPER]", "Read PCM shorts: $readShorts, RMS level: $rms")
                    val floatBuffer = FloatArray(readShorts) { i ->
                        buffer[i] / 32768.0f
                    }
                    // TODO: Передавать floatBuffer в обработчик whisper_full_default / whisperContext.benchFull()"""

single = """                    android.util.Log.d("[WHISPER]", "Read PCM shorts: $readShorts, RMS level: $rms")
                    val floatBuffer = FloatArray(readShorts) { i ->
                        buffer[i] / 32768.0f
                    }
                    // TODO: Передавать floatBuffer в обработчик whisper_full_default / whisperContext.benchFull()"""

content = content.replace(duplicate, single)

with open("app/src/main/java/com/example/utils/VoiceInputManager.kt", "w") as f:
    f.write(content)
