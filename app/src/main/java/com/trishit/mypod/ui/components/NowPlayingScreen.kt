package com.trishit.mypod.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.mypod.data.lyrics.LrcLine
import com.trishit.mypod.data.lyrics.LyricsResult
import com.trishit.mypod.source.NowPlayingState
import java.util.Locale

enum class NowPlayingDisplayMode {
    ARTWORK,
    LYRICS
}

@Composable
fun NowPlayingScreen(
    state: NowPlayingState,
    modifier: Modifier = Modifier,
    lyricsResult: LyricsResult? = null,
    isLoadingLyrics: Boolean = false,
    sensorPitch: Float = 0f,
    sensorRoll: Float = 0f,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    onToggleLyricsMode: (() -> Unit)? = null
) {
    val track = state.currentTrack
    var displayMode by remember { mutableStateOf(NowPlayingDisplayMode.ARTWORK) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF2F4F7),
                        Color(0xFFE5E8ED)
                    )
                )
            )
            .padding(8.dp)
    ) {
        if (track == null) {
            // Empty State
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = "No Track",
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Track Playing",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select music from the menu to start playback",
                        fontSize = 10.sp,
                        color = Color(0xFF777777),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Header Line: Queue Index, Mode Toggle & Source Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val totalInQueue = state.queue.size.coerceAtLeast(1)
                val queueIndexText = "${state.queueIndex + 1} of $totalInQueue"
                Text(
                    text = queueIndexText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF444444)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Favorite / Heart Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isFavorite) Color(0xFFE53935) else Color(0xFFDDDDDD)
                            )
                            .clickable {
                                onToggleFavorite?.invoke()
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (isFavorite) Color.White else Color(0xFF444444),
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Mode Toggle (Lyrics vs Artwork)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (displayMode == NowPlayingDisplayMode.LYRICS) Color(0xFF2B5B9E) else Color(0xFFDDDDDD)
                            )
                            .clickable {
                                displayMode = if (displayMode == NowPlayingDisplayMode.ARTWORK) {
                                    NowPlayingDisplayMode.LYRICS
                                } else {
                                    NowPlayingDisplayMode.ARTWORK
                                }
                                onToggleLyricsMode?.invoke()
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Lyrics,
                                contentDescription = "Toggle Lyrics",
                                tint = if (displayMode == NowPlayingDisplayMode.LYRICS) Color.White else Color(0xFF444444),
                                modifier = Modifier.size(11.dp)
                            )
                            if (isLoadingLyrics) {
                                Spacer(modifier = Modifier.width(3.dp))
                                CircularProgressIndicator(
                                    color = if (displayMode == NowPlayingDisplayMode.LYRICS) Color.White else Color(0xFF2B5B9E),
                                    strokeWidth = 1.dp,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Badge for source
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF2B5B9E))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = state.playbackSourceType.badge,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Display Area: 3D Artwork OR Synced Lyrics
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (displayMode == NowPlayingDisplayMode.ARTWORK) {
                    // 3D Parallax Artwork Mode
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 3D Parallax Artwork Component
                        ParallaxArtworkView(
                            artUri = track.artUri,
                            sensorPitch = sensorPitch,
                            sensorRoll = sensorRoll,
                            onClick = {
                                displayMode = NowPlayingDisplayMode.LYRICS
                            },
                            modifier = Modifier
                                .fillMaxHeight(0.95f)
                                .weight(0.55f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Track Details
                        Column(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = track.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = track.artist,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF333333),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = track.album,
                                fontSize = 9.5.sp,
                                color = Color(0xFF666666),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    // Synced Lyrics Mode
                    LyricsView(
                        trackTitle = track.title,
                        artistName = track.artist,
                        positionMs = state.positionMs,
                        lyricsResult = lyricsResult,
                        isLoading = isLoadingLyrics
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Area: Retro Glossy Progress Bar & Timers
            val posMs = state.positionMs
            val durMs = state.durationMs.coerceAtLeast(1L)
            val progressFraction = (posMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f)
            val remainingMs = (durMs - posMs).coerceAtLeast(0L)

            Column(modifier = Modifier.fillMaxWidth()) {
                // Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFD0D4D9))
                        .border(1.dp, Color(0xFF9E9E9E), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF5A94E8),
                                        Color(0xFF2266C7),
                                        Color(0xFF114291)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Timers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTimeMs(posMs),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "-${formatTimeMs(remainingMs)}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricsView(
    trackTitle: String,
    artistName: String,
    positionMs: Long,
    lyricsResult: LyricsResult?,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF181A20))
            .padding(6.dp)
    ) {
        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF5A94E8),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Searching LRCLIB lyrics...",
                        fontSize = 10.sp,
                        color = Color(0xFFAAAABB)
                    )
                }
            }
            lyricsResult == null || (!lyricsResult.isFound) -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lyrics,
                        contentDescription = null,
                        tint = Color(0xFF666677),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No lyrics found",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$trackTitle - $artistName",
                        fontSize = 9.sp,
                        color = Color(0xFF888899),
                        textAlign = TextAlign.Center
                    )
                }
            }
            lyricsResult.syncedLines.isNotEmpty() -> {
                // Synced LRC Lines
                val lines = lyricsResult.syncedLines
                val activeIndex = remember(positionMs, lines) {
                    findActiveLineIndex(lines, positionMs)
                }
                val listState = rememberLazyListState()

                LaunchedEffect(activeIndex) {
                    if (activeIndex >= 0) {
                        listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0))
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(lines) { index, line ->
                        val isActive = index == activeIndex
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isActive) {
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF2B5B9E),
                                                Color(0xFF1A3B69)
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(Color.Transparent, Color.Transparent)
                                        )
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = line.text.ifEmpty { "♪" },
                                fontSize = if (isActive) 11.5.sp else 10.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive) Color.White else Color(0xFF888899),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            !lyricsResult.plainLyrics.isNullOrBlank() -> {
                // Plain Unsynced Lyrics
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = lyricsResult.plainLyrics,
                            fontSize = 10.5.sp,
                            color = Color(0xFFDDDDDD),
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun findActiveLineIndex(lines: List<LrcLine>, positionMs: Long): Int {
    if (lines.isEmpty()) return -1
    for (i in lines.indices.reversed()) {
        if (positionMs >= lines[i].timestampMs) {
            return i
        }
    }
    return 0
}

private fun formatTimeMs(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
