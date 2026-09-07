# AGENTS.md — iPod Classic Music Player (Android)

This file directs any coding agent (Gemini, Claude Code, etc.) working in this repo. Read this in full before making changes. Keep it up to date as the project evolves — see **Memory Protocol** below.

---

## 1. Project Summary

Native Android music player, Kotlin + Jetpack Compose, skinned as a pixel-accurate iPod Classic (click wheel, list menus, chassis themes). Plays local files (via MediaStore + a folder-based file explorer), and streams from Spotify, YouTube, and (later) Apple Music, plus an optional yt-dlp fallback source. Now Playing shows 3D album art and LRCLIB-synced lyrics. Extras menu includes click-wheel-playable mini games.

Full feature spec lives in `docs/spec.md` (paste the original build prompt there if not already present). Treat that file as the source of truth for *what* to build; this file governs *how* to work in the repo.

## 2. Tech Stack (do not substitute without updating this file)

- Kotlin, Jetpack Compose (single-activity, no Material widgets visible in the skinned UI)
- Media3 / ExoPlayer + `MediaSessionService` for playback
- MVI pattern for state management (one store per major screen area: `PlayerViewModel`, `MenuNavState`, `AccountManager`)
- Hilt for DI
- Room for local persistence (playlists, cached lyrics, theme/account state)
- DataStore for lightweight prefs (last-browsed folder, active theme, active source)
- Retrofit/Ktor for LRCLIB + YouTube Data API + Spotify Web API calls
- Gradle Kotlin DSL + version catalog (`libs.versions.toml`) — never hand-edit raw version strings inline

## 3. Architecture Rules

- **`PlaybackSource` interface** is the single seam between UI and audio backends (`LocalSource`, `FileExplorerSource`, `SpotifySource`, `YouTubeSource`, `AppleMusicSource` [stub], `YtDlpSource`). New sources implement this interface — never branch on source type inside UI or ViewModel code.
- **`AuthProvider` interface** is the equivalent seam for Sign In / account state. One implementation per service.
- **Click wheel** is a single reusable Composable (`ClickWheel`) that emits a sealed `WheelEvent` (`Scroll(delta)`, `MenuPress`, `SelectPress`, `PrevPress`, `NextPress`, `PlayPausePress`, with press/hold variants). Every screen — menus, Now Playing, games — consumes `WheelEvent`, never raw touch gestures. Do not add screen-specific gesture detectors outside this component.
- **Navigation** uses an internal back-stack driven by MENU presses, independent of Android's system back gesture. Keep this a plain state machine (`MenuNavState`), not a second `NavHost` layered on top of the wheel logic.
- **Themes** (chassis color, wheel color, bezel tint) are a single `Theme` data class swapped via DataStore; never hardcode colors in individual screens — pull from the active `Theme`.
- Keep Spotify/YouTube/Apple Music/yt-dlp integration code in isolated modules (`:source-spotify`, `:source-youtube`, `:source-appmusic`, `:source-ytdlp`) so any one can be disabled or stripped from the build without touching core.

## 4. Known Constraints (do not "fix" these — they're real platform limits)

- Spotify App Remote SDK controls playback inside the installed Spotify app; it does not give raw audio for you to render through ExoPlayer. Don't attempt to pipe Spotify audio into `LocalSource`.
- YouTube Data API v3 gives metadata/search only, not audio URLs. Audio resolution for YouTube goes through the `:source-ytdlp` module, clearly labeled experimental in Settings.
- Apple Music (`AppleMusicSource`, `AppleMusicAuthProvider`) are stubs until MusicKit credentials exist. Don't implement real network calls against a guessed API shape — leave `TODO()` and a greyed-out "Coming Soon" UI state.

## 5. Conventions

- One Composable per file for anything screen-level; small reusable pieces can share a file.
- No magic numbers for wheel detent counts, animation durations, etc. — put them in a `Constants.kt` per module.
- Every new `PlaybackSource`/`AuthProvider` implementation needs a corresponding fake/mock for tests.
- Prefer `sealed interface` over enums for state that carries data.
- Commit messages: `[area] short description` (e.g. `[wheel] fix acceleration curve on fast spins`).

## 6. Build & Test

- `./gradlew assembleDebug` — build.
- `./gradlew testDebugUnitTest` — unit tests (ViewModels, `PlaybackSource` fakes, LRC parser).
- `./gradlew connectedDebugAndroidTest` — instrumented tests (click wheel gesture math, navigation stack).
- Run the LRC-parsing and wheel-acceleration logic through unit tests before wiring to UI — these are the two most bug-prone pieces.

## 7. Memory Protocol (read this every session)

This repo uses a running memory log so agent context survives between sessions.

- **`docs/AGENT_LOG.md`** — append-only. At the **end of every work session**, add a dated entry: what you changed, what's half-finished, any decisions made and why, and any new constraints discovered. Never delete or rewrite past entries — only append.
- **`docs/STATE.md`** — living snapshot, overwritten each session. Keep it short: current milestone (see §8), what's implemented vs stubbed per module, and the next 1–3 concrete tasks. This is the first file to read at the start of a session and the last file to update before ending one.
- Before starting work: read `AGENTS.md` (this file), then `docs/STATE.md`, then skim the tail of `docs/AGENT_LOG.md`.
- Before ending work: update `docs/STATE.md`, append to `docs/AGENT_LOG.md`, and update this file (`AGENTS.md`) itself if you changed an architectural rule, added a module, or discovered a new constraint — this file should never go stale.
- If you discover the spec (`docs/spec.md`) and reality have diverged (a feature was descoped, an approach changed), update the spec too — don't let it silently rot.

## 8. Milestones (update status inline as you go)

1. [x] Chassis + click wheel + static menu navigation
2. [x] Local playback + Files explorer + Now Playing + LRCLIB lyrics + 3D Cover Flow
3. [ ] Sign In menu + Spotify integration
4. [ ] YouTube + yt-dlp source
5. [ ] Apple Music stub wired into Sign In UI
6. [ ] Full theme set + haptics polish
7. [x] Games: Brick Breaker, Snake, Solitaire, Parachute, Music Quiz

## 9. Things Not To Do

- Don't add Material Design visible chrome (buttons, switches, etc.) anywhere in the skinned UI — everything is custom-drawn to match the iPod chassis.
- Don't let any screen bypass the `ClickWheel` → `WheelEvent` pipeline for input.
- Don't hardcode API keys/tokens — read from `local.properties` / secrets manager, gitignored.
- Don't implement Apple Music against guessed endpoints — stub only until real credentials exist.
- Don't overwrite `docs/AGENT_LOG.md` history — append only.
