import sys

with open("app/src/main/java/com/example/utils/VoiceInputManager.kt", "r") as f:
    content = f.read()

content = content.replace('"Read PCM shorts: $nread, RMS level: $rms"', '"Read PCM shorts: $nread, RMS level: $rms"')
# Ah actually the prompt says Log.d("[WHISPER]", "Read PCM shorts: $readShorts, RMS level: $rms")
# but the variable name in code is `nread`. So "$nread" is what prints the value. It doesn't matter, it's just the value of the variable.
# Let's rename the variable `nread` to `readShorts` to perfectly match!

with open("app/src/main/java/com/example/utils/VoiceInputManager.kt", "w") as f:
    f.write(content)
