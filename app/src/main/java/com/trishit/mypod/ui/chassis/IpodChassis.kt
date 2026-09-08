package com.trishit.mypod.ui.chassis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trishit.mypod.battery.BatteryState
import com.trishit.mypod.data.lyrics.LyricsResult
import com.trishit.mypod.data.theme.ThemePreset
import com.trishit.mypod.navigation.MenuNavigationManager
import com.trishit.mypod.navigation.MenuState
import com.trishit.mypod.source.AlbumInfo
import com.trishit.mypod.source.NowPlayingState
import com.trishit.mypod.source.TrackMetadata
import com.trishit.mypod.ui.components.BrushedMetalBackground
import com.trishit.mypod.ui.components.ClickWheel
import com.trishit.mypod.ui.components.IpodScreen
import com.trishit.mypod.ui.components.WheelEvent
import com.trishit.mypod.ui.theme.MyPodTheme
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun IpodChassis(
    themePreset: ThemePreset,
    menuState: MenuState,
    batteryState: BatteryState,
    nowPlayingState: NowPlayingState,
    onScrollDetents: (detents: Int) -> Unit,
    onDetentTick: () -> Unit,
    onCenterClick: () -> Unit,
    onMenuClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
    lyricsResult: LyricsResult? = null,
    isLoadingLyrics: Boolean = false,
    sensorPitch: Float = 0f,
    sensorRoll: Float = 0f,
    albums: List<AlbumInfo> = emptyList(),
    coverFlowIndex: Int = 0,
    ytdlpAlbums: List<AlbumInfo> = emptyList(),
    ytdlpCoverFlowIndex: Int = 0,
    onAlbumSelect: ((AlbumInfo) -> Unit)? = null,
    onPrevHold: (() -> Unit)? = null,
    onNextHold: (() -> Unit)? = null,
    onWheelEvent: ((WheelEvent) -> Unit)? = null,
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
    onVoiceSearchYtDlp: ((String) -> Unit)? = null,
) {
    BrushedMetalBackground(
        themePreset = themePreset,
        modifier = modifier.fillMaxSize()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            val totalWidth = maxWidth
            val totalHeight = maxHeight

            // Responsive Click Wheel Sizing based on available layout space across phone form factors
            val contentWidth = minOf(totalWidth, 480.dp)
            val innerWidth = contentWidth - 32.dp
            val screenHeight = innerWidth * (3f / 4f) + 20.dp
            val availableHeightForWheel = (totalHeight - screenHeight - 96.dp).coerceAtLeast(160.dp)

            val maxWheelByWidth = contentWidth * 0.82f
            val maxWheelByHeight = availableHeightForWheel * 0.90f
            val wheelSize = minOf(maxWheelByWidth, maxWheelByHeight).coerceIn(180.dp, 340.dp)

            Column(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Padding over the screen
                Spacer(modifier = Modifier.weight(.7f))

                // Requirement: Top Screen Frame with Dark Black Bezel and 4:3 Squarer Screen Ratio
                val bezelShape = RoundedCornerShape(18.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, bezelShape)
                        .clip(bezelShape)
                        .background(themePreset.bezelColor)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.Black.copy(alpha = 0.6f),
                                )
                            ),
                            shape = bezelShape,
                        )
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Screen Display Area enforced to exact 4:3 Aspect Ratio
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(6f / 5f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF222225), RoundedCornerShape(8.dp)),
                    ) {
                        IpodScreen(
                            menuState = menuState,
                            batteryState = batteryState,
                            isPlaying = nowPlayingState.isPlaying,
                            nowPlayingState = nowPlayingState,
                            lyricsResult = lyricsResult,
                            isLoadingLyrics = isLoadingLyrics,
                            sensorPitch = sensorPitch,
                            sensorRoll = sensorRoll,
                            albums = albums,
                            coverFlowIndex = coverFlowIndex,
                            ytdlpAlbums = ytdlpAlbums,
                            ytdlpCoverFlowIndex = ytdlpCoverFlowIndex,
                            onAlbumSelect = onAlbumSelect,
                            gameWheelEvents = gameWheelEvents,
                            onExitGame = onExitGame,
                            tracks = tracks,
                            onPlaySnippet = onPlaySnippet,
                            isFavorite = isFavorite,
                            onToggleFavorite = onToggleFavorite,
                            geminiApiKey = geminiApiKey,
                            onSaveGeminiApiKey = onSaveGeminiApiKey,
                            onCompleteOnboarding = onCompleteOnboarding,
                            onVoiceSearchMain = onVoiceSearchMain,
                            onVoiceSearchYtDlp = onVoiceSearchYtDlp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                // Add a flexible spacer above the click wheel
                Spacer(modifier = Modifier.weight(1f))

                // Tight gap between screen and wheel so they aren't far apart
                Spacer(modifier = Modifier.height(24.dp))

                // Requirement: Bottom Section - Circular Click Wheel
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    ClickWheel(
                        themePreset = themePreset,
                        sizeDp = wheelSize,
                        onScrollDetents = onScrollDetents,
                        onDetentTick = onDetentTick,
                        onCenterClick = onCenterClick,
                        onMenuClick = onMenuClick,
                        onPrevClick = onPrevClick,
                        onNextClick = onNextClick,
                        onPlayPauseClick = onPlayPauseClick,
                        onPrevHold = onPrevHold,
                        onNextHold = onNextHold,
                        onWheelEvent = onWheelEvent,
                    )
                }

                // Padding under the click wheel
                Spacer(modifier = Modifier.weight(1.2f))
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_7")
@Composable
fun IpodChassisPreview() {
    val navManager = remember { MenuNavigationManager() }
    val menuState = navManager.currentMenu
    MyPodTheme {
        IpodChassis(
            themePreset = ThemePreset.SPACE_GRAY,
            menuState = menuState,
            batteryState = BatteryState(levelPercentage = 85, isCharging = false),
            nowPlayingState = NowPlayingState(),
            onScrollDetents = {},
            onDetentTick = {},
            onCenterClick = {},
            onMenuClick = {},
            onPrevClick = {},
            onNextClick = {},
            onPlayPauseClick = {}
        )
    }
}
