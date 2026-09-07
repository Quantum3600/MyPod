# ==============================================================================
# MyPod R8 / ProGuard Keep Rules
# Prevents optimization/obfuscation crashes for HTTP requests, data models,
# parsers, Room DB, Retrofit, Moshi, Media3/ExoPlayer, and Coroutines.
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. General Keep Attributes
# ------------------------------------------------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions

# ------------------------------------------------------------------------------
# 2. HTTP Requests & Networking (Retrofit, OkHttp, Moshi, JSON)
# ------------------------------------------------------------------------------
# Retrofit
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}
-keep interface com.trishit.mypod.data.lyrics.LrclibApi { *; }

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Moshi & JSON Models
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class * extends com.squareup.moshi.JsonAdapter {
    public <init>(...);
}
-keep class **JsonAdapter {
    public <init>(...);
}

# ------------------------------------------------------------------------------
# 3. Data Models, DTOs, Parsers & State Classes
# ------------------------------------------------------------------------------
# LRCLIB & Lyrics Models & Parsers
-keep class com.trishit.mypod.data.lyrics.LrclibResponse { *; }
-keep class com.trishit.mypod.data.lyrics.LyricEntity { *; }
-keep class com.trishit.mypod.data.lyrics.LrcLine { *; }
-keep class com.trishit.mypod.data.lyrics.LrcParser { *; }

# Playlist & Theme Models
-keep class com.trishit.mypod.data.playlist.PlaylistEntity { *; }
-keep class com.trishit.mypod.data.playlist.PlaylistTrackEntity { *; }
-keep class com.trishit.mypod.data.theme.ThemePreset { *; }

# Source & Playback Models
-keep class com.trishit.mypod.source.TrackMetadata { *; }
-keep class com.trishit.mypod.source.AlbumInfo { *; }
-keep class com.trishit.mypod.source.PlaylistInfo { *; }
-keep class com.trishit.mypod.source.NowPlayingState { *; }
-keep class com.trishit.mypod.source.PlaybackSourceType { *; }

# Navigation & UI State Models
-keep class com.trishit.mypod.navigation.MenuItem { *; }
-keep class com.trishit.mypod.navigation.MenuState { *; }
-keep class com.trishit.mypod.navigation.RightPaneContent { *; }
-keep class com.trishit.mypod.battery.BatteryState { *; }
-keep class com.trishit.mypod.ui.components.WheelEvent { *; }

# Keep all data models and network classes in data and source packages
-keep class com.trishit.mypod.data.** { *; }
-keep class com.trishit.mypod.source.** { *; }

# ------------------------------------------------------------------------------
# 4. Room Local Database
# ------------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-keep class com.trishit.mypod.data.lyrics.LyricsDatabase { *; }
-keep class com.trishit.mypod.data.playlist.PlaylistDatabase { *; }
-keep class com.trishit.mypod.data.lyrics.LyricsDao { *; }
-keep class com.trishit.mypod.data.playlist.PlaylistDao { *; }
-keep class *_*_Impl { *; }

# ------------------------------------------------------------------------------
# 5. Media3 / ExoPlayer & Audio Playback Engine
# ------------------------------------------------------------------------------
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }
-keep class com.trishit.mypod.playback.PlaybackService { *; }
-keep class com.trishit.mypod.playback.AudioEngine { *; }

# ------------------------------------------------------------------------------
# 6. Enums & Companion Objects
# ------------------------------------------------------------------------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ------------------------------------------------------------------------------
# 7. Kotlin Coroutines
# ------------------------------------------------------------------------------
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }
-keep class kotlinx.coroutines.CoroutineExceptionHandler { *; }

# ------------------------------------------------------------------------------
# 8. Coil Image Loading & CameraX
# ------------------------------------------------------------------------------
-keep class io.coilkt.** { *; }
-keep class androidx.camera.** { *; }
