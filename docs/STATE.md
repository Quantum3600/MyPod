# Current State Snapshot

## Current Milestone
Extras Gimmicks (Voice Recorder, Camera, Gemini AI Voice Chat) - COMPLETED

## Implemented vs Stubbed

### Core & Lyrics (`:app`)
- **Unblocked Coroutine Track Collector ([`MainViewModel.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/MainViewModel.kt))**: Replaced blocking `playlistRepository.isFavoriteFlow.collect` call inside `nowPlayingState.collect` with independent `lyricsJob` and `favoriteJob` instances. Instantly sets `_isLoadingLyricsState.value = true` and `_lyricsState.value = null` on track changes without stalling future song switches.
- **Smart Title Cleaning & Fast LRCLIB Lookups ([`LyricsRepository.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/data/lyrics/LyricsRepository.kt))**: Automatically strips extensions, track numbers, and video noise before querying LRCLIB.
- **Example Synced Lyrics Fallback Engine ([`LyricsRepository.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/data/lyrics/LyricsRepository.kt))**: Generates time-synced example lyrics scaled to song duration when offline or when online lyrics are unavailable.

### Extras Gimmicks (`:app`)
- **Simple Voice Recorder ([`RecorderScreen.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/ui/extras/RecorderScreen.kt))**: iPod-styled audio memo recorder using Android `MediaRecorder` + `MediaPlayer`. Features live VU meter bars, duration counter, memo list, permission handling, and Click Wheel controls (Center/Play-Pause to start/stop recording and play saved memos).
- **Simple Camera ([`CameraScreen.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/ui/extras/CameraScreen.kt))**: Retro iPod LCD viewfinder using CameraX (`PreviewView`, `ImageCapture`). Features photo shutter flash, photo saved toast, photo gallery viewer, and Click Wheel controls (Center to snap photo, scroll/play-pause to browse gallery).
- **Gemini AI Voice Chat ([`VoiceChatScreen.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/ui/extras/VoiceChatScreen.kt))**: Conversational AI assistant powered by Gemini API (`gemini-1.5-flash`). Features speech-to-text recognition (`SpeechRecognizer`), Text-to-Speech voice responses (`TextToSpeech`), persistent API key management via DataStore (`UserSettingsRepository`), and Click Wheel interaction.

### Core & Playback (`:app`)
- **ExoPlayer Threading Safety ([`AudioEngine.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/playback/AudioEngine.kt))**: Enforced main thread execution via `runOnMainThread` helper for all `AudioEngine` player actions.
- **Foreground Service Crash Fix ([`AudioEngine.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/playback/AudioEngine.kt))**: Updated `startPlaybackService()` to use `appContext.startService(serviceIntent)`.
- **Radio Buttons Audio Sources Navigation ([`MainViewModel.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/MainViewModel.kt), [`MenuNavigationManager.kt`](file:///D:/projects/MyPod/app/src/main/java/com/bytekoders/mypod/navigation/MenuNavigationManager.kt))**: Single radio-button list menu for sources.

## Next Tasks
1. Milestone 3: Sign In menu + Spotify integration.
2. Milestone 4: YouTube + yt-dlp source.
