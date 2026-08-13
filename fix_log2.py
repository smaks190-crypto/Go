import sys

with open("app/src/main/java/com/example/utils/VoiceInputManager.kt", "r") as f:
    content = f.read()

idx = content.find("private fun startWhisperRecognizer")
end_idx = content.find("private fun startSherpaOnnxRecognizer")

if idx != -1 and end_idx != -1:
    block = content[idx:end_idx]
    
    target_log = 'android.util.Log.d("[WHISPER]", "Read PCM shorts: $nread, RMS level: $rms")'
    replacement_log = 'android.util.Log.d("[WHISPER]", "Read PCM shorts: $nread, RMS level: $rms") // as $readShorts'
    
    # Just to make the log exactly like prompt without changing var name:
    # Actually I will change the var name to readShorts
    block = block.replace("val nread = ", "val readShorts = ")
    block = block.replace("if (nread > 0)", "if (readShorts > 0)")
    block = block.replace("until nread)", "until readShorts)")
    block = block.replace("sumSquare / nread", "sumSquare / readShorts")
    block = block.replace("minOf(nread, 64)", "minOf(readShorts, 64)")
    block = block.replace("FloatArray(nread)", "FloatArray(readShorts)")
    block = block.replace('Read PCM shorts: $nread', 'Read PCM shorts: $readShorts')

    content = content[:idx] + block + content[end_idx:]
    with open("app/src/main/java/com/example/utils/VoiceInputManager.kt", "w") as f:
        f.write(content)
