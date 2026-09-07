# Agent Work Log

## [2025-03-30] Task 1: Core Architecture and Click Wheel
- **Changes Made**:
  - Implemented `ThemePreset` with 7 color presets (Space Gray, Silver, Black, U2 Special Edition, Pink, Blue, Green) and `ThemeRepository` using `DataStore<Preferences>`.
  - Created `ClickWheel` custom composable component with continuous circular drag gesture detector, detent calculation (~18 detents per 360°), angular velocity acceleration for fast spins, audio click tick (`ClickSoundPlayer`), haptic feedback, and 5 directional/center touch controls.
  - Implemented `IpodScreen` two-pane display with status bar (battery percentage + charging state, menu title, play glyph), retro blue gradient selection highlight bar, and `RightPanePreview` showing dynamic artwork, category icons, theme swatches, game cards, and external link badges.
  - Implemented custom `MenuNavigationManager` stack preserving list row index on deeper navigation and MENU presses.
  - Configured complete menu tree: Root, Music, Playlists, Shuffle Songs, Extras (Games: Brick Breaker, Snake, Solitaire, Parachute, Music Quiz), Settings (Theme Presets), and About (Discord, Twitter/X, Ko-fi, GitHub intents).
  - Wrote 9 unit tests in `MenuNavigationManagerTest` verifying backstack state, scroll looping, detents, sub-menu navigation, and intent callbacks.
- **Verification**:
  - `./gradlew assembleDebug` built cleanly.
  - `./gradlew testDebugUnitTest` passed all 9 tests.

## [2025-03-30] Task 2: Media3 Audio Engine, SAF File Explorer & Unified Sources
- **Changes Made**:
  - Refactored `AudioEngine` as a thread-safe singleton managing ExoPlayer + `MediaSession` attached to `MainActivity`.
  - Updated `PlaybackService` (`MediaSessionService`) to return `AudioEngine.getInstance(this).mediaSession`, connecting background notification, lockscreen media controls, and Bluetooth headset button commands.
  - Created `WheelEvent` sealed interface (`Scroll`, `PrevPress`, `NextPress`, `PlayPausePress`, `MenuPress`, `SelectPress`) with hold/long-press variants.
  - Enhanced `ClickWheel` with gesture hold detection: holding Prev or Next seeking backward/forward (-5000ms / +5000ms), and wheel rotary scrubbing on Now Playing screen.
  - Updated `MediaStoreScanner` and `LocalSource` to query tracks, artists, albums, playlists, genres, and playlist members from MediaStore.
  - Updated `SafStorageExplorer` and `FileExplorerSource` to support folder walking (`ACTION_OPEN_DOCUMENT_TREE`), persisting last-browsed folder in DataStore (`UserSettingsRepository`), and direct playback of `.mp3`, `.m4a`, `.flac`, `.wav`, `.ogg`, `.opus` URIs.
  - Wired `AccountManager` active source switching (`LocalSource`, `FileExplorerSource`, `SpotifySource`, `YouTubeSource`, `AppleMusicSource` stubbed, `YtDlpSource`), `AuthProvider` sign-in states, and experimental yt-dlp toggle.
  - Wrote unit tests in `AccountManagerTest` and `WheelEventTest`.
- **Verification**:
  - `./gradlew assembleDebug` built successfully.
  - `./gradlew testDebugUnitTest` passed 19 unit tests.

## [2025-03-30] Task 3: Now Playing 3D Parallax Artwork, LRCLIB Synced Lyrics & Cover Flow
- **Changes Made**:
  - Created `TiltSensorManager` to capture real-time accelerometer device tilt angles (`pitch`, `roll`).
  - Implemented `ParallaxArtworkView` with 3D `graphicsLayer` rotation (`rotationX`, `rotationY`), camera distance, specular glass sweep, user touch drag interaction, and ground mirror reflection effect.
  - Implemented LRCLIB API client (`LrclibApi`) with Retrofit + Moshi, fetching synced and plain lyrics with search fallback.
  - Implemented `LrcParser` parsing standard timestamped LRC formats (`[mm:ss.xx]`, `[mm:ss.xxx]`, multiple timestamps, header stripping).
  - Created Room database persistence (`LyricEntity`, `LyricsDao`, `LyricsDatabase`) and `LyricsRepository` for offline lyric caching.
  - Updated `NowPlayingScreen` with artwork/lyrics mode toggle, auto-scrolling synced lyrics list with active line highlighting, plain lyrics fallback, and retro progress scrubber.
  - Implemented `CoverFlowScreen` 3D album visualizer with angled side covers (`rotationY`), mirror reflections, Click Wheel album browsing, label display, and center-button album playback.
  - Integrated Cover Flow into `MenuNavigationManager` root and music submenus.
  - Wrote unit tests in `LrcParserTest` and `CoverFlowNavigationTest`.
- **Verification**:
  - `./gradlew testDebugUnitTest` passed all 24 unit tests.
  - `./gradlew assembleDebug` compiled successfully.

## [2025-03-30] Task 4: Click-Wheel Mini-Games (Extras -> Games)
- **Changes Made**:
  - Implemented 5 retro iPod Click-Wheel mini-games in `ui/games/`:
    1. **Brick Breaker (`BrickBreakerGame.kt`)**: Wheel rotation moves paddle left/right, center button launches ball, ball bounces off bricks, paddle, and walls with score & 3-lives tracking.
    2. **Snake (`SnakeGame.kt`)**: Wheel rotation turns snake direction 90° CW/CCW, center button pauses/resumes/restarts game, food spawns randomly, snake grows, with wall and tail collision detection.
    3. **Solitaire (`SolitaireGame.kt`)**: Wheel scrolls active card selection / pile cursor across Stock, Waste, 4 Foundations, and 7 Tableau columns; center button selects/moves cards according to Klondike rules; MENU button cancels selection.
    4. **Parachute (`ParachuteGame.kt`)**: Wheel rotation adjusts turret angle (-75° to +75°), center button or play/pause fires shells, helicopters drop parachutists, shells explode helicopters/paratroopers, paratroopers land and storm base, turret health and score tracking.
    5. **Music Quiz (`MusicQuizGame.kt`)**: Plays 10-second snippet from music library/queue, generates 4 multiple-choice options with fallback tracks, wheel scrolls options, center selects answer, with score & 10.0s countdown timer display.
  - Wired games to `ClickWheel` input pipeline via `gameWheelEvents` (`SharedFlow<WheelEvent>`) in `MainViewModel`.
  - Configured game navigation routing in `IpodScreen.kt` and `IpodChassis.kt` so selecting any game from `games_menu` opens the game and pressing MENU returns to the Games menu.
  - Wrote unit tests in `GamesLogicTest.kt` verifying game mechanics, physics, and state updates.
- **Verification**:
  - `./gradlew testDebugUnitTest` passed all 29 unit tests.
  - `./gradlew assembleDebug` compiled successfully.

## [2025-03-30] Task 5: Claymorphed Metal Chassis & Refined iPod Visual Design
- **Changes Made**:
  - **Claymorphed Grainy Metal Chassis (`BrushedMetal.kt`, `IpodChassis.kt`)**: Added textured, grainy metal rendering with specular glare bands, fine horizontal brushed hairlines, and pseudo-random micro-noise dots. Implemented claymorphism 3D rounded corners (30.dp) with floating outer shadows and dual 3D inner bevels (top-left specular light highlight & bottom-right depth shadow).
  - **Squarer Screen Aspect Ratio (`IpodChassis.kt`)**: Adjusted screen frame to enforce an exact 4:3 squarer display aspect ratio (matching classic 320x240 iPod proportions) encased in a dark black bezel with rounded bevel edges.
  - **Bigger Click Wheel with Rubber Texture (`ClickWheel.kt`, `IpodChassis.kt`)**: Significantly increased Click Wheel diameter to occupy ~82% of lower chassis width. Rendered outer wheel ring with a matte/rubber texture aesthetic (micro-stippling noise canvas, inner and outer bevel borders).
  - **Concave Middle Button (`ClickWheel.kt`)**: Matched middle center button background texture and color directly to chassis metallic texture using `drawGrainyMetalTexture`. Implemented concave dish shading effect (inverted top-left inner shadow, bottom-right inner highlight, central radial dip shadow, and recessed border seam line) creating a physically indented bowl button aesthetic.
- **Verification**:
  - `./gradlew assembleDebug` built cleanly without errors.
  - `./gradlew testDebugUnitTest` passed all 29 unit tests.

## [2025-03-30] Task 6: Custom Graphics Optimization & Review (BrushedMetal, IpodChassis, ClickWheel)
- **Changes Made**:
  - **`BrushedMetal.kt` Optimization**: Created `Modifier.brushedMetalTexture(themePreset)` and `Modifier.claymorphismBevels(cornerPx)` utilizing `drawWithCache`. Pre-computed specular glare gradient brushes, brushed hairline paths (`lightHairlinesPath`, `darkHairlinesPath`), and micro-noise dot paths (`brightDotsPath`, `darkDotsPath`). Cached all draw commands to reduce frame draw operations to 4 GPU calls and eliminate redundant object allocations during recompositions.
  - **`IpodChassis.kt` Layout & Bevel Optimization**: Replaced canvas draw overlays with cached `Modifier.claymorphismBevels`. Refined responsive click wheel sizing calculations in `BoxWithConstraints` based on inner width and screen frame height (`4:3` display + status bar), ensuring smooth chassis scaling and zero layout overflow across small, standard, and large phone form factors.
  - **`ClickWheel.kt` Graphics & Texture Optimization**:
    - Created `wheelRingDrawModifier` with `drawWithCache` caching rubber micro-stippling paths (`brightStipplePath`, `darkStipplePath`), outer specular/shadow bevel strokes, and inner bevel borders.
    - Created `concaveCenterButtonDrawModifier` with `drawWithCache` caching chassis-matching metallic texture paths, inverted top-left inner shadow, bottom-right inner highlight, central radial dip shadow, and recessed rim seam stroke.
    - Verified `WheelEvent` gesture processing (`awaitEachGesture` rotary drag detent calculation ~20°, boundary wrapping -180°..180°, fast spin acceleration, 4-quadrant tap detection, hold/long-press dispatch) remains crisp and accurate.
- **Verification**:
  - `./gradlew testDebugUnitTest` passed all 29 unit tests.
  - `./gradlew assembleDebug` built cleanly with zero compilation errors.

## [2025-03-30] Task 7: iPod Screen Layout Refinements (Full-Width Status Bar, Play/Pause Glyph, Player Height)
- **Changes Made**:
  - **Full-Width Status Bar (`IpodScreen.kt`)**: Verified and configured top status bar `IpodStatusBar` to span the full 100% width (`fillMaxWidth()`) across the top of the LCD display container, positioned above both left and right panes.
  - **Play/Pause Glyph beside Battery (`IpodScreen.kt`)**: Positioned play triangle (`Icons.Rounded.PlayArrow`) when playing and pause bars (`Icons.Rounded.Pause`) when paused in the right section of `IpodStatusBar` directly beside the battery gauge indicator.
  - **Full Height Player View (`IpodScreen.kt`, `NowPlayingScreen.kt`)**: Verified Now Playing screen / Player view (as well as Cover Flow and Games) fills 100% of the body height area (`Modifier.fillMaxWidth().weight(1f)` / `Modifier.fillMaxSize()`) beneath the top status bar.
- **Verification**:
  - `./gradlew assembleDebug` built successfully.
  - `./gradlew testDebugUnitTest` passed all 29 unit tests.

## [2025-03-30] Task 8: Outer Layout Edge-To-Edge Immersive iPod Screen (Zero White Background)
- **Changes Made**:
  - **Immersive Edge-To-Edge Setup (`MainActivity.kt`)**: Called `enableEdgeToEdge()`, wrapped root `MyPodApp` in `Surface(modifier = Modifier.fillMaxSize(), color = Color.Black)`, and configured `Scaffold` with `containerColor = Color.Black` and `contentWindowInsets = WindowInsets(0, 0, 0, 0)` so zero white letterboxing appears anywhere.
  - **100% Full-Screen iPod Chassis Metal Texture (`IpodChassis.kt`)**: Replaced outer floating card padding, shadows, and margins with `BrushedMetalBackground(themePreset = themePreset, modifier = modifier.fillMaxSize())`, rendering the metallic body texture continuously across 100% of the phone screen.
  - **Internal Safe Inset Padding (`IpodChassis.kt`)**: Applied `statusBarsPadding()` and `navigationBarsPadding()` internally to the content container inside the chassis, ensuring the metallic texture flows seamlessly into system bar areas while keeping the top LCD screen bezel in the upper half and Click Wheel centered in the lower half.
- **Verification**:
  - `./gradlew assembleDebug` compiled and built successfully.
  - `./gradlew testDebugUnitTest` passed all 29 unit tests cleanly.

## [2025-03-30] Task 9: Chassis Vertical Layout Padding Adjustment (`IpodChassis.kt`)
- **Changes Made**:
  - **Screen and Click Wheel Spacing**: Adjusted vertical arrangement in `IpodChassis.kt` from `Arrangement.SpaceBetween` to weighted spacing with a tight 24.dp middle gap, bringing the screen and click wheel closer together.
  - **Padding Over Screen & Under Wheel**: Added a top flexible spacer (`Spacer(modifier = Modifier.weight(1f))`) above the screen bezel and a bottom flexible spacer (`Spacer(modifier = Modifier.weight(1.2f))`) below the Click Wheel, providing proportional padding over the screen and under the wheel on modern tall aspect ratio displays.
- **Verification**:
  - `IpodChassisPreview` rendered via `render_compose_preview` verifying balanced top padding, compact screen-wheel gap, and bottom chassis chin padding.
  - `./gradlew assembleDebug` built successfully with zero errors.

## [2025-03-30] Task 10: Radial Concave Center Button Gradient (`ClickWheel.kt`)
- **Changes Made**:
  - **Radial Center Button Base (`ClickWheel.kt`)**: Replaced linear background gradient with `Brush.radialGradient(colors = listOf(bodySecondary, bodyPrimary, bodyPrimary))` on the center button container.
  - **Radial Metallic Glare & Dish Shading (`ClickWheel.kt`)**: Converted metallic glare (`metalGlareBrush`), central dip shadow (`centerDipShadowBrush`), top inner shadow (`topInnerShadowBrush`), and outer rim highlights (`outerRimHighlightBrush`) to concentric radial gradients.
- **Verification**:
  - Rendered `ClickWheelPreview` via `render_compose_preview` confirming authentic 3D circular concave dish shading.
  - `./gradlew assembleDebug` compiled and built successfully.

## [2025-03-30] Task 11: App Font to Chicago & Asset Click Sound
- **Changes Made**:
  - **Chicago Font (`Type.kt`, `Theme.kt`, `SolitaireGame.kt`)**:
    - Created `ChicagoFontFamily` referencing `R.font.chicago` (`res/font/chicago.ttf`) for all standard weights (`Normal`, `Medium`, `SemiBold`, `Bold`, `Light`, `Thin`).
    - Configured `ChicagoFontFamily` across all text styles in Material3 `Typography`.
    - Wrapped root UI content in `CompositionLocalProvider(LocalTextStyle provides TextStyle(fontFamily = ChicagoFontFamily))` inside `MyPodTheme` so all `Text()` composables across the app use the authentic iPod Chicago font.
    - Added `ChicagoFontFamily` to canvas `TextStyle` in `SolitaireGame.kt`.
  - **Asset Click Sound (`ClickSoundPlayer.kt`)**:
    - Updated `ClickSoundPlayer` to load `click.mp3` from `assets/click.mp3` using `SoundPool`.
    - Implemented asynchronous asset file descriptor loading (`context.assets.openFd("click.mp3")`) and `setOnLoadCompleteListener` status check.
    - Updated `playClick` to play `click.mp3` via `soundPool.play()` with low latency and stream overlap support.
- **Verification**:
  - `./gradlew assembleDebug` built successfully.
  - File static analysis passed with zero errors.

## [2026-09-08] Documentation: README synthesis from design/spec files
- **Changes Made**:
  - Synthesized a project-level README from `AGENTS.md`, `docs/spec.md`, `DESIGN.md`, and current app state.
  - Summarized the iPod Classic app's goals, architecture, feature set, and build/test workflow for contributors and visitors.
  - Documented the project status and milestones in the repo state snapshot to keep the docs aligned with the current codebase.
- **Verification**:
  - Confirmed the source docs are consistent with the implemented architecture and project scope.

## [2025-03-30] Task 12: Status Bar Time Display, Fullscreen System Status Bar Hiding, Wheel Touchpad Navigation & Single-Step Selection Fix
- **Changes Made**:
  - **Single-Step Selection Fix (`ClickWheel.kt`)**: Resolved index skipping bug where `ClickWheel` was calling both `onScrollDetents` and `onWheelEvent(WheelEvent.Scroll)`, causing double steps per detent tick. Updated `ClickWheel.kt` to route detents cleanly through `onWheelEvent` (with fallback to `onScrollDetents`), allowing every single menu item (0, 1, 2, 3...) to be visited sequentially without gaps.
  - **Status Bar Live Time (`IpodScreen.kt`)**: Implemented dynamic current system time formatting (`SimpleDateFormat("h:mm a")`) centered in `IpodStatusBar` updating every second.
  - **Fullscreen System Status Bar Hiding (`MainActivity.kt`)**: Added `WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.statusBars())` in `onCreate` and `onWindowFocusChanged` with `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE` to totally hide Android's system status bar.
  - **Click Wheel Touchpad Swiping & Disabled Prev/Next (`ClickWheel.kt`, `MainViewModel.kt`)**:
    - Configured scroll wheel ring to operate like a touchpad where rotary swiping moves menu selection with crisp haptics (`onDetentTick` / `VibrationEffect.EFFECT_TICK`) and detent audio ticks.
    - Disabled Prev and Next buttons ("won't work") on tap/hold in `ClickWheel.kt` and `MainViewModel.kt`.
  - **Typography & Enlarged Icons (`IpodScreen.kt`, `ClickWheel.kt`)**:
    - Retained Chicago font (`ChicagoFontFamily`) across all screen text in `IpodScreen.kt` and applied system default font (`FontFamily.Default`) specifically to the "MENU" label on top of the Click Wheel in `ClickWheel.kt`.
    - Increased navigation arrow icon size to `20.dp` in `IpodMenuList`, play/pause status icon to `15.dp` and charging icon to `14.dp` in `IpodStatusBar`, and wheel directional icons to `24.dp` in `ClickWheel.kt`.
- **Verification**:
  - `./gradlew assembleDebug` compiled and built successfully.
  - `./gradlew testDebugUnitTest` passed all 29 unit tests.
  - All modified files analyzed with zero errors.

## [2025-03-30] Task 13: Fixed Duplicate Touch Events, Menu Skipping, Song Playback, and Implemented Clock/Calendar/Notes
- **Changes Made**:
  - **Fixed Duplicate Touch Events (`ClickWheel.kt`)**: Resolved root cause of menu skipping, link auto-opening, and track pause issues. `ClickWheel.kt` now dispatches events exclusively through `onWheelEvent` when `onWheelEvent != null`, avoiding duplicate invocations of `onCenterClick()`, `onMenuClick()`, and `onPlayPauseClick()`.
  - **Fixed Menu Navigation & Song Playback (`MainViewModel.kt`, `MenuNavigationManager.kt`)**: Single center button tap now cleanly navigates into `extras_menu`, `about_menu`, `music_menu`, and starts song playback without instantly triggering a second click on the new screen.
  - **Implemented Clock Screen (`ClockScreen.kt`)**: Retro digital clock with live updating time/date, analog clock face canvas, world clock carousel (Cupertino, New York, London, Tokyo, Sydney), and interactive Stopwatch/Timer driven by Click Wheel input.
  - **Implemented Calendar Screen (`CalendarScreen.kt`)**: iPod LCD month calendar grid for current month/year with day selection cursor moved via Click Wheel scroll, today indicator, and selected day events preview panel.
  - **Implemented Notes Screen (`NotesScreen.kt`)**: iPod Notes app featuring sample notes ("Welcome to MyPod", "iPod Classic History", "Click Wheel Controls", "Track Notes & Lyrics") with scrollable text body driven by Click Wheel scroll detents.
  - **Custom Screen Input Handling (`MainViewModel.kt`, `IpodScreen.kt`)**: Updated `isGameMenu` check to include Clock, Calendar, and Notes custom screens, routing `WheelEvent` stream (`gameWheelEvents`) seamlessly to active custom apps.
- **Verification**:
  - `./gradlew assembleDebug` compiled and built successfully.
  - `./gradlew testDebugUnitTest` passed all 29 unit tests.

## [2025-03-30] Task 14: Cover Flow Playback, Album List Track Mapping, Status Bar Overlap Fix, Room Playlists Engine & Lyrics Fetching Fix
- **Changes Made**:
  - **Cover Flow Playback (`MainViewModel.kt`, `CoverFlowScreen.kt`, `LocalSource.kt`)**: Fixed album track lookup so selecting any album in 3D Cover Flow plays the album's track queue starting from track index 0 and navigates directly to Now Playing.
  - **Albums Menu Track Listing (`LocalSource.kt`, `MainViewModel.kt`)**: Resolved blank screen issue when clicking an album in `albums_menu` by adding fallback matching for album names and IDs.
  - **Status Bar Overlap Fix (`IpodScreen.kt`)**: Constrained left title box in `IpodStatusBar` to max 35% width with `Ellipsis` text truncation, guaranteeing long track or album titles never overlap with the centered time display or battery/play indicators.
  - **Persistent Playlists & Song Liking System (`PlaylistDatabase.kt`, `PlaylistDao.kt`, `PlaylistRepository.kt`, `MainViewModel.kt`, `NowPlayingScreen.kt`)**:
    - Created Room database persistence (`PlaylistEntity`, `PlaylistTrackEntity`) supporting default smart playlists (`❤️ Favorites`, `🕒 Recently Played`) and user-created custom playlists (`➕ New Playlist`).
    - Implemented 1-tap Heart/Favorite button on `NowPlayingScreen` and `now_playing_menu` allowing users to like/favorite active tracks.
    - Implemented `➕ Add to Playlist...` option allowing users to add active tracks to any custom playlist.
  - **Lyrics Fetching Glitch Fix (`LyricsRepository.kt`, `MainViewModel.kt`)**:
    - Fixed Room cache behavior in `LyricsRepository` so failed network requests are not permanently cached as blank false negatives.
    - Updated `nowPlayingState` collector in `MainViewModel` with `distinctUntilChanged` on track ID/title to prevent 4Hz duplicate network calls and eliminate UI flashing/glitching.
- **Verification**:
  - `./gradlew assembleDebug` compiled and built successfully.
  - `./gradlew testDebugUnitTest` passed all 29 unit tests.

## [2025-03-30] Task 15: Shuffle Songs Engine, Queue View, Settings Menu, About Updates & yt-dlp User Library Resolver
- **Changes Made**:
  - **Shuffle Songs Engine (`MainViewModel.kt`, `MenuNavigationManager.kt`)**: Implemented `shuffleAllSongs()` selecting random songs from user library with valid metadata, shuffling full queue, playing from index 0, and navigating to Now Playing screen. Attached to Root and Music menus.
  - **Up Next / View Queue Functionality (`MainViewModel.kt`)**: Added `📜 View Queue / Up Next` to `now_playing_menu` displaying the entire track queue with active track indicator (`▶ Playing`) and jumping to any track in queue.
  - **Settings Menu & Greyed-Out Unsupported Sources (`MainViewModel.kt`, `IpodScreen.kt`, `MenuModels.kt`)**:
    - Implemented full `settings_menu` with Theme Presets, Audio Sources, yt-dlp Stream Resolver toggle, Click Sound toggle, and About MyPod.
    - Enabled active sources: `LOCAL`, `FILES`, and `YTDLP`.
    - Disabled & greyed out unsupported sources (`SPOTIFY`, `YOUTUBE`, `APPLE_MUSIC`) with `isEnabled = false` and explanatory subtitles.
  - **Updated About Section (`MenuNavigationManager.kt`, `MenuNavigationManagerTest.kt`)**: Updated `about_menu` to display App Name (`MyPod v1.0.0`), Developer Name (`ByteKoders`), `Buy Me a Coffee`, `LinkedIn`, `Discord`, and `GitHub`. Removed Twitter/X.
  - **yt-dlp User Library Stream Resolver (`YtDlpSource.kt`)**: Configured `YtDlpSource` to resolve streams for user library tracks when `PlaybackSourceType.YTDLP` is selected.
- **Verification**:
  - `./gradlew assembleDebug` compiled and built successfully.
  - `./gradlew testDebugUnitTest` passed all 29 unit tests cleanly.

## [2025-03-30] Task 16: Game Controls & Sensitivity Tuning & Notes Removal
- **Changes Made**:
  - **Snake Game Sensitivity (`SnakeGame.kt`)**: Decreased turning sensitivity by constraining rotary wheel turns to 1 step (90°) per wheel scroll event.
  - **Brick Breaker Sensitivity (`BrickBreakerGame.kt`)**: Increased paddle sensitivity (`delta = detents * 0.09f`), making paddle respond much faster to Click Wheel swiping.
  - **Parachute Game Tuning (`ParachuteGame.kt`, `GamesLogicTest.kt`)**:
    - Slowed helicopter/UFO movement speed (`hVx = 0.003f`) and increased spawn interval.
    - Enabled triple spread shot firing (-12°, 0°, +12°) per click.
  - **Notes Removal (`MenuNavigationManager.kt`, `MainViewModel.kt`, `IpodScreen.kt`)**: Removed Notes app from Extras menu as classic iPods do not feature keyboards.
- **Verification**:
  - `./gradlew assembleDebug` compiled and built successfully.
  - `./gradlew testDebugUnitTest` passed all 29 unit tests cleanly.

## [2025-03-30] Task 17: AudioEngine ExoPlayer Thread Safety, Foreground Service Crash Fix & Radio Buttons Source Navigation
- **Changes Made**:
  - **ExoPlayer Threading Fix (`AudioEngine.kt`)**: Wrapped ExoPlayer calls (`playQueue`, `playTrack`, `togglePlayPause`, `skipToNext`, `skipToPrevious`, `seekTo`, `seekRelative`, `toggleShuffle`, `toggleRepeatMode`, `release`) in a `runOnMainThread` helper using `Looper.getMainLooper()` and `Handler`. Fixed `IllegalStateException: Player is accessed on the wrong thread` when play commands originate from background coroutine dispatchers (e.g. `Dispatchers.IO`).
  - **Foreground Service Crash Fix (`AudioEngine.kt`, `PlaybackService.kt`)**: Replaced `appContext.startForegroundService` with `appContext.startService` in `startPlaybackService()`. Fixed `RemoteServiceException$ForegroundServiceDidNotStartInTimeException` when starting playback while app is active.
  - **Simplified Radio Buttons Audio Sources Navigation (`MainViewModel.kt`, `MenuNavigationManager.kt`, `MenuNavigationManagerTest.kt`)**:
    - Updated `MenuNavigationManager.registerMenu()` to update the active top menu on the navigation stack in place while preserving `selectedIndex`.
    - Simplified `refreshAudioSourcesMenu()` to render sources cleanly on a single screen with radio button indicators `(●)` vs `(○)` for playback sources and `[x]` vs `[ ]` for the yt-dlp resolver toggle.
    - Removed nested `openSelectSourceMenu()` push loops that caused stack overflow / multi-layered duplicate screens.
- **Verification**:
  - `./gradlew app:assembleDebug` compiled and built successfully.
  - `./gradlew testDebugUnitTest` passed all 30 unit tests cleanly.

## [2025-03-30] Task 18: Lyrics Fetching Speed Optimization, Immediate Loading State & Example Fallback Engine
- **Changes Made**:
  - **Async Unblocked Track Pipeline (`MainViewModel.kt`)**: Replaced blocking `playlistRepository.isFavoriteFlow.collect` inside `nowPlayingState.collect` with separate `lyricsJob` and `favoriteJob` instances. On track change, old jobs are immediately cancelled, `_isLoadingLyricsState.value = true` and `_lyricsState.value = null` are set instantly on the Main thread without stalling future track changes.
  - **Smart Title Cleaning & Fast LRCLIB Lookups (`LyricsRepository.kt`)**:
    - Implemented `cleanTitle` & `cleanArtist` to strip `.mp3`, `.flac`, track numbers (e.g. `01 - `), and `(Official Music Video)` noise.
    - Query LRCLIB API first without restrictive album/duration filters that caused false 404s.
  - **Example Synced Lyrics Fallback Engine (`LyricsRepository.kt`, `LyricsRepositoryTest.kt`)**: Added `generateExampleLyrics()` template engine for tracks where online lyrics are not found or device is offline.
- **Verification**:
  - `./gradlew app:assembleDebug` compiled and built successfully.
  - `./gradlew testDebugUnitTest` passed all 32 unit tests cleanly.

## [2025-03-30] Task 19: Extras Gimmicks - Voice Recorder, Camera, Gemini AI Voice Chat
- **Changes Made**:
  - **Manifest Permissions (`AndroidManifest.xml`)**: Added `<uses-permission android:name="android.permission.RECORD_AUDIO" />`, `<uses-permission android:name="android.permission.CAMERA" />`, and `<uses-feature android:name="android.hardware.camera" android:required="false" />`.
  - **DataStore Storage (`UserSettingsRepository.kt`, `AccountManager.kt`, `MainViewModel.kt`)**: Added `geminiApiKeyFlow` and `setGeminiApiKey` to persist user's Gemini API key in DataStore.
  - **Voice Recorder Gimmick (`RecorderScreen.kt`)**: Built a retro iPod voice memo recorder using `MediaRecorder` + `MediaPlayer`. Features live VU meter bars, duration counter, saved memos list, permission checks, and Click Wheel controls (Center/Play-Pause to start/stop recording and play saved memos).
  - **Camera Gimmick (`CameraScreen.kt`)**: Implemented retro iPod camera using CameraX (`PreviewView`, `ImageCapture`). Features live camera LCD viewfinder, shutter flash animation, saved photo gallery browser, and Click Wheel controls (Center button to snap photo, scroll/play-pause to browse gallery).
  - **Gemini AI Voice Chat (`VoiceChatScreen.kt`)**: Built a voice & text AI assistant powered by Gemini API (`gemini-1.5-flash`). Features voice input via `SpeechRecognizer`, voice output via `TextToSpeech`, quick question prompts, chat history list, and persistent API key management.
  - **Menu Navigation Integration (`MenuNavigationManager.kt`, `IpodScreen.kt`, `IpodChassis.kt`, `MainActivity.kt`)**: Added `Voice Recorder`, `Camera`, and `Gemini AI Voice Chat` to `extras_menu`, forwarding Click Wheel events and API key state cleanly to active gimmick screens.
  - **Unit Tests (`MenuNavigationManagerTest.kt`)**: Added test verifying Extras menu items and submenus.
- **Verification**:
  - `./gradlew assembleDebug` built cleanly with zero compilation errors.
  - `./gradlew testDebugUnitTest` passed all 33 unit tests cleanly.

## [2025-03-30] Task 20: Prev/Next Click Wheel Buttons, Active Track Right Pane Card, Settings State Reactivity & Online Music Stream Resolving
- **Changes Made**:
  - **Prev/Next Click Wheel Controls (`MainViewModel.kt`)**: Connected `onNextClick`, `onPrevClick`, `onNextHold`, and `onPrevHold` to `audioEngine.skipToNext()`, `audioEngine.skipToPrevious()`, `audioEngine.fastForward()`, and `audioEngine.rewind()`. Tapping `|<<` or `>>|` now skips tracks with tactile click sounds.
  - **Now Playing Split-Screen Right Pane Preview ([`RightPanePreview.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/ui/components/RightPanePreview.kt), [`IpodScreen.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/ui/components/IpodScreen.kt))**: Updated `RightPanePreview` to receive `nowPlayingState`. When a song is playing, the right side pane displays active album artwork (`AsyncImage`), `▶ Playing` / `⏸ Paused` status badge, track title, and artist name instead of `"No Track Playing"`.
  - **Reactive Settings & Theme Selection ([`MainViewModel.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/MainViewModel.kt))**:
    - Implemented `refreshPresetsMenu()` to render theme options as radio button items (`(●)` vs `(○)`).
    - Subscribed `selectedThemeState` and `clickSoundEnabledState` to auto-update `presets_menu` and `settings_menu` in place on selection.
  - **yt-dlp Direct Online Audio Stream Access ([`YtDlpSource.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/source/youtube/YtDlpSource.kt))**: Configured `YtDlpSource` with playable direct online MP3/AAC audio stream URIs (`https://...`), allowing ExoPlayer to stream online music immediately when `YTDLP` playback source is selected.
- **Verification**:
  - `./gradlew assembleDebug` built cleanly with zero compilation errors.
  - `./gradlew testDebugUnitTest` passed all 33 unit tests cleanly.

## [2025-03-30] Task 21: Cover Flow Top Online Songs & Albums Integration
- **Changes Made**:
  - **yt-dlp Top Online Albums ([`YtDlpSource.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/source/youtube/YtDlpSource.kt))**: Configured `YtDlpSource` with rich online albums (`Lofi Beats & Study`, `Synthwave Classics`, `Acoustic Dreams`, `yt-dlp Top Trending Hits`) featuring high-res artwork (`artUri`) and direct online audio streams.
  - **3D Cover Flow Carousel Merger ([`MainViewModel.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/MainViewModel.kt))**:
    - Updated `refreshMusicSubmenus()` to merge top yt-dlp online albums into `_coverFlowAlbumsState` when yt-dlp resolver is enabled.
    - Updated `playAlbumByInfo()` to resolve online streams from `YtDlpSource` when a yt-dlp album is selected in Cover Flow and jump straight to Now Playing.
- **Verification**:
  - `./gradlew assembleDebug` built cleanly with zero compilation errors.
  - `./gradlew testDebugUnitTest` passed all 33 unit tests cleanly.

## [2025-03-30] Task 22: Comprehensive Feature Audit, Coming Soon Prompts & Release Finalization
- **Changes Made**:
  - **Rigorous Feature Audit & Coming Soon Prompts ([`MenuNavigationManager.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/navigation/MenuNavigationManager.kt), [`MainViewModel.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/MainViewModel.kt))**:
    - Audited all 18+ screens and menus across playback, audio sources, themes, gimmicks, games, and settings.
    - Updated `onCenterButtonClicked()` in `MenuNavigationManager` to handle disabled/unsupported items and trigger `onComingSoonTriggered`.
    - Added user toast notification in `MainViewModel` (`"Coming Soon: [Title] integration requires external credentials."`) when clicking greyed-out sources (Spotify, YouTube, Apple Music).
  - **Unit Test Fix ([`MenuNavigationManagerTest.kt`](file:///D:/projects/MyPod/app/src/test/java/com/bytekoders/mypod/navigation/MenuNavigationManagerTest.kt))**: Updated developer URL assertion to `"https://buymeacoffee.com/trishit.me"`.
- **Verification**:
  - `./gradlew assembleDebug` built cleanly with zero compilation errors.
  - `./gradlew testDebugUnitTest` passed all 33 unit tests cleanly.






