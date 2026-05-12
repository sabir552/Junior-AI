package com.junior.ai

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object BrainRouter {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val chatHistory = mutableListOf<JSONObject>()

    val SYSTEM_PROMPT = """
Tu Junior hai — ek ultra powerful personal AI assistant, bilkul JARVIS ki tarah.
- Hamesha Urdu mein jawab do jab tak English mein na poocha jaye
- Short, confident aur helpful jawab do — max 3 sentences TTS ke liye
- "Sir" kehna mat bhulo
- Seedha point pe aao, bakwaas bilkul nahi
- Agar koi device command ho toh execute karo aur confirm karo
- Tu ek supercomputer ki tarah kaam karta hai — har sawaal ka jawab hai tere paas
""".trimIndent()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SMART ROUTER — Kaunsa model use ho
    fun route(message: String): String {
        val msg = message.lowercase()
        return when {
            // Device commands → local handle
            isDeviceCommand(msg) -> "device"

            // Image/Vision → Gemini
            msg.contains("photo") || msg.contains("image") ||
            msg.contains("tasveer") || msg.contains("dekho") -> "gemini"

            // Deep reasoning → DeepSeek
            msg.contains("kyun") || msg.contains("explain") ||
            msg.contains("samjhao") || msg.length > 500 -> "deepseek"

            // Code → Mistral
            msg.contains("code") || msg.contains("python") ||
            msg.contains("javascript") || msg.contains("bug") ||
            msg.contains("error") -> "mistral"

            // Web search → Perplexity via OpenRouter
            msg.contains("news") || msg.contains("weather") ||
            msg.contains("aaj") || msg.contains("latest") ||
            msg.contains("abhi") -> "openrouter"

            // Fast simple → Cerebras
            msg.length < 50 -> "cerebras"

            // Default → Groq (best balance)
            else -> "groq"
        }
    }

    fun isDeviceCommand(msg: String): Boolean {
        val deviceWords = listOf(
            "torch", "battery", "volume", "sms", "call",
            "camera", "photo", "wifi", "bluetooth", "brightness",
            "screenshot", "alarm", "reminder", "clipboard",
            "vibrate", "notification", "flashlight", "hotspot",
            "airplane", "mobile data", "location", "gps",
            "app kholo", "app band", "music", "pause", "play"
        )
        return deviceWords.any { msg.contains(it) }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // MAIN PROCESS FUNCTION
    fun process(message: String, callback: (String) -> Unit) {
        val model = route(message)
        Log.d("Junior", "Routing to: $model")

        // History mein add karo
        chatHistory.add(JSONObject().apply {
            put("role", "user")
            put("content", message)
        })

        // Keep last 10 messages
        if (chatHistory.size > 10) {
            chatHistory.removeAt(0)
        }

        when (model) {
            "groq" -> callGroq(message, callback)
            "deepseek" -> callDeepSeek(message, callback)
            "mistral" -> callMistral(message, callback)
            "cerebras" -> callCerebras(message, callback)
            "openrouter" -> callOpenRouter(message, callback)
            "gemini" -> callGemini(message, callback)
            else -> callGroq(message, callback)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // GROQ
    fun callGroq(message: String, callback: (String) -> Unit, fast: Boolean = false) {
        val messages = JSONArray()
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", SYSTEM_PROMPT)
        })
        chatHistory.takeLast(6).forEach { messages.put(it) }

        val body = JSONObject().apply {
            put("model", if (fast) ApiConfig.GROQ_FAST_MODEL else ApiConfig.GROQ_MODEL)
            put("messages", messages)
            put("max_tokens", 200)
            put("temperature", 0.7)
        }

        makeRequest(
            url = ApiConfig.GROQ_URL,
            body = body,
            authHeader = "Bearer ${ApiConfig.GROQ_KEY}",
            callback = callback,
            fallback = { callOpenRouter(message, callback) }
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // DEEPSEEK
    private fun callDeepSeek(message: String, callback: (String) -> Unit) {
        val messages = JSONArray()
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", SYSTEM_PROMPT)
        })
        chatHistory.takeLast(6).forEach { messages.put(it) }

        val body = JSONObject().apply {
            put("model", ApiConfig.DEEPSEEK_MODEL)
            put("messages", messages)
            put("max_tokens", 200)
        }

        makeRequest(
            url = ApiConfig.DEEPSEEK_URL,
            body = body,
            authHeader = "Bearer ${ApiConfig.DEEPSEEK_KEY}",
            callback = callback,
            fallback = { callGroq(message, callback) }
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // MISTRAL
    private fun callMistral(message: String, callback: (String) -> Unit) {
        val messages = JSONArray()
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", SYSTEM_PROMPT)
        })
        chatHistory.takeLast(6).forEach { messages.put(it) }

        val body = JSONObject().apply {
            put("model", ApiConfig.MISTRAL_MODEL)
            put("messages", messages)
            put("max_tokens", 200)
        }

        makeRequest(
            url = ApiConfig.MISTRAL_URL,
            body = body,
            authHeader = "Bearer ${ApiConfig.MISTRAL_KEY}",
            callback = callback,
            fallback = { callGroq(message, callback) }
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // CEREBRAS — Ultra Fast
    private fun callCerebras(message: String, callback: (String) -> Unit) {
        val messages = JSONArray()
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", SYSTEM_PROMPT)
        })
        messages.put(JSONObject().apply {
            put("role", "user")
            put("content", message)
        })

        val body = JSONObject().apply {
            put("model", ApiConfig.CEREBRAS_MODEL)
            put("messages", messages)
            put("max_tokens", 150)
        }

        makeRequest(
            url = ApiConfig.CEREBRAS_URL,
            body = body,
            authHeader = "Bearer ${ApiConfig.CEREBRAS_KEY}",
            callback = callback,
            fallback = { callGroq(message, callback) }
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // OPENROUTER — 100+ Models Backup
    private fun callOpenRouter(message: String, callback: (String) -> Unit) {
        val messages = JSONArray()
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", SYSTEM_PROMPT)
        })
        chatHistory.takeLast(6).forEach { messages.put(it) }

        val body = JSONObject().apply {
            put("model", "meta-llama/llama-3.3-70b-instruct:free")
            put("messages", messages)
            put("max_tokens", 200)
        }

        makeRequest(
            url = ApiConfig.OPENROUTER_URL,
            body = body,
            authHeader = "Bearer ${ApiConfig.OPENROUTER_KEY}",
            callback = callback,
            fallback = { callback("Sorry sir, network issue hai.") }
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // GEMINI — Vision
    fun callGemini(message: String, callback: (String) -> Unit, imageBase64: String? = null) {
        val parts = JSONArray()
        parts.put(JSONObject().apply { put("text", message) })

        if (imageBase64 != null) {
            parts.put(JSONObject().apply {
                put("inline_data", JSONObject().apply {
                    put("mime_type", "image/jpeg")
                    put("data", imageBase64)
                })
            })
        }

        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", parts)
                })
            })
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", 300)
                put("temperature", 0.7)
            })
        }

        val url = "${ApiConfig.GEMINI_URL}/${ApiConfig.GEMINI_MODEL}:generateContent?key=${ApiConfig.GEMINI_KEY}"

        val request = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callGroq(message, callback)
            }
            override fun onResponse(call: Call, response: Response) {
                try {
                    val json = JSONObject(response.body?.string() ?: "")
                    val text = json.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    addToHistory("assistant", text)
                    callback(text)
                } catch (e: Exception) {
                    callGroq(message, callback)
                }
            }
        })
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // GENERIC REQUEST MAKER
    private fun makeRequest(
        url: String,
        body: JSONObject,
        authHeader: String,
        callback: (String) -> Unit,
        fallback: () -> Unit
    ) {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", authHeader)
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Junior", "API failed: ${e.message}")
                fallback()
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val json = JSONObject(response.body?.string() ?: "")
                    val text = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    addToHistory("assistant", text)
                    callback(text)
                } catch (e: Exception) {
                    Log.e("Junior", "Parse error: ${e.message}")
                    fallback()
                }
            }
        })
    }

    private fun addToHistory(role: String, content: String) {
        chatHistory.add(JSONObject().apply {
            put("role", role)
            put("content", content)
        })
        if (chatHistory.size > 10) chatHistory.removeAt(0)
    }

    fun clearHistory() { chatHistory.clear() }
}
