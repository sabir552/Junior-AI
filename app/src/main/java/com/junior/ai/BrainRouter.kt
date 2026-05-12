package com.junior.ai

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object BrainRouter {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val chatHistory = mutableListOf<JSONObject>()

    val SYSTEM_PROMPT = """
Tu Junior hai — ek ultra powerful personal AI assistant, bilkul JARVIS ki tarah.
- Hamesha Urdu mein jawab do jab tak English mein na poocha jaye
- Short, confident aur helpful jawab do — max 3 sentences TTS ke liye
- Sir kehna mat bhulo
- Seedha point pe aao, bakwaas bilkul nahi
- Agar koi device command ho toh execute karo aur confirm karo
- Tu ek supercomputer ki tarah kaam karta hai
""".trimIndent()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SMART ROUTER
    fun route(message: String): String {
        val msg = message.lowercase()
        return when {
            isDeviceCommand(msg) -> "device"
            msg.contains("photo") || msg.contains("image") ||
            msg.contains("tasveer") -> "gemini"
            msg.contains("kyun") || msg.contains("explain") ||
            msg.contains("samjhao") || msg.length > 500 -> "deepseek"
            msg.contains("code") || msg.contains("python") ||
            msg.contains("javascript") || msg.contains("bug") -> "mistral"
            msg.contains("news") || msg.contains("weather") ||
            msg.contains("latest") -> "openrouter"
            msg.length < 50 -> "cerebras"
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
            "app kholo", "app band", "music", "pause", "play",
            "torch on", "torch off", "torch chala", "torch band"
        )
        return deviceWords.any { msg.contains(it) }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // MAIN PROCESS
    fun process(message: String, callback: (String) -> Unit) {
        val model = route(message)
        Log.d("Junior", "=== Processing: $message ===")
        Log.d("Junior", "=== Routing to: $model ===")

        chatHistory.add(JSONObject().apply {
            put("role", "user")
            put("content", message)
        })
        if (chatHistory.size > 10) chatHistory.removeAt(0)

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
    fun callGroq(message: String, callback: (String) -> Unit) {
        Log.d("Junior", "Calling Groq...")
        Log.d("Junior", "Groq Key: ${ApiConfig.GROQ_KEY.take(10)}...")

        val messages = buildMessages(message)
        val body = JSONObject().apply {
            put("model", ApiConfig.GROQ_MODEL)
            put("messages", messages)
            put("max_tokens", 200)
            put("temperature", 0.7)
        }

        makeRequest(
            url = ApiConfig.GROQ_URL,
            body = body,
            authHeader = "Bearer ${ApiConfig.GROQ_KEY}",
            callback = callback,
            fallback = {
                Log.e("Junior", "Groq failed — trying OpenRouter")
                callOpenRouter(message, callback)
            }
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // DEEPSEEK
    private fun callDeepSeek(message: String, callback: (String) -> Unit) {
        Log.d("Junior", "Calling DeepSeek...")
        val messages = buildMessages(message)
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
        Log.d("Junior", "Calling Mistral...")
        val messages = buildMessages(message)
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
    // CEREBRAS
    private fun callCerebras(message: String, callback: (String) -> Unit) {
        Log.d("Junior", "Calling Cerebras...")
        val messages = buildMessages(message)
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
    // OPENROUTER
    private fun callOpenRouter(message: String, callback: (String) -> Unit) {
        Log.d("Junior", "Calling OpenRouter...")
        val messages = buildMessages(message)
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
            fallback = { callback("Sir network issue hai, thodi der baad try karein.") }
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // GEMINI
    fun callGemini(message: String, callback: (String) -> Unit, imageBase64: String? = null) {
        Log.d("Junior", "Calling Gemini...")
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
                put(JSONObject().apply { put("parts", parts) })
            })
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", 300)
                put("temperature", 0.7)
            })
        }

        val url = "${ApiConfig.GEMINI_URL}/${ApiConfig.GEMINI_MODEL}" +
                ":generateContent?key=${ApiConfig.GEMINI_KEY}"

        Log.d("Junior", "Gemini URL: $url")

        val request = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Junior", "Gemini FAIL: ${e.message}")
                callGroq(message, callback)
            }

            override fun onResponse(call: Call, response: Response) {
                val rawBody = response.body?.string() ?: ""
                Log.d("Junior", "Gemini Response code: ${response.code}")
                Log.d("Junior", "Gemini Response: $rawBody")
                try {
                    val json = JSONObject(rawBody)
                    if (json.has("candidates")) {
                        val text = json.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")
                        addToHistory("assistant", text)
                        callback(text)
                    } else if (json.has("error")) {
                        val errMsg = json.getJSONObject("error").getString("message")
                        Log.e("Junior", "Gemini API Error: $errMsg")
                        callGroq(message, callback)
                    } else {
                        Log.e("Junior", "Gemini unknown: $rawBody")
                        callGroq(message, callback)
                    }
                } catch (e: Exception) {
                    Log.e("Junior", "Gemini parse error: ${e.message}")
                    callGroq(message, callback)
                }
            }
        })
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // GENERIC REQUEST
    private fun makeRequest(
        url: String,
        body: JSONObject,
        authHeader: String,
        callback: (String) -> Unit,
        fallback: () -> Unit
    ) {
        Log.d("Junior", "Making request to: $url")

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", authHeader)
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://github.com/sabir552/Junior-AI")
            .addHeader("X-Title", "Junior AI")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Junior", "REQUEST FAILED: ${e.message}")
                Log.e("Junior", "URL was: $url")
                fallback()
            }

            override fun onResponse(call: Call, response: Response) {
                val rawBody = response.body?.string() ?: ""
                Log.d("Junior", "Response code: ${response.code}")
                Log.d("Junior", "Response body: $rawBody")

                try {
                    val json = JSONObject(rawBody)
                    when {
                        json.has("choices") -> {
                            val text = json.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                            Log.d("Junior", "Success! Response: $text")
                            addToHistory("assistant", text)
                            callback(text)
                        }
                        json.has("error") -> {
                            val errMsg = json.getJSONObject("error")
                                .getString("message")
                            Log.e("Junior", "API Error: $errMsg")
                            callback("Sir API error aya: $errMsg")
                        }
                        else -> {
                            Log.e("Junior", "Unknown response format: $rawBody")
                            fallback()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Junior", "Parse error: ${e.message}")
                    Log.e("Junior", "Raw body was: $rawBody")
                    fallback()
                }
            }
        })
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // HELPERS
    private fun buildMessages(message: String): JSONArray {
        val messages = JSONArray()
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", SYSTEM_PROMPT)
        })
        chatHistory.takeLast(6).forEach { messages.put(it) }
        messages.put(JSONObject().apply {
            put("role", "user")
            put("content", message)
        })
        return messages
    }

    private fun addToHistory(role: String, content: String) {
        chatHistory.add(JSONObject().apply {
            put("role", role)
            put("content", content)
        })
        if (chatHistory.size > 10) chatHistory.removeAt(0)
    }

    fun clearHistory() {
        chatHistory.clear()
    }
}
