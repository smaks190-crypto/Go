import sys

with open("app/src/main/java/com/example/data/SpeechModelManager.kt", "r") as f:
    content = f.read()

# Make sure tokens.txt doesn't crash if network fails, actually it's wrapped in try/catch!
