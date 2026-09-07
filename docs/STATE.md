# Current State Snapshot

## Current Milestone
Game Difficulty Tuning & Notes Cleanup - COMPLETED

## Implemented vs Stubbed

### Core & Chassis (`:app`)
- **Game Sensitivity & Difficulty Adjustments ([`SnakeGame.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/ui/games/SnakeGame.kt), [`BrickBreakerGame.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/ui/games/BrickBreakerGame.kt), [`ParachuteGame.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/ui/games/ParachuteGame.kt))**:
  - **Snake Game**: Decreased turning sensitivity by constraining rotary wheel turns to 1 step (90°) per wheel scroll event.
  - **Brick Breaker Game**: Increased paddle sensitivity (`delta = detents * 0.09f`), making the paddle respond much faster to Click Wheel swiping.
  - **Parachute Game**: Slowed helicopter/UFO speed (`hVx = 0.003f`) and enabled triple spread shot firing (-12°, 0°, +12°) per click.
- **Removed Notes App ([`MenuNavigationManager.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/navigation/MenuNavigationManager.kt), [`MainViewModel.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/MainViewModel.kt), [`IpodScreen.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/ui/components/IpodScreen.kt))**: Removed Notes item from Extras menu as classic iPods do not feature keyboards.
- **Shuffle Songs Engine (`MainViewModel.kt`)**: Shuffles user library tracks with valid metadata and plays from track 0.
- **Up Next Queue View (`MainViewModel.kt`)**: Interactive queue view allowing users to inspect upcoming songs and jump to any track.
- **Settings Menu & Sources (`MainViewModel.kt`, `IpodScreen.kt`)**:
  - Enabled active sources: `LOCAL`, `FILES`, and `YTDLP`.
  - Disabled/Greyed out unsupported sources (`SPOTIFY`, `YOUTUBE`, `APPLE_MUSIC`).
- **Updated About Section (`MenuNavigationManager.kt`)**: Displays App Name (`MyPod v1.0.0`), Developer (`ByteKoders`), `Buy Me a Coffee`, `LinkedIn`, `Discord`, and `GitHub`.
- **Persistent Playlists & Song Liking (`PlaylistRepository.kt`)**: Room database persistence for user playlists and 1-tap heart favorite toggle.

## Next Tasks
1. Milestone 3: Sign In menu + Spotify integration.
2. Milestone 4: YouTube + yt-dlp source.
