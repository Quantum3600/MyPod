package com.trishit.mypod.ui.extras

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.trishit.mypod.ui.components.WheelEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun RecorderScreen(
    wheelEvents: SharedFlow<WheelEvent>,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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

    if (!hasPermission) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF0F4F8))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = null,
                tint = Color(0xFF1E5BB5),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Microphone Access Needed",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Grant permission to record voice memos on your iPod.",
                fontSize = 10.sp,
                color = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5BB5))
            ) {
                Text("Allow Access", fontSize = 11.sp, color = Color.White)
            }
        }
        return
    }

    // Storage directory for recordings
    val recordingsDir = remember {
        File(context.filesDir, "recordings").apply { mkdirs() }
    }

    var recordingsList by remember {
        mutableStateOf(getSavedRecordings(recordingsDir))
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingTimeMs by remember { mutableLongStateOf(0L) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var activeOutputFile by remember { mutableStateOf<File?>(null) }

    var isPlaying by remember { mutableStateOf(false) }
    var playingFile by remember { mutableStateOf<File?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var vuLevel by remember { mutableStateOf(0f) }

    // Ticker for recording timer and VU meter
    LaunchedEffect(isRecording) {
        var startTime = System.currentTimeMillis()
        while (isRecording) {
            delay(100L)
            recordingTimeMs += System.currentTimeMillis() - startTime
            startTime = System.currentTimeMillis()
            vuLevel = Random.nextFloat() * 0.8f + 0.2f
        }
        vuLevel = 0f
    }

    // Helper functions
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        mediaRecorder = null
        isRecording = false
        recordingsList = getSavedRecordings(recordingsDir)
    }

    fun startRecording() {
        if (isPlaying) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
        }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outFile = File(recordingsDir, "Memo_$timestamp.m4a")
        activeOutputFile = outFile

        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(outFile.absolutePath)
            recorder.prepare()
            recorder.start()
            mediaRecorder = recorder
            isRecording = true
            recordingTimeMs = 0L
        } catch (_: Exception) {
            recorder.release()
            isRecording = false
        }
    }

    fun playRecording(file: File) {
        if (isRecording) stopRecording()

        mediaPlayer?.release()
        val player = MediaPlayer()
        try {
            player.setDataSource(file.absolutePath)
            player.prepare()
            player.start()
            player.setOnCompletionListener {
                isPlaying = false
                playingFile = null
            }
            mediaPlayer = player
            playingFile = file
            isPlaying = true
        } catch (_: Exception) {
            player.release()
            isPlaying = false
        }
    }

    fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        playingFile = null
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) stopRecording()
            if (isPlaying) stopPlayback()
        }
    }

    // Handle Wheel Events
    LaunchedEffect(recordingsList.size, isRecording, isPlaying) {
        wheelEvents.collect { event ->
            when (event) {
                is WheelEvent.Scroll -> {
                    if (recordingsList.isNotEmpty()) {
                        var next = selectedIndex + event.detents
                        while (next < 0) next += recordingsList.size
                        selectedIndex = next % recordingsList.size
                    }
                }
                is WheelEvent.SelectPress -> {
                    if (isRecording) {
                        stopRecording()
                    } else if (recordingsList.isNotEmpty()) {
                        val file = recordingsList[selectedIndex.coerceIn(0, recordingsList.lastIndex)]
                        if (isPlaying && playingFile == file) {
                            stopPlayback()
                        } else {
                            playRecording(file)
                        }
                    } else {
                        startRecording()
                    }
                }
                is WheelEvent.PlayPausePress -> {
                    if (isRecording) {
                        stopRecording()
                    } else {
                        startRecording()
                    }
                }
                is WheelEvent.MenuPress -> {
                    if (isRecording) stopRecording()
                    if (isPlaying) stopPlayback()
                    onExit()
                }
                else -> {}
            }
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex) {
        if (recordingsList.isNotEmpty()) {
            listState.animateScrollToItem(selectedIndex.coerceIn(0, recordingsList.lastIndex))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(8.dp)
    ) {
        // Voice Memo Status Header Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isRecording) listOf(
                            Color(0xFFB91C1C),
                            Color(0xFF7F1D1D)
                        ) else listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0F172A)
                        )
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
                            .size(32.dp)
                            .background(
                                color = if (isRecording) Color.Red else Color(0xFF334155),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = if (isRecording) "RECORDING MEMO..." else if (isPlaying) "PLAYING MEMO" else "VOICE RECORDER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        val secs = (recordingTimeMs / 1000L) % 60
                        val mins = (recordingTimeMs / 60000L)
                        val timerStr = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

                        Text(
                            text = if (isRecording) timerStr else "${recordingsList.size} Saved Memos",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // VU Meter Bars
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.height(18.dp)
                ) {
                    repeat(5) { i ->
                        val barHeightFactor = if (isRecording) (vuLevel * ((i + 1) * 0.2f)).coerceIn(0.15f, 1f) else 0.15f
                        val animatedHeight by animateFloatAsState(
                            targetValue = barHeightFactor,
                            animationSpec = tween(durationMillis = 80),
                            label = "vu"
                        )

                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height((18 * animatedHeight).dp)
                                .background(
                                    if (isRecording) Color(0xFFEF4444) else Color(0xFF38BDF8),
                                    RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Instructional bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                .padding(vertical = 4.dp, horizontal = 6.dp)
        ) {
            Text(
                text = if (isRecording) "Press Center or Play/Pause to STOP" else "Press Play/Pause to RECORD | Center to PLAY",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Saved Recordings List
        if (recordingsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recorded voice memos.\nPress Play/Pause to start recording.",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
            ) {
                itemsIndexed(recordingsList) { idx, file ->
                    val isSelected = idx == selectedIndex
                    val isCurrentlyPlaying = isPlaying && playingFile == file

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .background(
                                if (isSelected) {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF3F82DB),
                                            Color(0xFF104192)
                                        )
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White,
                                            Color(0xFFFAFAFA)
                                        )
                                    )
                                }
                            )
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isCurrentlyPlaying) Icons.Rounded.PlayArrow else Icons.Rounded.Mic,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color(0xFF475569),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = file.name,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF1E293B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            val sizeKb = file.length() / 1024
                            Text(
                                text = "${sizeKb} KB",
                                fontSize = 8.5.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getSavedRecordings(dir: File): List<File> {
    return dir.listFiles { f -> f.extension == "m4a" || f.extension == "3gp" }
        ?.sortedByDescending { it.lastModified() }
        ?: emptyList()
}
