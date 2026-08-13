import sys

with open("app/src/main/java/com/example/utils/VoiceInputManager.kt", "r") as f:
    content = f.read()

target = """                if (nread > 0) {
                    var sumSquare = 0.0
                    for (i in 0 until nread) {
                        val sample = buffer[i].toDouble()
                        sumSquare += sample * sample
                        val s = buffer[i]
                        audioBuffer.write(s.toInt() and 0xFF)
                        audioBuffer.write((s.toInt() shr 8) and 0xFF)
                    }
                    val rms = Math.sqrt(sumSquare / nread) / 32768.0
                    val volumeLevel = (Math.sqrt(rms) * 12.0).toFloat().coerceIn(0f, 12f)
                    _rmsDb.value = volumeLevel"""

replacement = """                if (nread > 0) {
                    var sumSquare = 0.0
                    for (i in 0 until nread) {
                        val sample = buffer[i].toDouble()
                        sumSquare += sample * sample
                        val s = buffer[i]
                        audioBuffer.write(s.toInt() and 0xFF)
                        audioBuffer.write((s.toInt() shr 8) and 0xFF)
                    }
                    val rms = Math.sqrt(sumSquare / nread) / 32768.0
                    val volumeLevel = (Math.sqrt(rms) * 12.0).toFloat().coerceIn(0f, 12f)
                    _rmsDb.value = volumeLevel

                    android.util.Log.d("[WHISPER]", "Read PCM shorts: $nread, RMS level: $rms")
                    val floatBuffer = FloatArray(nread) { i ->
                        buffer[i] / 32768.0f
                    }
                    // TODO: Передавать floatBuffer в обработчик whisper_full_default / whisperContext.benchFull()"""

idx = content.find("private fun startWhisperRecognizer")
end_idx = content.find("private fun startSherpaOnnxRecognizer")
if idx != -1 and end_idx != -1:
    whisper_block = content[idx:end_idx]
    if target in whisper_block:
        whisper_block = whisper_block.replace(target, replacement)
        content = content[:idx] + whisper_block + content[end_idx:]
        with open("app/src/main/java/com/example/utils/VoiceInputManager.kt", "w") as f:
            f.write(content)
        print("VoiceInputManager patched successfully")
    else:
        print("Target not found in whisper block")
else:
    print("Could not find startWhisperRecognizer")
