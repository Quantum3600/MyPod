package com.bytekoders.mypod

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bytekoders.mypod.battery.BatteryState
import com.bytekoders.mypod.battery.rememberBatteryState
import com.bytekoders.mypod.data.theme.ThemePreset
import com.bytekoders.mypod.navigation.MenuNavigationManager
import com.bytekoders.mypod.source.NowPlayingState
import com.bytekoders.mypod.ui.chassis.IpodChassis
import com.bytekoders.mypod.ui.theme.MyPodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemStatusBar()
        enableEdgeToEdge()
        requestAudioPermissions()
        setContent {
            MyPodTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    MyPodApp()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemStatusBar()
        }
    }

    private fun hideSystemStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
    }

    private fun requestAudioPermissions() {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(perm), 101)
        }
    }
}

@Composable
fun MyPodApp(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedTheme by viewModel.selectedThemeState.collectAsStateWithLifecycle()
    val currentMenuState by viewModel.currentMenuState.collectAsStateWithLifecycle()
    val nowPlayingState by viewModel.nowPlayingState.collectAsStateWithLifecycle()
    val lyricsResult by viewModel.lyricsState.collectAsStateWithLifecycle()
    val isLoadingLyrics by viewModel.isLoadingLyricsState.collectAsStateWithLifecycle()
    val tiltState by viewModel.tiltSensorState.collectAsStateWithLifecycle()
    val coverFlowAlbums by viewModel.coverFlowAlbumsState.collectAsStateWithLifecycle()
    val coverFlowIndex by viewModel.coverFlowIndexState.collectAsStateWithLifecycle()
    val quizTracks by viewModel.quizTracksState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavoriteState.collectAsStateWithLifecycle()

    val batteryState by rememberBatteryState()
    val localView = LocalView.current

    val safFolderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            viewModel.onSafFolderPicked(it.toString())
        }
    }

    LaunchedEffect(Unit) {
        viewModel.launchSafFolderPickerEvent.collect {
            safFolderPickerLauncher.launch(null)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        IpodChassis(
            themePreset = selectedTheme,
            menuState = currentMenuState,
            batteryState = batteryState,
            nowPlayingState = nowPlayingState,
            lyricsResult = lyricsResult,
            isLoadingLyrics = isLoadingLyrics,
            sensorPitch = tiltState.first,
            sensorRoll = tiltState.second,
            albums = coverFlowAlbums,
            coverFlowIndex = coverFlowIndex,
            onAlbumSelect = { album ->
                viewModel.playAlbumByInfo(album)
            },
            onScrollDetents = { detents ->
                viewModel.onScrollDetents(detents)
            },
            onDetentTick = {
                viewModel.triggerDetentTick(localView)
            },
            onCenterClick = {
                viewModel.onCenterClick(localView)
            },
            onMenuClick = {
                viewModel.onMenuClick(localView)
            },
            onPrevClick = {
                viewModel.onPrevClick(localView)
            },
            onNextClick = {
                viewModel.onNextClick(localView)
            },
            onPlayPauseClick = {
                viewModel.onPlayPauseClick(localView)
            },
            onPrevHold = {
                viewModel.onPrevHold()
            },
            onNextHold = {
                viewModel.onNextHold()
            },
            onWheelEvent = { event ->
                viewModel.onWheelEvent(event)
            },
            gameWheelEvents = viewModel.gameWheelEvents,
            onExitGame = {
                viewModel.onMenuClick(localView)
            },
            tracks = quizTracks,
            onPlaySnippet = { track ->
                viewModel.playQuizSnippet(track)
            },
            isFavorite = isFavorite,
            onToggleFavorite = {
                viewModel.toggleFavoriteCurrentTrack()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
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

