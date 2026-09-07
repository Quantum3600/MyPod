# Build Prompt: "iPod Classic" Android Music Player

Copy everything below into Gemini.

---

## Project Brief

Build a native Android music player app in **Kotlin + Jetpack Compose** that is a pixel-accurate visual and interaction replica of the **iPod Classic (6th/7th gen)** UI — hardware shell, click wheel, and menu system — while playing modern local and streaming audio sources under the hood.

Reference behavior: the Retro Music app's "iPod Classic" skin (list-based menu navigation with a blue highlight bar, top status bar with title/play-icon/battery, and a physical click-wheel control cluster below the screen) is a good visual baseline — match that fidelity or exceed it.

---

## 1. Hardware Shell & Screen

- Render the full iPod Classic chassis: brushed-metal/aluminum back (Space Gray and Silver presets, user-selectable), black screen bezel, and the circular click wheel below it.
- Screen area (top of device):
    - Status bar: current menu title (left), small play/pause glyph, and a battery indicator (reflect real device battery level + charging state).
    - Body: a two-pane iPod list — left pane list rows with a blue gradient highlight on the selected row, right pane (when applicable, e.g. Now Playing) shows album art/visualizer.
    - Support both the classic **single-column list menu** (Music, Playlists, Shuffle Songs, Extras, Settings, About) and a **Cover Flow–style / Now Playing full-screen view**.
- Screen should render at a fixed aspect ratio matching the real device and scale to fit any phone screen, letterboxed like the reference screenshots (black bars top, device chrome below).

## 2. Click Wheel Interaction

Build a custom Compose `Canvas`/gesture-detector component simulating the real click wheel:

- **Circular scroll (the wheel ring):** Track continuous circular drag gestures. Convert angular delta into discrete "detents" (like the real wheel's tactile clicks — roughly 15–24 per revolution). Each detent scrolls the active list by one row. Angular velocity should drive **acceleration** (fast spins fling/scroll multiple rows, like real iPod scroll physics), plus a light haptic tick per detent (`HapticFeedbackConstants` or `Vibrator` short pulse).
- **MENU (top of wheel):** Acts as **Back** — pops the navigation stack one level. Long-press could jump to Now Playing (optional, real device behavior).
- **Center button:** **Select** — activates the highlighted row / confirms.
- **Prev/Next (left/right of wheel):** Skip track (tap) / seek backward-forward when held (press-and-hold seeking, matching real iPod behavior).
- **Play/Pause (bottom of wheel):** Toggles playback globally, from any screen.
- Use a `NavHost`-driven back stack so MENU always mirrors real back-navigation semantics, and highlight state (selected index per list) must be preserved when returning to a screen via MENU.

## 3. Menu / Navigation Structure

Replicate the classic hierarchy:

```
Retro (root)
├── Now Playing
├── Music
│   ├── Sign In / Songs / Artists / Albums / Playlists / Genres
├── Playlists
├── Shuffle Songs
├── Extras
│   └── Games — a list sub-menu of click-wheel-playable mini games, e.g.:
│       ├── Brick Breaker (wheel rotates the paddle)
│       ├── Snake (wheel rotation steers, center button for a soft turn-confirm if needed)
│       ├── Solitaire (wheel scrolls between cards/piles, center selects, MENU backs out of a selection)
│       ├── Parachute (classic iPod game — click wheel spins the gun, center/play button fires)
│       └── Music Quiz (plays a snippet from the local library, wheel scrolls through title guesses)
├── Settings
│   └── Presets — expand beyond Space Gray / Silver into a small theme sub-menu:
│       ├── Space Gray
│       ├── Silver
│       ├── Black (all-black chassis, classic "iPod Classic Black")
│       ├── U2 Special Edition (black chassis, red click wheel accent)
│       ├── Pink / Blue / Green (colored-back "iPod mini"-style variants, optional stretch)
│       └── each theme swaps chassis texture, click-wheel color, and screen bezel tint; persist selection the same way as Space Gray/Silver (Room/DataStore)
└── About
    └── Discord / Twitter / Ko-fi / Github links (external, open via intent)
```

Each list screen uses the same row component (title text, blue selection bar, chevron where it navigates deeper).

## 4. Now Playing Screen

- **3D album art**: render the album art as a textured plane (Jetpack Compose `graphicsLayer` with `rotationX`/`rotationY`/`cameraDistance`, or a lightweight OpenGL/Filament/Sceneform-free custom 3D transform) that responds to:
    - Device tilt (accelerometer/gyroscope) for a subtle parallax/3D tilt effect, and/or
    - Manual drag on the art itself.
    - Include a soft reflection/gloss layer under the art (classic iPod "glass" look), optional cover-flow style side-swipe between tracks.
- Track metadata: title, artist, album, elapsed/duration, scrubber bar styled like the iPod's thin progress bar.
- **Synced lyrics**: fetch from the [LRCLIB API](https://lrclib.net/docs) (`GET /api/get?artist_name=&track_name=&album_name=&duration=`) using track metadata; parse the returned `syncedLyrics` (LRC format `[mm:ss.xx]`) and highlight the current line in time with playback position, auto-scrolling like a karaoke view. Fall back to `plainLyrics` if no synced lyrics exist, and to "no lyrics found" gracefully if LRCLIB returns nothing.

## 5. Audio Sources

### 5.1 Local playback
- Use **Media3 (ExoPlayer)** with a `MediaSessionService` for background playback, lock-screen/notification controls, and Bluetooth/media-button support.
- Browse local audio via `MediaStore.Audio` (with `READ_MEDIA_AUDIO` runtime permission on API 33+, `READ_EXTERNAL_STORAGE` below), building Songs/Albums/Artists/Playlists views from `MediaStore` metadata + embedded ID3/FLAC art.

### 5.1a Local File Explorer
Add a dedicated **Files** entry under Music (or its own top-level menu item) that browses the device's actual folder structure instead of just the flattened `MediaStore` library:

- Use the **Storage Access Framework** (`ACTION_OPEN_DOCUMENT_TREE`) to let the user pick a root folder (e.g. `Music/`, `Download/`, an SD card path), then walk it with `DocumentFile`/`DocumentsContract` so it also works cleanly on scoped-storage devices (API 30+) without broad storage permissions.
- Render folders and audio files as iPod-style list rows (folder rows show a chevron and navigate deeper via the center button; MENU goes back up a directory, exactly like the app's own menu navigation — reuse the same list component and click-wheel scrolling).
- Support common containers directly (`.mp3`, `.m4a`, `.flac`, `.wav`, `.ogg`, `.opus`) by feeding the selected file's `Uri` straight into ExoPlayer as a `MediaItem`, without requiring it to be indexed in `MediaStore` first.
- Cache the last-browsed path and any user-picked root(s) in Room/DataStore so re-opening Files returns to where the user left off.
- Optional: a simple in-list search/filter field at the top of the Files screen, navigable the same way as everything else (wheel scroll to move through matches, center to select).

### 5.2 Sign In menu — multi-service account switching
Replace the single "Sign In" row with an account-management menu that mirrors the iPod's list style:

- **Sign In** row expands into a sub-list of supported streaming accounts: **Spotify**, **YouTube (Music)**, and **Apple Music** (stub this one out behind a feature flag/interface now, implement later — see note below).
- Each service row shows connection state inline (e.g. "Spotify — Connected as <username>" vs "Spotify — Not Connected"), selectable via center button to trigger that service's OAuth/login flow.
- A separate **Active Source** row (or a toggle at the top of the Sign In list) lets the user pick which *signed-in* service currently drives playback/browsing — only one streaming source is "active" at a time, consistent with the single `PlaybackSource` driving Now Playing. Switching active source should not sign the others out.
- Persist connection tokens per service (Spotify: SDK-managed auth; YouTube: OAuth via Google Sign-In + YouTube Data API scopes; Apple Music: MusicKit token, once implemented) in encrypted storage (`EncryptedSharedPreferences`/Room + Tink).
- Structure this as an `AccountManager`-style class holding one `AuthState` per service, so adding Apple Music later is just implementing one more `AuthProvider`, not restructuring the menu.

### 5.3 Spotify streaming
- Integrate the **Spotify App Remote SDK** for playback control (play/pause/seek/skip) when the user is signed into Spotify.
- ⚠️ Note for the build: the App Remote SDK controls playback **inside the installed Spotify app** (requires Spotify Premium and the Spotify app installed) — it does not hand your app a raw audio stream to render yourself. Design the Now Playing screen to reflect this: when Spotify is the active source, treat Spotify as the audio engine and mirror its playback state/art/metadata into your iPod UI via the SDK's subscriptions, rather than trying to pipe raw audio through ExoPlayer.

### 5.4 YouTube (Music) streaming
- Two viable paths — implement one behind the `PlaybackSource` interface, keep the other as a documented alternative:
    - **YouTube Data API v3** (official, OAuth-based) for search/browse/playlist access once signed in; note that the official API does **not** provide direct audio-stream URLs for playback, only metadata — actual audio still has to come from the yt-dlp path below or from opening the official YouTube app.
    - The existing **yt-dlp streaming source** (see 5.6) resolves an actual playable audio URL; pair it with the Data API (or yt-dlp's own search) for browsing so signed-in YouTube behaves like a real source in the Sign In menu, not just raw URL playback.

### 5.5 Apple Music streaming (future)
- Not implemented in this pass — add an `AppleMusicSource : PlaybackSource` stub and an `AuthProvider` stub in the Sign In menu now (greyed-out "Coming Soon" row) so the account-switching UI and playback abstraction don't need rework later.
- When implemented, it would use **MusicKit for Android/Web** with an Apple Developer token + user Music-User-Token, subject to an active Apple Music subscription.

### 5.6 yt-dlp streaming source
- Fully support resolving a playable stream URL via a `yt-dlp` backend (e.g., a bundled Python/`youtubedl-android` wrapper, or a self-hosted lightweight backend you control that runs `yt-dlp` and returns a direct media URL) and feed that URL into ExoPlayer as a normal `MediaItem`.

### 5.7 Unified playback abstraction
- Define a `PlaybackSource` interface (`LocalSource`, `FileExplorerSource`, `SpotifySource`, `YouTubeSource`, `AppleMusicSource` (stub), `YtDlpSource`) so the Now Playing UI, click-wheel controls, and lyrics fetch logic are source-agnostic and only care about a common `NowPlayingState` (title, artist, album, art, position, duration, isPlaying).

## 6. Architecture

- **UI:** Jetpack Compose, single-activity, custom-drawn iPod chassis (no Material widgets visible — everything skinned).
- **Pattern:** MVI (given prior experience with this pattern) — one `PlayerViewModel`/`MviStore` driving `NowPlayingState`, `MenuNavState`, and `PlaybackSource` selection.
- **Navigation:** Custom back-stack (not standard Compose `NavHost` visuals, but can reuse `NavHost` internally) so MENU button = system back-equivalent within the app's own stack, independent of the OS back gesture.
- **Networking:** Retrofit/Ktor client for LRCLIB.
- **DI:** Hilt or Koin.
- **Persistence:** Room for playlists, cached lyrics, and Space Gray/Silver theme preference.
- **Background audio:** Media3 `MediaSessionService` + `MediaController` so playback survives navigation and screen-off.

## 7. Deliverables

1. A working Android Studio project (Gradle Kotlin DSL, version catalog).
2. The custom click-wheel component as an isolated, reusable Composable.
3. Local playback fully functional end-to-end (browse → select → play → Now Playing with lyrics), including the folder-based Files explorer.
4. Sign In menu with working Spotify and YouTube auth flows, Apple Music stubbed, and an Active Source switcher, all behind the `PlaybackSource`/`AuthProvider` abstractions.
5. yt-dlp source implemented and toggleable in Settings.
6. Full theme sub-menu (Space Gray, Silver, Black, U2 Special Edition, plus color variants) with persisted selection.
7. At least Brick Breaker and Snake playable end-to-end via the click wheel under Extras → Games; Solitaire, Parachute, and Music Quiz as stretch goals in the same menu.

Build this incrementally: (1) chassis + click wheel + static menu navigation first, (2) local playback + Files explorer + Now Playing + LRCLIB lyrics, (3) Sign In menu + Spotify integration, (4) YouTube + yt-dlp source, (5) Apple Music stub, (6) polish (haptics, full theme set, games).