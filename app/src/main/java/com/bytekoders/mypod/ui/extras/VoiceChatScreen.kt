package com.bytekoders.mypod.ui.extras

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bytekoders.mypod.ui.components.WheelEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class ChatMessage(
    val sender: String, // "User" or "Gemini"
    val text: String,
    val timestampMs: Long = System.currentTimeMillis()
)

@Composable
fun VoiceChatScreen(
    wheelEvents: SharedFlow<WheelEvent>,
    apiKey: String,
    onSaveApiKey: (String) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }

    var inputApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var isEditingKey by remember { mutableStateOf(apiKey.isBlank()) }

    var chatMessages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("Gemini", "Hello! I am Gemini AI on your iPod. Press Center or Play/Pause to talk!")
            )
        )
    }

    var isListening by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    // Quick Prompts list for quick wheel selection
    val quickPrompts = remember {
        listOf(
            "Tell me a short music joke",
            "What makes the iPod Classic iconic?",
            "Suggest 3 classic 2000s rock albums",
            "Explain quantum physics in 1 sentence"
        )
    }
    var selectedPromptIdx by remember { mutableIntStateOf(0) }

    // Init Text-to-Speech Engine
    LaunchedEffect(Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ttsEngine = tts
            }
        }
    }

    fun speakText(text: String) {
        ttsEngine?.let { tts ->
            tts.stop()
            isSpeaking = true
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "gemini_voice")
        }
    }

    fun stopSpeaking() {
        ttsEngine?.stop()
        isSpeaking = false
    }

    // Call Gemini API
    fun sendToGemini(userMessage: String) {
        if (apiKey.isBlank()) {
            isEditingKey = true
            return
        }

        chatMessages = chatMessages + ChatMessage("User", userMessage)
        isThinking = true

        scope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

                val payload = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", userMessage)
                                })
                            })
                        })
                    })
                }

                val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBodyStr = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val jsonObj = JSONObject(responseBodyStr)
                    val candidates = jsonObj.optJSONArray("candidates")
                    val answerText = candidates
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")
                        ?: "No response received."

                    withContext(Dispatchers.Main) {
                        isThinking = false
                        chatMessages = chatMessages + ChatMessage("Gemini", answerText)
                        speakText(answerText)
                    }
                } else {
                    val errObj = runCatching { JSONObject(responseBodyStr) }.getOrNull()
                    val errMsg = errObj?.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                    withContext(Dispatchers.Main) {
                        isThinking = false
                        chatMessages = chatMessages + ChatMessage("Gemini", "Error: $errMsg")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isThinking = false
                    chatMessages = chatMessages + ChatMessage("Gemini", "Failed to connect: ${e.localizedMessage}")
                }
            }
        }
    }

    // Init Speech Recognizer
    fun startListening() {
        if (!hasMicPermission) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        stopSpeaking()

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }

                override fun onError(error: Int) {
                    isListening = false
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull()
                    if (!spokenText.isNullOrBlank()) {
                        sendToGemini(spokenText)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }
            recognizer.startListening(intent)
        } else {
            // Fallback to quick prompt
            sendToGemini(quickPrompts[selectedPromptIdx])
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
            ttsEngine?.stop()
            ttsEngine?.shutdown()
        }
    }

    // Wheel Events
    LaunchedEffect(chatMessages.size, isEditingKey) {
        wheelEvents.collect { event ->
            when (event) {
                is WheelEvent.Scroll -> {
                    if (!isEditingKey) {
                        var next = selectedPromptIdx + event.detents
                        while (next < 0) next += quickPrompts.size
                        selectedPromptIdx = next % quickPrompts.size
                    }
                }
                is WheelEvent.SelectPress -> {
                    if (isEditingKey) {
                        if (inputApiKey.isNotBlank()) {
                            onSaveApiKey(inputApiKey.trim())
                            isEditingKey = false
                        }
                    } else {
                        startListening()
                    }
                }
                is WheelEvent.PlayPausePress -> {
                    if (isSpeaking) {
                        stopSpeaking()
                    } else if (!isEditingKey) {
                        sendToGemini(quickPrompts[selectedPromptIdx])
                    }
                }
                is WheelEvent.MenuPress -> {
                    stopSpeaking()
                    speechRecognizer?.destroy()
                    onExit()
                }
                else -> {}
            }
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(8.dp)
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF6366F1), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "GEMINI AI VOICE CHAT",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isListening) "Listening..." else if (isThinking) "Thinking..." else if (isSpeaking) "Speaking..." else "Ready",
                            fontSize = 8.5.sp,
                            color = Color(0xFFA5B4FC)
                        )
                    }
                }

                if (apiKey.isBlank() || isEditingKey) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEF4444), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "KEY NEEDED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Rounded.VolumeUp else Icons.Rounded.Mic,
                        contentDescription = null,
                        tint = if (isSpeaking) Color(0xFF34D399) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // API Key editing card or Chat View
        if (isEditingKey) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Key,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter Gemini API Key",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Paste key to enable voice & text response.",
                        fontSize = 9.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputApiKey,
                        onValueChange = { inputApiKey = it },
                        placeholder = { Text("AIzaSy...", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (inputApiKey.isNotBlank()) {
                                onSaveApiKey(inputApiKey.trim())
                                isEditingKey = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                    ) {
                        Text("Save & Continue", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        } else {
            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(chatMessages) { msg ->
                    val isUser = msg.sender == "User"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(
                                    color = if (isUser) Color(0xFF4F46E5) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(6.dp)
                        ) {
                            Column {
                                Text(
                                    text = msg.sender,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUser) Color(0xFFA5B4FC) else Color(0xFF64748B)
                                )
                                Text(
                                    text = msg.text,
                                    fontSize = 10.sp,
                                    color = if (isUser) Color.White else Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Quick Prompt Selector Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Column {
                    Text(
                        text = "QUICK QUESTION (Wheel scroll to change | Play/Pause to send):",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = quickPrompts[selectedPromptIdx],
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1B4B),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
