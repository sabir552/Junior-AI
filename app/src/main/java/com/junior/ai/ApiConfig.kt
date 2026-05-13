package com.junior.ai

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object ApiConfig {

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("junior_keys", Context.MODE_PRIVATE)
        logKeys()
    }

    // Keys SharedPreferences se aayengi
    var GROQ_KEY: String
        get() = prefs.getString("GROQ_KEY", "") ?: ""
        set(v) = prefs.edit().putString("GROQ_KEY", v).apply()

    var GEMINI_KEY: String
        get() = prefs.getString("GEMINI_KEY", "") ?: ""
        set(v) = prefs.edit().putString("GEMINI_KEY", v).apply()

    var COHERE_KEY: String
        get() = prefs.getString("COHERE_KEY", "") ?: ""
        set(v) = prefs.edit().putString("COHERE_KEY", v).apply()

    var OPENROUTER_KEY: String
        get() = prefs.getString("OPENROUTER_KEY", "") ?: ""
        set(v) = prefs.edit().putString("OPENROUTER_KEY", v).apply()

    var HUGGINGFACE_KEY: String
        get() = prefs.getString("HUGGINGFACE_KEY", "") ?: ""
        set(v) = prefs.edit().putString("HUGGINGFACE_KEY", v).apply()

    var MISTRAL_KEY: String
        get() = prefs.getString("MISTRAL_KEY", "") ?: ""
        set(v) = prefs.edit().putString("MISTRAL_KEY", v).apply()

    var TOGETHER_KEY: String
        get() = prefs.getString("TOGETHER_KEY", "") ?: ""
        set(v) = prefs.edit().putString("TOGETHER_KEY", v).apply()

    var CEREBRAS_KEY: String
        get() = prefs.getString("CEREBRAS_KEY", "") ?: ""
        set(v) = prefs.edit().putString("CEREBRAS_KEY", v).apply()

    var DEEPSEEK_KEY: String
        get() = prefs.getString("DEEPSEEK_KEY", "") ?: ""
        set(v) = prefs.edit().putString("DEEPSEEK_KEY", v).apply()

    fun keysSet(): Boolean = GROQ_KEY.isNotEmpty() || OPENROUTER_KEY.isNotEmpty()

    fun saveAllKeys(
        groq: String, gemini: String, cohere: String,
        openrouter: String, huggingface: String, mistral: String,
        together: String, cerebras: String, deepseek: String
    ) {
        GROQ_KEY = groq.trim()
        GEMINI_KEY = gemini.trim()
        COHERE_KEY = cohere.trim()
        OPENROUTER_KEY = openrouter.trim()
        HUGGINGFACE_KEY = huggingface.trim()
        MISTRAL_KEY = mistral.trim()
        TOGETHER_KEY = together.trim()
        CEREBRAS_KEY = cerebras.trim()
        DEEPSEEK_KEY = deepseek.trim()
        logKeys()
    }

    private fun logKeys() {
        Log.d("Junior", "=== KEYS ===")
        Log.d("Junior", "GROQ: ${if(GROQ_KEY.isEmpty()) "EMPTY" else GROQ_KEY.take(12)}...")
        Log.d("Junior", "GEMINI: ${if(GEMINI_KEY.isEmpty()) "EMPTY" else GEMINI_KEY.take(12)}...")
        Log.d("Junior", "OPENROUTER: ${if(OPENROUTER_KEY.isEmpty()) "EMPTY" else OPENROUTER_KEY.take(12)}...")
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
