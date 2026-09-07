package com.bytekoders.mypod.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytekoders.mypod.battery.BatteryState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import com.bytekoders.mypod.data.lyrics.LyricsResult
import com.bytekoders.mypod.navigation.MenuState
import com.bytekoders.mypod.navigation.RightPaneContent
import com.bytekoders.mypod.source.AlbumInfo
import com.bytekoders.mypod.source.NowPlayingState
import com.bytekoders.mypod.source.TrackMetadata
import com.bytekoders.mypod.ui.extras.CalendarScreen
import com.bytekoders.mypod.ui.extras.CameraScreen
import com.bytekoders.mypod.ui.extras.ClockScreen
import com.bytekoders.mypod.ui.extras.RecorderScreen
import com.bytekoders.mypod.ui.extras.VoiceChatScreen
import com.bytekoders.mypod.ui.games.BrickBreakerScreen
import com.bytekoders.mypod.ui.games.MusicQuizScreen
import com.bytekoders.mypod.ui.games.ParachuteScreen
import com.bytekoders.mypod.ui.games.SnakeScreen
import com.bytekoders.mypod.ui.games.SolitaireScreen
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun IpodScreen(
    menuState: MenuState,
    batteryState: BatteryState,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    nowPlayingState: NowPlayingState = NowPlayingState(),
    lyricsResult: LyricsResult? = null,
    isLoadingLyrics: Boolean = false,
    sensorPitch: Float = 0f,
    sensorRoll: Float = 0f,
    albums: List<AlbumInfo> = emptyList(),
    coverFlowIndex: Int = 0,
    onAlbumSelect: ((AlbumInfo) -> Unit)? = null,
    gameWheelEvents: SharedFlow<WheelEvent>? = null,
    onExitGame: (() -> Unit)? = null,
    tracks: List<TrackMetadata> = emptyList(),
    onPlaySnippet: ((TrackMetadata) -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    geminiApiKey: String = "",
    onSaveGeminiApiKey: ((String) -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Status Bar spanning 100% width across the top of the LCD screen
        IpodStatusBar(
            title = menuState.title,
            batteryState = batteryState,
            isPlaying = isPlaying,
            modifier = Modifier.fillMaxWidth()
        )

        when (menuState.id) {
            "now_playing_menu" -> {
                // Full Screen Now Playing view occupying full height of body area beneath top status bar
                NowPlayingScreen(
                    state = nowPlayingState,
                    lyricsResult = lyricsResult,
                    isLoadingLyrics = isLoadingLyrics,
                    sensorPitch = sensorPitch,
                    sensorRoll = sensorRoll,
                    isFavorite = isFavorite,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            "cover_flow_menu" -> {
                // Interactive 3D Cover Flow album visualizer occupying full height
                CoverFlowScreen(
                    albums = albums,
                    selectedIndex = coverFlowIndex,
                    onAlbumSelect = { album ->
                        onAlbumSelect?.invoke(album)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            "brick_breaker_stub" -> {
                if (gameWheelEvents != null) {
                    BrickBreakerScreen(
                        wheelEvents = gameWheelEvents,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            "snake_stub" -> {
                if (gameWheelEvents != null) {
                    SnakeScreen(
                        wheelEvents = gameWheelEvents,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            "solitaire_stub" -> {
                if (gameWheelEvents != null) {
                    SolitaireScreen(
                        wheelEvents = gameWheelEvents,
                        onExitGame = { onExitGame?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            "parachute_stub" -> {
                if (gameWheelEvents != null) {
                    ParachuteScreen(
                        wheelEvents = gameWheelEvents,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            "quiz_stub" -> {
                if (gameWheelEvents != null) {
                    MusicQuizScreen(
                        wheelEvents = gameWheelEvents,
                        tracks = tracks,
                        onPlaySnippet = onPlaySnippet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            "clock_menu" -> {
                if (gameWheelEvents != null) {
                    ClockScreen(
                        wheelEvents = gameWheelEvents,
                        onExit = { onExitGame?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            "calendar_menu" -> {
                if (gameWheelEvents != null) {
                    CalendarScreen(
                        wheelEvents = gameWheelEvents,
                        onExit = { onExitGame?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            "recorder_menu" -> {
                if (gameWheelEvents != null) {
                    RecorderScreen(
                        wheelEvents = gameWheelEvents,
                        onExit = { onExitGame?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            "camera_menu" -> {
                if (gameWheelEvents != null) {
                    CameraScreen(
                        wheelEvents = gameWheelEvents,
                        onExit = { onExitGame?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            "gemini_chat_menu" -> {
                if (gameWheelEvents != null) {
                    VoiceChatScreen(
                        wheelEvents = gameWheelEvents,
                        apiKey = geminiApiKey,
                        onSaveApiKey = { newKey ->
                            onSaveGeminiApiKey?.invoke(newKey)
                        },
                        onExit = { onExitGame?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            else -> {
                // Two-Pane Content Split Area occupying full height beneath top status bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Left Pane: Menu Items List
                    Box(
                        modifier = Modifier
                            .weight(0.53f)
                            .fillMaxHeight()
                            .background(Color.White)
                    ) {
                        IpodMenuList(menuState = menuState)
                    }

                    // Vertical Separator Line
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFD0D0D5),
                                        Color(0xFFB0B0B5),
                                        Color(0xFFD0D0D5)
                                    )
                                )
                            )
                    )

                    // Right Pane: Dynamic Graphic / Artwork / Preview
                    val activeItem = if (menuState.items.isNotEmpty()) {
                        val idx = menuState.selectedIndex.coerceIn(0, menuState.items.lastIndex)
                        menuState.items[idx]
                    } else null

                    Box(
                        modifier = Modifier
                            .weight(0.47f)
                            .fillMaxHeight()
                    ) {
                        RightPanePreview(
                            content = activeItem?.rightPane ?: RightPaneContent.DefaultArtwork,
                            nowPlayingState = nowPlayingState
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IpodStatusBar(
    title: String,
    batteryState: BatteryState,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        while (true) {
            currentTimeString = formatter.format(Date())
            delay(1000L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8ECEF),
                        Color(0xFFC8CED4)
                    )
                )
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Left: Title constrained to max 35% width so long titles never overlap center time
        Box(
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Center: Time Display
        if (currentTimeString.isNotEmpty()) {
            Text(
                text = currentTimeString,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222),
                maxLines = 1,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Right: Play/Pause status icon directly beside battery indicator
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play arrow when playing, Pause bars when paused
            val glyphIcon = if (isPlaying) Icons.Rounded.PlayArrow else Icons.Rounded.Pause
            val glyphColor = if (isPlaying) Color(0xFF1E5BB5) else Color(0xFF444444)

            Icon(
                imageVector = glyphIcon,
                contentDescription = if (isPlaying) "Playing" else "Paused",
                tint = glyphColor,
                modifier = Modifier.size(15.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (batteryState.isCharging) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = "Charging",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
            }

            // Battery Gauge Box
            Box(
                modifier = Modifier
                    .size(width = 22.dp, height = 11.dp)
                    .border(1.dp, Color(0xFF444444), RoundedCornerShape(2.dp))
                    .padding(1.dp)
            ) {
                val fillFraction = (batteryState.levelPercentage / 100f).coerceIn(0.05f, 1f)
                val barColor = when {
                    batteryState.isCharging -> Color(0xFF4CAF50)
                    batteryState.levelPercentage <= 20 -> Color(0xFFE53935)
                    else -> Color(0xFF2E7D32)
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fillFraction)
                        .clip(RoundedCornerShape(1.dp))
                        .background(barColor)
                )
            }
            // Battery Tip
            Box(
                modifier = Modifier
                    .size(width = 1.5.dp, height = 4.dp)
                    .background(Color(0xFF444444), RoundedCornerShape(topEnd = 1.dp, bottomEnd = 1.dp))
            )
        }

        // Bottom border line on status bar
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = Color(0xFF9E9E9E),
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
private fun IpodMenuList(menuState: MenuState) {
    val listState = rememberLazyListState()

    // Auto-scroll list as selectedIndex changes
    LaunchedEffect(menuState.selectedIndex) {
        if (menuState.items.isNotEmpty()) {
            val targetIdx = menuState.selectedIndex.coerceIn(0, menuState.items.lastIndex)
            listState.animateScrollToItem(targetIdx)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(menuState.items) { index, item ->
            val isSelected = index == menuState.selectedIndex

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
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
                        val textColor = when {
                            !item.isEnabled && isSelected -> Color(0xFFDDDDDD)
                            !item.isEnabled -> Color(0xFF888888)
                            isSelected -> Color.White
                            else -> Color(0xFF111111)
                        }
                        val subColor = when {
                            !item.isEnabled && isSelected -> Color(0xFFCCCCCC)
                            !item.isEnabled -> Color(0xFFAAAAAA)
                            isSelected -> Color.White.copy(alpha = 0.8f)
                            else -> Color(0xFF777777)
                        }

                        Text(
                            text = item.title,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.subtitle != null && !isSelected) {
                            Text(
                                text = item.subtitle,
                                fontSize = 8.5.sp,
                                color = subColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (item.hasSubMenu) {
                        val arrowTint = when {
                            !item.isEnabled -> Color(0xFFAAAAAA)
                            isSelected -> Color.White
                            else -> Color(0xFF888888)
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Navigate",
                            tint = arrowTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Subtle bottom hairline divider for unselected rows
                if (!isSelected) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = Color(0xFFEEEEEE),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1f
                        )
                    }
                }
            }
        }
    }
}
