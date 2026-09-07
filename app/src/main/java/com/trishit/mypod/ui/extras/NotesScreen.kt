package com.trishit.mypod.ui.extras

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.mypod.ui.components.WheelEvent
import kotlinx.coroutines.flow.SharedFlow

data class NoteItem(
    val id: String,
    val title: String,
    val dateStr: String,
    val content: String
)

@Composable
fun NotesScreen(
    wheelEvents: SharedFlow<WheelEvent>,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sampleNotes = remember {
        listOf(
            NoteItem(
                id = "n1",
                title = "Welcome to MyPod",
                dateStr = "Mar 30, 2025",
                content = """Welcome to MyPod — the pixel-accurate iPod Classic music player for Android!

Features:
• Authentic Click Wheel with rotary touchpad gestures and tactile click haptics.
• Multi-source audio playback: Local MediaStore files, SAF Storage Explorer, Spotify Web API, and YouTube Music.
• 3D Parallax album artwork with tilt-sensor accelerometer physics.
• Auto-scrolling LRCLIB synced lyrics engine.
• 3D Interactive Cover Flow album visualizer.
• Classic retro iPod Click-Wheel mini-games: Brick Breaker, Snake, Solitaire, Parachute, and Music Quiz!"""
            ),
            NoteItem(
                id = "n2",
                title = "iPod Classic History",
                dateStr = "Oct 23, 2001",
                content = """On October 23, 2001, Apple introduced the original iPod with the tagline: '1,000 songs in your pocket.'

Key Innovations:
1. Mechanical & Touch Scroll Wheel for rapid navigation through thousands of songs.
2. High-contrast monochrome LCD screen with Chicago font.
3. 5GB hard drive storage.
4. Seamless album, artist, and playlist organization."""
            ),
            NoteItem(
                id = "n3",
                title = "Click Wheel Controls",
                dateStr = "Mar 28, 2025",
                content = """Click Wheel Control Quick Reference:

• Rotary Swipe: Rotate finger around the wheel ring to scroll lists, adjust volume, or scrub track position.
• Center Button: Select menu item, toggle play/pause on Now Playing, launch/restart games.
• MENU Button: Go back one step in navigation hierarchy, cancel selections, or return from games/apps.
• Play/Pause Button: Quick toggle play/pause from anywhere.
• Hold Events: Press and hold MENU or Play/Pause for extra shortcuts."""
            ),
            NoteItem(
                id = "n4",
                title = "Track Notes & Lyrics",
                dateStr = "Mar 25, 2025",
                content = """Synced Lyrics & Artwork:

• Now Playing screen automatically fetches synced LRC timestamps from LRCLIB.
• Tap center button on Now Playing screen or swipe rotary wheel to toggle between 3D Artwork Parallax and Lyrics Mode.
• Lyrics auto-scroll smoothly with active line highlighting."""
            )
        )
    }

    var selectedNoteIndex by remember { mutableIntStateOf(0) }
    var activeNoteDetail by remember { mutableStateOf<NoteItem?>(null) }
    val scrollState = rememberScrollState()

    // Collect Wheel Events
    LaunchedEffect(Unit) {
        wheelEvents.collect { event ->
            when (event) {
                is WheelEvent.Scroll -> {
                    val detail = activeNoteDetail
                    if (detail != null) {
                        // Scroll note text content
                        val newScroll = (scrollState.value + event.detents * 40).coerceIn(0, scrollState.maxValue)
                        scrollState.scrollTo(newScroll)
                    } else {
                        // Scroll note list
                        var next = selectedNoteIndex + event.detents
                        while (next < 0) next += sampleNotes.size
                        selectedNoteIndex = next % sampleNotes.size
                    }
                }
                is WheelEvent.SelectPress -> {
                    if (activeNoteDetail != null) {
                        // Exit detail view back to notes list
                        activeNoteDetail = null
                    } else {
                        // Open selected note
                        activeNoteDetail = sampleNotes[selectedNoteIndex.coerceIn(0, sampleNotes.lastIndex)]
                    }
                }
                is WheelEvent.MenuPress -> {
                    if (activeNoteDetail != null) {
                        activeNoteDetail = null
                    } else {
                        onExit()
                    }
                }
                else -> {}
            }
        }
    }

    val note = activeNoteDetail
    if (note != null) {
        // Full Note Reader View
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFFFFDF5)) // Retro paper tint
                .padding(8.dp)
        ) {
            // Note Title Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF3F82DB), Color(0xFF104192))
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = note.dateStr,
                        fontSize = 8.5.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Scrollable Note Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = note.content,
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }
        }
    } else {
        // Notes Directory List
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            sampleNotes.forEachIndexed { idx, item ->
                val isSelected = idx == selectedNoteIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .background(
                            if (isSelected) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF3F82DB),
                                        Color(0xFF1E5BB5),
                                        Color(0xFF104192)
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFFFFF),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF111111),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = item.dateStr,
                            fontSize = 8.5.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF888888),
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Open Note",
                            tint = if (isSelected) Color.White else Color(0xFF888888),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
