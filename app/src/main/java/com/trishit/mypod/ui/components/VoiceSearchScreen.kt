package com.trishit.mypod.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.SharedFlow
import java.util.Locale

@Composable
fun VoiceSearchScreen(
    searchTargetTitle: String,
    onResultFound: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    wheelEvents: SharedFlow<WheelEvent>? = null
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var rmsLevel by remember { mutableFloatStateOf(0.15f) }
    var recognizedQuery by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    var hasPermission by remember {
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
        hasPermission = granted
    }

    fun stopListeningSafely() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        isListening = false
    }

    fun startListening() {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        stopListeningSafely()
        errorMessage = null
        isSearching = false

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    errorMessage = null
                }
                override fun onBeginningOfSpeech() {
                    isListening = true
                }
                override fun onRmsChanged(rmsdB: Float) {
                    if (isListening) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.15f, 1.0f)
                        rmsLevel = normalized
                    }
                }
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }
                override fun onError(error: Int) {
                    isListening = false
                    rmsLevel = 0.15f
                    errorMessage = when (error) {
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. Press CENTER to try again."
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Speak clearly."
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Try again."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network issue. Check connection."
                        else -> "Speech recognition paused. Press CENTER to speak."
                    }
                }
                override fun onResults(results: Bundle?) {
                    isListening = false
                    rmsLevel = 0.15f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull()?.trim()
                    if (!spokenText.isNullOrBlank()) {
                        recognizedQuery = spokenText
                        isSearching = true
                        onResultFound(spokenText)
                    } else {
                        errorMessage = "No speech detected. Press CENTER to try again."
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak search query...")
            }
            recognizer.startListening(intent)
            isListening = true
        } else {
            errorMessage = "Voice recognition service unavailable on device."
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            startListening()
        }
    }

    // Handle Wheel Events
    LaunchedEffect(wheelEvents) {
        wheelEvents?.collect { event ->
            when (event) {
                is WheelEvent.MenuPress -> {
                    stopListeningSafely()
                    onCancel()
                }
                is WheelEvent.SelectPress -> {
                    if (isListening) {
                        stopListeningSafely()
                    } else if (!isSearching) {
                        startListening()
                    }
                }
                is WheelEvent.PlayPausePress -> {
                    if (isListening) {
                        stopListeningSafely()
                    } else if (!isSearching) {
                        startListening()
                    }
                }
                else -> {}
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopListeningSafely()
        }
    }

    // Pulse animation for recording state
    val transition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8ECEF))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = if (isListening) {
                            listOf(Color(0xFFC62828), Color(0xFF8E0000))
                        } else if (isSearching) {
                            listOf(Color(0xFF00796B), Color(0xFF004D40))
                        } else {
                            listOf(Color(0xFF3273B5), Color(0xFF1C4B82))
                        }
                    )
                )
                .padding(vertical = 8.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🎙️ VOICE SEARCH • $searchTargetTitle",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                if (isListening) {
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🔴 REC",
                            color = Color(0xFFD32F2F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else if (isSearching) {
                    Text(
                        text = "🔍 SEARCHING",
                        color = Color.Yellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Center Voice Recording Indicator & Visualizer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .scale(if (isListening) 1f + (rmsLevel * 0.15f) else 1f)
                    .clip(CircleShape)
                    .background(
                        when {
                            isListening -> Color(0xFFE53935)
                            isSearching -> Color(0xFF00897B)
                            else -> Color(0xFF546E7A)
                        }
                    )
                    .border(
                        width = if (isListening) 4.dp else 2.dp,
                        color = if (isListening) Color.White else Color(0xFFB0BEC5),
                        shape = CircleShape
                    )
                    .clickable {
                        if (isListening) stopListeningSafely() else if (!isSearching) startListening()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSearching) Icons.Rounded.Search else Icons.Rounded.Mic,
                    contentDescription = "Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sound Wave Equalizer Bars
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .height(30.dp)
                    .fillMaxWidth()
            ) {
                val barMultipliers = listOf(0.4f, 0.7f, 1.0f, 0.8f, 1.0f, 0.7f, 0.4f)
                barMultipliers.forEachIndexed { index, mult ->
                    val barHeight = if (isListening) {
                        (10 + (rmsLevel * 20 * mult)).dp
                    } else if (isSearching) {
                        (12 + ((index % 3) * 6)).dp
                    } else {
                        4.dp
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .width(6.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isListening) Color(0xFFE53935)
                                else if (isSearching) Color(0xFF00897B)
                                else Color(0xFF90A4AE)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Message Box
            Text(
                text = when {
                    isListening -> "🔴 RECORDING... Speak now"
                    isSearching -> "🔍 Searching for: \"$recognizedQuery\"..."
                    errorMessage != null -> errorMessage!!
                    else -> "Press CENTER to start recording"
                },
                color = if (isListening) Color(0xFFB71C1C) else Color(0xFF212121),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Controls Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = "Voice Controls",
                        tint = Color(0xFF1C4B82),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!hasPermission) "Permission Required" else "iPod Wheel Controls",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (!hasPermission) {
                        "Press CENTER to grant microphone permission."
                    } else {
                        "• CENTER / PLAY: Start or Pause recording\n• MENU: Cancel & Return to iPod Menu"
                    },
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
