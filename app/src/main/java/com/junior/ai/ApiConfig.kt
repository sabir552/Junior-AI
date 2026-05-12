package com.junior.ai

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.dataStore by preferencesDataStore(name = "junior_prefs")

object ApiConfig {
    // BuildConfig se keys aayengi (GitHub Secrets se)
    var GROQ_KEY = BuildConfig.GROQ_API_KEY
    var GEMINI_KEY = BuildConfig.GEMINI_API_KEY
    var COHERE_KEY = BuildConfig.COHERE_API_KEY
    var OPENROUTER_KEY = BuildConfig.OPENROUTER_API_KEY
    var HUGGINGFACE_KEY = BuildConfig.HUGGINGFACE_API_KEY
    var MISTRAL_KEY = BuildConfig.MISTRAL_API_KEY
    var TOGETHER_KEY = BuildConfig.TOGETHER_API_KEY
    var CEREBRAS_KEY = BuildConfig.CEREBRAS_API_KEY
    var DEEPSEEK_KEY = BuildConfig.DEEPSEEK_API_KEY

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
