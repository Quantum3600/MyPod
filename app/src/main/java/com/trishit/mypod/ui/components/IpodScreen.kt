package com.trishit.mypod.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.mypod.battery.BatteryState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import com.trishit.mypod.data.lyrics.LyricsResult
import com.trishit.mypod.navigation.MenuItem
import com.trishit.mypod.navigation.MenuState
import com.trishit.mypod.navigation.RightPaneContent
import com.trishit.mypod.source.AlbumInfo
import com.trishit.mypod.source.NowPlayingState
import com.trishit.mypod.source.TrackMetadata
import com.trishit.mypod.ui.extras.CalendarScreen
import com.trishit.mypod.ui.extras.CameraScreen
import com.trishit.mypod.ui.extras.ClockScreen
import com.trishit.mypod.ui.extras.RecorderScreen
import com.trishit.mypod.ui.extras.VoiceChatScreen
import com.trishit.mypod.ui.games.BrickBreakerScreen
import com.trishit.mypod.ui.games.MusicQuizScreen
import com.trishit.mypod.ui.games.ParachuteScreen
import com.trishit.mypod.ui.games.SnakeScreen
import com.trishit.mypod.ui.games.SolitaireScreen
import com.trishit.mypod.ui.onboarding.OnboardingScreen
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
    ytdlpAlbums: List<AlbumInfo> = emptyList(),
    ytdlpCoverFlowIndex: Int = 0,
    onAlbumSelect: ((AlbumInfo) -> Unit)? = null,
    gameWheelEvents: SharedFlow<WheelEvent>? = null,
    onExitGame: (() -> Unit)? = null,
    tracks: List<TrackMetadata> = emptyList(),
    onPlaySnippet: ((TrackMetadata) -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    geminiApiKey: String = "",
    onSaveGeminiApiKey: ((String) -> Unit)? = null,
    onCompleteOnboarding: (() -> Unit)? = null,
    onVoiceSearchMain: ((String) -> Unit)? = null,
    onVoiceSearchYtDlp: ((String) -> Unit)? = null
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
            "ytdlp_cover_flow_menu" -> {
                CoverFlowScreen(
                    albums = ytdlpAlbums,
                    selectedIndex = ytdlpCoverFlowIndex,
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
            "onboarding_menu" -> {
                if (gameWheelEvents != null) {
                    OnboardingScreen(
                        wheelEvents = gameWheelEvents,
                        onCompleteOnboarding = { onCompleteOnboarding?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            "main_search_trigger" -> {
                VoiceSearchScreen(
                    searchTargetTitle = "Local Music",
                    onResultFound = { query ->
                        onVoiceSearchMain?.invoke(query)
                    },
                    onCancel = { onExitGame?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            "ytdlp_search_trigger" -> {
                VoiceSearchScreen(
                    searchTargetTitle = "yt-dlp YouTube",
                    onResultFound = { query ->
                        onVoiceSearchYtDlp?.invoke(query)
                    },
                    onCancel = { onExitGame?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            else -> {
                // Two-Pane Content Split Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Left Pane - iPod Classic Menu List (occupies left half)
                    MenuList(
                        menuState = menuState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    // Vertical Split Divider line matching classical dark gray bezel
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(Color(0xFFB0B0B0))
                    )

                    // Right Pane - Dynamic Graphic Context / Album Artwork (occupies right half)
                    RightPanePreview(
                        content = menuState.items.getOrNull(menuState.selectedIndex)?.rightPane
                            ?: RightPaneContent.DefaultArtwork,
                        nowPlayingState = nowPlayingState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
fun IpodStatusBar(
    title: String,
    batteryState: BatteryState,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFD0D7DE),
            Color(0xFFB8C2CC),
            Color(0xFFA2B0BC)
        )
    )

    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            delay(10000L)
        }
    }

    Box(
        modifier = modifier
            .height(24.dp)
            .background(brush = gradientBrush)
            .border(width = 0.5.dp, color = Color(0xFF808E9B))
            .padding(horizontal = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Status Icon (Play / Pause Indicator)
            Box(
                modifier = Modifier.size(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Playing",
                        tint = Color(0xFF1E272C),
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Pause,
                        contentDescription = "Paused",
                        tint = Color(0xFF576574),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Center Title Text with Current Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = Color(0xFF101820),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (currentTimeString.isNotBlank()) {
                    Text(
                        text = " • $currentTimeString",
                        color = Color(0xFF334155),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }

            // Right Status Graphic (iPod Classic Battery Bar)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (batteryState.isCharging) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = "Charging",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(12.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(10.dp)
                        .border(1.dp, Color(0xFF2C3E50), RoundedCornerShape(2.dp))
                        .padding(1.dp)
                ) {
                    val fillRatio = (batteryState.levelPercentage / 100f).coerceIn(0f, 1f)
                    val barColor = when {
                        batteryState.isCharging -> Color(0xFF4CAF50)
                        batteryState.levelPercentage > 20 -> Color(0xFF4CAF50)
                        else -> Color(0xFFE53935)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fillRatio)
                            .background(barColor, RoundedCornerShape(1.dp))
                    )
                }
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(4.dp)
                        .background(Color(0xFF2C3E50))
                )
            }
        }
    }
}

@Composable
fun MenuList(
    menuState: MenuState,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(menuState.selectedIndex) {
        if (menuState.items.isNotEmpty()) {
            val targetIdx = menuState.selectedIndex.coerceIn(0, menuState.items.lastIndex)
            listState.animateScrollToItem(targetIdx)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.background(Color.White)
    ) {
        itemsIndexed(menuState.items) { index, item ->
            val isSelected = index == menuState.selectedIndex
            MenuItemRow(item = item, isSelected = isSelected)
        }
    }
}

@Composable
fun MenuItemRow(
    item: MenuItem,
    isSelected: Boolean
) {
    val backgroundBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF3882C7),
                Color(0xFF1E528B)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color.White, Color.White)
        )
    }

    val textColor = if (isSelected) Color.White else Color(0xFF111111)
    val subtitleColor = if (isSelected) Color(0xFFE0E0E0) else Color(0xFF666666)

    val rowHeight = if (item.subtitle != null) 34.dp else 26.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .background(brush = backgroundBrush)
            .border(
                width = 0.5.dp,
                color = if (isSelected) Color(0xFF163E6B) else Color(0xFFEFEFEF)
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            item.subtitle?.let { sub ->
                Text(
                    text = sub,
                    color = subtitleColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (item.hasSubMenu) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Submenu",
                tint = if (isSelected) Color.White else Color(0xFF888888),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
