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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// COLORS — JARVIS Theme
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

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startJuniorService()
        } else {
            statusText.value = "Permissions chahiye sir!"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        messages.add(ChatMessage(
            "Salam sir! Mein Junior hu. Permissions de kar activate karein.",
            false, "JUNIOR"
        ))

        setContent {
            JuniorApp(
                messages = messages,
                status = statusText.value,
                isListening = isListening.value,
                isSpeaking = isSpeaking.value,
                onActivate = { checkAndRequestPermissions() },
                onSendText = { text -> handleTextCommand(text) }
            )
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.VIBRATE,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            startJuniorService()
        } else {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    private fun startJuniorService() {
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
        messages.add(ChatMessage(text, true))
        val deviceControl = DeviceControl(this)

        if (BrainRouter.isDeviceCommand(text.lowercase())) {
            val result = deviceControl.execute(text)
            messages.add(ChatMessage(result, false, "DEVICE"))
        } else {
            BrainRouter.process(text) { response ->
                runOnUiThread {
                    messages.add(ChatMessage(response, false, "AI"))
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// MAIN UI
@Composable
fun JuniorApp(
    messages: List<ChatMessage>,
    status: String,
    isListening: Boolean,
    isSpeaking: Boolean,
    onActivate: () -> Unit,
    onSendText: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            JuniorHeader(status = status, isSpeaking = isSpeaking, isListening = isListening)

            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { msg ->
                    MessageBubble(msg)
                }
            }

            // Activate Button (agar service nahi chal rahi)
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

            // Input Area
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
// JARVIS HEADER
@Composable
fun JuniorHeader(status: String, isSpeaking: Boolean, isListening: Boolean) {
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
                Brush.verticalGradient(listOf(Color(0xFF0D0D1A), Color(0xFF050510)))
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // JARVIS Ring Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .rotate(rotation)
                        .border(2.dp, JBlue.copy(alpha = 0.6f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .rotate(-rotation * 0.7f)
                        .border(1.dp, JGreen.copy(alpha = 0.4f), CircleShape)
                )
                Text("🤖", fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("JUNIOR",
                    color = JBlue, fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                Text(status,
                    color = Color.Gray, fontSize = 11.sp)
            }

            // Status pill
            Row(
                modifier = Modifier
                    .background(JCard, RoundedCornerShape(20.dp))
                    .border(1.dp, JBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(dotColor, CircleShape)
                )
                Text(dotLabel, color = dotColor, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// MESSAGE BUBBLE
@Composable
fun MessageBubble(msg: ChatMessage) {
    val bgColor = if (msg.isUser) {
        Brush.linearGradient(listOf(Color(0xFF003366), Color(0xFF0055AA)))
    } else {
        Brush.linearGradient(listOf(JCard, JCard))
    }
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
                .background(bgColor, RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (msg.isUser) 16.dp else 4.dp,
                    bottomEnd = if (msg.isUser) 4.dp else 16.dp
                ))
                .border(1.dp, if (msg.isUser) Color(0xFF0066CC) else JBorder,
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
            Text(msg.text, color = Color.White,
                fontSize = 14.sp, lineHeight = 20.sp)
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

        // Send button
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
