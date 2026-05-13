package com.junior.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

val JBlue = Color(0xFF00D4FF)
val JDarkBlue = Color(0xFF001133)
val JBg = Color(0xFF050510)
val JCard = Color(0xFF0A0A1A)
val JBorder = Color(0xFF1A1A3A)
val JGreen = Color(0xFF00FF88)
val JRed = Color(0xFFFF4444)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val model: String = ""
)

class MainActivity : ComponentActivity() {

    private val messages = mutableStateListOf<ChatMessage>()
    private var statusText = mutableStateOf("Shuru ho raha hai...")
    private var isListening = mutableStateOf(false)
    private var isSpeaking = mutableStateOf(false)
    private var showSettings = mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            startJuniorService()
        } else {
            statusText.value = "Permissions chahiye sir!"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiConfig.init(this)

        if (!ApiConfig.keysSet()) {
            showSettings.value = true
            messages.add(ChatMessage(
                "Salam sir! Pehle API keys enter karein Settings mein. ⚙️",
                false, "JUNIOR"
            ))
        } else {
            messages.add(ChatMessage(
                "Salam sir! Mein Junior hu. Activate karein! 🚀",
                false, "JUNIOR"
            ))
        }

        setContent {
            if (showSettings.value) {
                SettingsScreen(
                    onSave = { groq, gemini, cohere, openrouter, hf, mistral, together, cerebras, deepseek ->
                        ApiConfig.saveAllKeys(
                            groq, gemini, cohere, openrouter,
                            hf, mistral, together, cerebras, deepseek
                        )
                        showSettings.value = false
                        messages.add(ChatMessage(
                            "Keys save ho gayi sir! Ab activate karein. ✅",
                            false, "JUNIOR"
                        ))
                    }
                )
            } else {
                JuniorApp(
                    messages = messages,
                    status = statusText.value,
                    isListening = isListening.value,
                    isSpeaking = isSpeaking.value,
                    onActivate = { checkAndRequestPermissions() },
                    onSendText = { text -> handleTextCommand(text) },
                    onSettings = { showSettings.value = true }
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.VIBRATE,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isEmpty()) startJuniorService()
        else permissionLauncher.launch(notGranted.toTypedArray())
    }

    private fun startJuniorService() {
        if (!ApiConfig.keysSet()) {
            showSettings.value = true
            return
        }
        val intent = Intent(this, JuniorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        statusText.value = "\"Junior\" boliye..."
        messages.add(ChatMessage(
            "Online aur taiyar hu sir! Bas 'Junior' boliye. 🎤",
            false, "JUNIOR"
        ))
    }

    private fun handleTextCommand(text: String) {
        if (!ApiConfig.keysSet()) {
            messages.add(ChatMessage("Pehle API keys enter karein sir! ⚙️", false, "JUNIOR"))
            showSettings.value = true
            return
        }
        messages.add(ChatMessage(text, true))
        val deviceControl = DeviceControl(this)
        if (BrainRouter.isDeviceCommand(text.lowercase())) {
            val result = deviceControl.execute(text)
            messages.add(ChatMessage(result, false, "DEVICE"))
        } else {
            messages.add(ChatMessage("Soch raha hu sir...", false, "AI"))
            BrainRouter.process(text) { response ->
                runOnUiThread {
                    messages.removeLastOrNull()
                    messages.add(ChatMessage(response, false, "AI"))
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SETTINGS SCREEN
@Composable
fun SettingsScreen(
    onSave: (String, String, String, String, String, String, String, String, String) -> Unit
) {
    var groq by remember { mutableStateOf(ApiConfig.GROQ_KEY) }
    var gemini by remember { mutableStateOf(ApiConfig.GEMINI_KEY) }
    var cohere by remember { mutableStateOf(ApiConfig.COHERE_KEY) }
    var openrouter by remember { mutableStateOf(ApiConfig.OPENROUTER_KEY) }
    var hf by remember { mutableStateOf(ApiConfig.HUGGINGFACE_KEY) }
    var mistral by remember { mutableStateOf(ApiConfig.MISTRAL_KEY) }
    var together by remember { mutableStateOf(ApiConfig.TOGETHER_KEY) }
    var cerebras by remember { mutableStateOf(ApiConfig.CEREBRAS_KEY) }
    var deepseek by remember { mutableStateOf(ApiConfig.DEEPSEEK_KEY) }
    var showKeys by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "⚙️ API Keys Settings",
            color = JBlue, fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 40.dp, bottom = 8.dp)
        )
        Text(
            "Apni free API keys yahan paste karein sir.",
            color = Color.Gray, fontSize = 13.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Switch(
                checked = showKeys,
                onCheckedChange = { showKeys = it },
                colors = SwitchDefaults.colors(checkedThumbColor = JBlue)
            )
            Text("Keys dikhao", color = Color.Gray, fontSize = 12.sp)
        }

        val viz = if (showKeys) VisualTransformation.None
                  else PasswordVisualTransformation()

        KeyField("Groq API Key *", groq, viz) { groq = it }
        KeyField("Gemini API Key", gemini, viz) { gemini = it }
        KeyField("OpenRouter API Key", openrouter, viz) { openrouter = it }
        KeyField("Cohere API Key", cohere, viz) { cohere = it }
        KeyField("HuggingFace API Key", hf, viz) { hf = it }
        KeyField("Mistral API Key", mistral, viz) { mistral = it }
        KeyField("Together AI Key", together, viz) { together = it }
        KeyField("Cerebras API Key", cerebras, viz) { cerebras = it }
        KeyField("DeepSeek API Key", deepseek, viz) { deepseek = it }

        Text(
            "* Groq key zaroori hai — baaki optional hain",
            color = Color.Gray, fontSize = 11.sp
        )

        Button(
            onClick = {
                onSave(groq, gemini, cohere, openrouter, hf, mistral, together, cerebras, deepseek)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = JBlue),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("Keys Save Karo ✅", color = JDarkBlue,
                fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun KeyField(
    label: String,
    value: String,
    viz: VisualTransformation,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, color = Color.Gray, fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = viz,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = JBlue,
            unfocusedBorderColor = JBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = JBlue,
            focusedLabelColor = JBlue
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// MAIN APP UI
@Composable
fun JuniorApp(
    messages: List<ChatMessage>,
    status: String,
    isListening: Boolean,
    isSpeaking: Boolean,
    onActivate: () -> Unit,
    onSendText: (String) -> Unit,
    onSettings: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Box(modifier = Modifier.fillMaxSize().background(JBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            JuniorHeader(
                status = status,
                isSpeaking = isSpeaking,
                isListening = isListening,
                onSettings = onSettings
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { msg -> MessageBubble(msg) }
            }

            if (!JuniorService.isRunning) {
                Button(
                    onClick = onActivate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = JBlue),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Junior Activate Karo 🚀",
                        color = JDarkBlue, fontWeight = FontWeight.Bold)
                }
            }

            InputArea(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        onSendText(inputText)
                        inputText = ""
                    }
                }
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// HEADER
@Composable
fun JuniorHeader(
    status: String,
    isSpeaking: Boolean,
    isListening: Boolean,
    onSettings: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "rotate"
    )
    val dotColor = when {
        isSpeaking -> JBlue
        isListening -> JRed
        else -> JGreen
    }
    val dotLabel = when {
        isSpeaking -> "BOL..."
        isListening -> "SUN..."
        else -> "READY"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0D0D1A), JBg))
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                Box(modifier = Modifier.size(48.dp).rotate(rotation)
                    .border(2.dp, JBlue.copy(alpha = 0.6f), CircleShape))
                Box(modifier = Modifier.size(36.dp).rotate(-rotation * 0.7f)
                    .border(1.dp, JGreen.copy(alpha = 0.4f), CircleShape))
                Text("🤖", fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("JUNIOR", color = JBlue, fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                Text(status, color = Color.Gray, fontSize = 11.sp)
            }

            Row(
                modifier = Modifier
                    .background(JCard, RoundedCornerShape(20.dp))
                    .border(1.dp, JBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(modifier = Modifier.size(7.dp).background(dotColor, CircleShape))
                Text(dotLabel, color = dotColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            // Settings button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(JCard, CircleShape)
                    .border(1.dp, JBorder, CircleShape)
                    .clickable { onSettings() },
                contentAlignment = Alignment.Center
            ) {
                Text("⚙️", fontSize = 16.sp)
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// MESSAGE BUBBLE
@Composable
fun MessageBubble(msg: ChatMessage) {
    val modelIcons = mapOf(
        "GROQ" to "⚡", "GEMINI" to "🔵", "DEVICE" to "📱",
        "AI" to "🧠", "JUNIOR" to "🤖", "DEEPSEEK" to "🔮",
        "MISTRAL" to "🌟", "CEREBRAS" to "⚡"
    )
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    if (msg.isUser)
                        Brush.linearGradient(listOf(Color(0xFF003366), Color(0xFF0055AA)))
                    else
                        Brush.linearGradient(listOf(JCard, JCard)),
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (msg.isUser) 16.dp else 4.dp,
                        bottomEnd = if (msg.isUser) 4.dp else 16.dp
                    )
                )
                .border(1.dp,
                    if (msg.isUser) Color(0xFF0066CC) else JBorder,
                    RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            if (!msg.isUser && msg.model.isNotEmpty()) {
                Text(
                    "${modelIcons[msg.model] ?: "🤖"} ${msg.model}",
                    color = Color.Gray, fontSize = 9.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(msg.text, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// INPUT AREA
@Composable
fun InputArea(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF080815))
            .border(1.dp, JBorder)
            .padding(10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Ya type karein...", color = Color.Gray, fontSize = 14.sp) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JBlue.copy(alpha = 0.5f),
                unfocusedBorderColor = JBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = JBlue
            ),
            shape = RoundedCornerShape(24.dp),
            maxLines = 3
        )
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF003366), Color(0xFF0066FF))),
                    CircleShape
                )
                .clickable { onSend() },
            contentAlignment = Alignment.Center
        ) {
            Text("➤", color = Color.White, fontSize = 18.sp)
        }
    }
}
