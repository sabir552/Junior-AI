package com.junior.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class VoiceManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsSpeaking = false
    var onWakeWord: (() -> Unit)? = null
    var onCommand: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var isListeningForCommand = false

    init {
        initTts()
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TTS INIT
    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("ur", "PK")
                tts?.setSpeechRate(0.9f)
                tts?.setPitch(0.95f)
                Log.d("Junior", "TTS ready")
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SPEAK
    fun speak(text: String, onDone: (() -> Unit)? = null) {
        val clean = text
            .replace("*", "").replace("#", "")
            .replace(Regex("[❌✅🔋📸🎤🤖⚡🔵🟢🔴]"), "")
            .trim()

        if (clean.isEmpty()) {
            onDone?.invoke()
            return
        }

        isTtsSpeaking = true
        Log.d("Junior", "Speaking: $clean")

        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "junior_utterance")

        tts?.setOnUtteranceProgressListener(object :
            android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { isTtsSpeaking = true }
            override fun onDone(utteranceId: String?) {
                isTtsSpeaking = false
                onDone?.invoke()
            }
            override fun onError(utteranceId: String?) {
                isTtsSpeaking = false
                onDone?.invoke()
            }
        })
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // START WAKE WORD LISTENING
    fun startWakeWordListening() {
        if (isTtsSpeaking) return
        isListeningForCommand = false
        startListening(isWakeWord = true)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // START COMMAND LISTENING
    fun startCommandListening() {
        isListeningForCommand = true
        startListening(isWakeWord = false)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // CORE LISTENING
    private fun startListening(isWakeWord: Boolean) {
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur-PK")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ur-PK")
            putExtra(RecognizerIntent.EXTRA_ALSO_LANGUAGE_PREFERENCE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            if (isWakeWord) {
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500L)
            }
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("Junior", if (isWakeWord) "Wake listening..." else "Command listening...")
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()?.lowercase() ?: return

                if (isWakeWord && containsWakeWord(partial)) {
                    Log.d("Junior", "Wake word detected in partial!")
                    speechRecognizer?.stopListening()
                    onWakeWord?.invoke()
                }
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: return

                Log.d("Junior", "Result: $text")

                if (isWakeWord) {
                    if (containsWakeWord(text.lowercase())) {
                        onWakeWord?.invoke()
                    } else {
                        // Dobara sun
                        startWakeWordListening()
                    }
                } else {
                    // Command mila
                    val command = text
                        .replace("جونیار", "", ignoreCase = true)
                        .replace("junior", "", ignoreCase = true)
                        .trim()
                    if (command.isNotEmpty()) {
                        onCommand?.invoke(command)
                    } else {
                        onError?.invoke("Kuch suna nahi sir")
                    }
                }
            }

            override fun onError(error: Int) {
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "no_match"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "timeout"
                    SpeechRecognizer.ERROR_AUDIO -> "audio_error"
                    else -> "error_$error"
                }
                Log.d("Junior", "STT Error: $msg")

                if (isWakeWord) {
                    // Error pe bhi dobara sun — hamesha active
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (!isTtsSpeaking) startWakeWordListening()
                    }, 500)
                } else {
                    onError?.invoke("Dobara boliye sir")
                }
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("Junior", "Listen error: ${e.message}")
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // WAKE WORD CHECK
    private fun containsWakeWord(text: String): Boolean {
        val wakeWords = listOf(
            "junior", "جونیار", "جونئیر",
            "junio", "yunior", "younior"
        )
        return wakeWords.any { text.contains(it, ignoreCase = true) }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    fun destroy() {
        stopListening()
        tts?.shutdown()
        tts = null
    }

    fun isSpeaking() = isTtsSpeaking
}
