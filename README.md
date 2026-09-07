# MyPod

<p align="center">
  <img src="https://img.shields.io/badge/Android-API%2029%2B-3DDC84?logo=android&logoColor=white" alt="Android API 29+" />
  <img src="https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Compose-Jetpack-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Media3-ExoPlayer-FF6F00?logo=googlechrome&logoColor=white" alt="Media3" />
</p>

A native Android music player inspired by the classic iPod design language: brushed metal chassis, oversized click wheel, custom menu stack, and a retro screen aesthetic built in Jetpack Compose.

MyPod blends nostalgic hardware styling with modern playback architecture: local media playback, lyrics, file browsing, theme presets, mini-games, and a modular source design that can evolve toward Spotify, YouTube, and Apple Music integration.

## Highlights

- iPod Classic-inspired hardware shell and screen layout
- Large click-wheel control surface with ring scrolling and button actions
- Local playback via Media3 / ExoPlayer with background service support
- Folder-based file explorer using SAF and direct audio playback URIs
- Now Playing screen with album art, progress bar, and synced/unsynced lyrics
- Multiple theme presets and a refined skeuomorphic design system
- Extras menu with games and auxiliary app experiences
- Data persistence for themes, settings, playlists, and cached lyrics

## Design philosophy

MyPod is not a generic music app; it is a faithful homage to the iPod Classic experience. The visual system is defined in `DESIGN.md`, which carries the core design language for the chassis, click wheel, screen bezel, textures, and theme tokens.

The app follows a strict skeuomorphic design direction:

- clay-morphed metallic body
- matte rubberized wheel
- recessed center button
- square, framed screen proportions
- consistent light-and-shadow treatment to simulate physical depth

## Feature overview

### Music and playback

- Local library browsing through MediaStore metadata
- Folder explorer for direct audio files
- Album, artist, and playlist discovery
- Playback state reflected through a single `PlaybackSource` abstraction
- Background playback with MediaSession support
- Lyrics via LRCLIB lookup with cached and fallback strategies

### Interface

- Retro-style list navigation and menu stack
- Click-wheel-driven input pipeline using `WheelEvent`
- Menu propagation and back-stack logic kept in a custom state machine
- Full-screen, hardware-inspired UI without visible Material widgets

### Extras

- Voice recorder
- Camera app
- Voice chat experience with speech recognition / text-to-speech
- Mini-games: Brick Breaker, Snake, Solitaire, Parachute, and Music Quiz
- Clock, calendar, and notes-inspired screens

### Future-facing architecture

The project is structured around source abstraction boundaries, in line with `AGENTS.md`:

- `PlaybackSource` isolates UI from audio backends
- `AuthProvider` is prepared for service-specific sign-in flows
- streaming services are isolated from the core app by module boundaries
- Spotify, YouTube, Apple Music, and yt-dlp are treated as pluggable integrations

## Architecture at a glance

MyPod follows a compact architecture around Compose UI and state management:

- Kotlin + Jetpack Compose for the UI
- Media3 / ExoPlayer for transport and playback
- Room for structured persistence
- DataStore for preferences and active settings
- Retrofit / Moshi / OkHttp for API integration
- Hilt-style DI patterns and MVI-inspired state handling

## Project structure

```text
MyPod/
├── AGENTS.md
├── DESIGN.md
├── README.md
├── docs/
│   ├── AGENT_LOG.md
│   ├── SPEC.md
│   └── STATE.md
├── app/
│   ├── src/main/java/com/trishit/mypod/
│   └── src/test/java/
├── build.gradle.kts
├── gradle/
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── local.properties
```

The app is intentionally organized around the iPod-inspired UI, playback subsystems, and data services. See the implementation under `app/src/main/java/com/trishit/mypod` for the main UI, navigation, source management, and media code.

## Build and run

### Prerequisites

- Android Studio (latest stable recommended)
- JDK 11+
- Android SDK with API 29+ target support
- Emulator or physical Android device

### Commands

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

Run the app from Android Studio or launch the debug build on a target device/emulator.

## Documentation

This repository keeps its design and product direction in a few canonical files:

- `AGENTS.md` — contributor rules, architecture constraints, and repo process
- `DESIGN.md` — iPod Classic visual identity and design specification
- `docs/spec.md` — feature and product requirements
- `docs/STATE.md` — current milestone snapshot
- `docs/AGENT_LOG.md` — session-by-session engineering history

## Milestone status

Current repo state reflects a mature prototype with many core features completed:

- [x] Chassis + click wheel + navigation shell
- [x] Local playback + file explorer + Now Playing + lyrics
- [x] Games + extras modules
- [ ] Sign In menu + Spotify integration
- [ ] YouTube + yt-dlp source
- [ ] Apple Music stub integration
- [ ] Full theme and haptics polish

## Caveats and constraints

The project intentionally respects platform and product boundaries documented in `AGENTS.md`:

- Spotify playback is handled through the Spotify app ecosystem rather than raw audio piping
- YouTube metadata is not a direct audio stream source; yt-dlp is the practical route for actual playback
- Apple Music remains a stub until real MusicKit credentials are available
- The UI avoids Material chrome and keeps the custom iPod shell as the only visible interface

## Why this project is interesting

MyPod is a cross between a design exercise and an engineering challenge: it combines retro hardware reproduction with current Android media architecture and modern Compose UI. It is a good fit for developers who enjoy:

- custom UI design systems
- touch-driven input modeling
- media pipelines and background playback
- state machines and navigation logic
- retro product design translated into a modern app

## License

This project is currently a personal/build-focused application without a formal license declaration in the repository root. If you plan to reuse or redistribute it, check the repository settings and add a license file before publishing beyond local development.

## Thanks and next steps

The project is best experienced as a living prototype of a classic music device recreated for Android. If you want to push it further, the most valuable next contributions are:

1. complete the sign-in and source switching system
2. connect a real Spotify or YouTube flow behind the abstract source system
3. finish the yt-dlp/streaming integration path
4. polish theme and haptic fidelity across the full UI

For deeper technical guidance, start with `AGENTS.md` and the design documents in `docs/`.
