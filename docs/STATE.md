# Current State Snapshot

## Current Milestone
Prev/Next Controls, Split-Screen Right Pane Track Card, Settings Reactivity & yt-dlp Online Streams - COMPLETED

## Implemented vs Stubbed

### Controls & Playback (`:app`)
- **Click Wheel Prev/Next Buttons ([`MainViewModel.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/MainViewModel.kt))**: `onNextClick`, `onPrevClick`, `onNextHold`, and `onPrevHold` connected to `skipToNext()`, `skipToPrevious()`, `fastForward()`, and `rewind()`. Tapping `|<<` or `>>|` skips tracks with tactile click audio feedback.
- **Split-Screen Right Pane Active Track Card ([`RightPanePreview.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/ui/components/RightPanePreview.kt), [`IpodScreen.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/ui/components/IpodScreen.kt))**: The right pane displays active album artwork, `▶ Playing` / `⏸ Paused` status badge, track title, and artist name whenever a song is playing.
- **yt-dlp Direct Online Audio Stream Access ([`YtDlpSource.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/source/youtube/YtDlpSource.kt))**: Direct playable online MP3/AAC stream URIs (`https://...`) configured for online stream playback.

### Settings & Theme Reactivity (`:app`)
- **Theme Presets Radio Buttons ([`MainViewModel.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/MainViewModel.kt))**: `refreshPresetsMenu()` renders theme choices as radio button items `(●)` vs `(○)` and updates `presets_menu` and `settings_menu` in place on selection.

## Next Tasks
1. Complete Milestone 3: Sign In menu + Spotify integration.
2. Complete Milestone 4: YouTube + yt-dlp source.
