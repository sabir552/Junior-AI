package com.junior.ai

import android.content.Context
import android.util.Log

object ApiConfig {
    // Keys — pehle BuildConfig se, agar empty ho to error
    var GROQ_KEY = ""
    var GEMINI_KEY = ""
    var COHERE_KEY = ""
    var OPENROUTER_KEY = ""
    var HUGGINGFACE_KEY = ""
    var MISTRAL_KEY = ""
    var TOGETHER_KEY = ""
    var CEREBRAS_KEY = ""
    var DEEPSEEK_KEY = ""

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        
        // BuildConfig se load karo
        try {
            GROQ_KEY = BuildConfig.GROQ_API_KEY
            GEMINI_KEY = BuildConfig.GEMINI_API_KEY
            COHERE_KEY = BuildConfig.COHERE_API_KEY
            OPENROUTER_KEY = BuildConfig.OPENROUTER_API_KEY
            HUGGINGFACE_KEY = BuildConfig.HUGGINGFACE_API_KEY
            MISTRAL_KEY = BuildConfig.MISTRAL_API_KEY
            TOGETHER_KEY = BuildConfig.TOGETHER_API_KEY
            CEREBRAS_KEY = BuildConfig.CEREBRAS_API_KEY
            DEEPSEEK_KEY = BuildConfig.DEEPSEEK_API_KEY
        } catch (e: Exception) {
            Log.e("Junior", "BuildConfig load error: ${e.message}")
        }

        // Debug logging
        Log.d("Junior", "=== API KEYS CHECK ===")
        Log.d("Junior", "GROQ: ${if (GROQ_KEY.isEmpty()) "EMPTY!" else GROQ_KEY.take(15)}...")
        Log.d("Junior", "GEMINI: ${if (GEMINI_KEY.isEmpty()) "EMPTY!" else GEMINI_KEY.take(15)}...")
        Log.d("Junior", "OPENROUTER: ${if (OPENROUTER_KEY.isEmpty()) "EMPTY!" else OPENROUTER_KEY.take(15)}...")

        // Agar sab empty hain to warning
        if (GROQ_KEY.isEmpty() && GEMINI_KEY.isEmpty() && OPENROUTER_KEY.isEmpty()) {
            Log.e("Junior", "❌ ALL API KEYS ARE EMPTY!")
            Log.e("Junior", "APK mein keys properly embed nahi hui!")
        }

        initialized = true
    }

    // Models
    const val GROQ_MODEL = "llama-3.3-70b-versatile"
    const val GROQ_FAST_MODEL = "llama-3.1-8b-instant"
    const val GEMINI_MODEL = "gemini-1.5-flash"
    const val MISTRAL_MODEL = "mistral-large-latest"
    const val DEEPSEEK_MODEL = "deepseek-chat"
    const val CEREBRAS_MODEL = "llama3.1-70b"

    // Endpoints
    const val GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
    const val GROQ_STT_URL = "https://api.groq.com/openai/v1/audio/transcriptions"
    const val GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    const val COHERE_URL = "https://api.cohere.ai/v1/chat"
    const val OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
    const val MISTRAL_URL = "https://api.mistral.ai/v1/chat/completions"
    const val DEEPSEEK_URL = "https://api.deepseek.com/chat/completions"
    const val CEREBRAS_URL = "https://api.cerebras.ai/v1/chat/completions"
    const val TOGETHER_URL = "https://api.together.xyz/v1/chat/completions"
    const val HUGGINGFACE_URL = "https://api-inference.huggingface.co/models"
}
