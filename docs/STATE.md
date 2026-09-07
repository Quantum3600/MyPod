# Current State Snapshot

## Current Milestone
3D Cover Flow Top Online Songs & Albums Integration - COMPLETED

## Implemented vs Stubbed

### Cover Flow & Playback (`:app`)
- **Cover Flow Top Online Albums ([`YtDlpSource.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/source/youtube/YtDlpSource.kt), [`MainViewModel.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/MainViewModel.kt))**:
  - `YtDlpSource` provides top online albums (`Lofi Beats & Study`, `Synthwave Classics`, `Acoustic Dreams`, `yt-dlp Top Trending Hits`) featuring high-res cover artwork (`artUri`).
  - When yt-dlp Stream Resolver is enabled, `refreshMusicSubmenus()` automatically merges these top online albums into the 3D Cover Flow carousel (`_coverFlowAlbumsState`).
  - Clicking any online album in Cover Flow resolves the stream from `YtDlpSource`, begins audio playback, and opens Now Playing.

### Controls & Settings (`:app`)
- **Click Wheel Prev/Next Buttons**: `|<<` and `>>|` skip tracks with click sound feedback.
- **Split-Screen Right Pane Track Card**: Displays active song title, artist, artwork, and `▶ Playing` / `⏸ Paused` status badge.
- **Reactive Settings & Themes**: In-place radio button selection for theme presets and audio sources.

## Next Tasks
1. Complete Milestone 3: Sign In menu + Spotify integration.
2. Complete Milestone 4: YouTube + yt-dlp source.
