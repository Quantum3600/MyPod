package com.trishit.mypod.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.trishit.mypod.MainActivity
import com.trishit.mypod.source.NowPlayingState
import com.trishit.mypod.source.PlaybackSourceType
import com.trishit.mypod.source.TrackMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioEngine private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    val player: ExoPlayer = ExoPlayer.Builder(appContext)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val sessionActivityPendingIntent: PendingIntent = PendingIntent.getActivity(
        appContext,
        0,
        Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val mediaSession: MediaSession = MediaSession.Builder(appContext, player)
        .setSessionActivity(sessionActivityPendingIntent)
        .build()

    private val _nowPlayingState = MutableStateFlow(NowPlayingState())
    val nowPlayingState: StateFlow<NowPlayingState> = _nowPlayingState.asStateFlow()

    private var currentQueue: List<TrackMetadata> = emptyList()
    private var currentIndex: Int = 0
    private var positionUpdateJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startPositionUpdates()
                    startPlaybackService()
                } else {
                    stopPositionUpdates()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                updateState { it.copy(isBuffering = isBuffering) }

                if (playbackState == Player.STATE_ENDED) {
                    skipToNext()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val idx = player.currentMediaItemIndex
                if (idx in currentQueue.indices) {
                    currentIndex = idx
                    val track = currentQueue[idx]
                    updateState {
                        it.copy(
                            currentTrack = track,
                            queueIndex = idx,
                            durationMs = track.durationMs
                        )
                    }
                }
            }
        })
    }

    private inline fun runOnMainThread(crossinline action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            Handler(Looper.getMainLooper()).post { action() }
        }
    }

    private fun startPlaybackService() {
        try {
            val serviceIntent = Intent(appContext, PlaybackService::class.java)
            appContext.startService(serviceIntent)
        } catch (_: Exception) {}
    }

    private fun updateState(transform: (NowPlayingState) -> NowPlayingState) {
        _nowPlayingState.value = transform(_nowPlayingState.value)
    }

    fun setSourceType(sourceType: PlaybackSourceType) {
        updateState { it.copy(playbackSourceType = sourceType) }
    }

    fun playTrack(track: TrackMetadata) {
        playQueue(listOf(track), 0)
    }

    fun playQueue(tracks: List<TrackMetadata>, startIndex: Int = 0) = runOnMainThread {
        if (tracks.isEmpty()) return@runOnMainThread
        currentQueue = tracks
        currentIndex = startIndex.coerceIn(tracks.indices)

        val mediaItems = tracks.map { track ->
            val builder = MediaItem.Builder()
            if (track.mediaUri != null) {
                builder.setUri(Uri.parse(track.mediaUri))
            } else {
                builder.setMediaId(track.id)
            }
            builder.setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(track.artUri?.let { Uri.parse(it) })
                    .build()
            )
            builder.build()
        }

        player.setMediaItems(mediaItems, currentIndex, 0L)
        player.prepare()
        player.play()

        val selectedTrack = tracks[currentIndex]
        updateState {
            it.copy(
                currentTrack = selectedTrack,
                queue = tracks,
                queueIndex = currentIndex,
                playbackSourceType = selectedTrack.sourceType,
                durationMs = selectedTrack.durationMs
            )
        }
    }

    fun togglePlayPause() = runOnMainThread {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0, 0L)
            }
            player.play()
        }
    }

    fun skipToNext() = runOnMainThread {
        if (currentQueue.isEmpty()) return@runOnMainThread
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else {
            player.seekTo(0, 0L)
        }
        player.play()
    }

    fun skipToPrevious() = runOnMainThread {
        if (currentQueue.isEmpty()) return@runOnMainThread
        if (player.currentPosition > 3000L) {
            player.seekTo(0L)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else {
            player.seekTo(currentQueue.lastIndex, 0L)
        }
        player.play()
    }

    fun fastForward(offsetMs: Long = 5000L) {
        seekRelative(offsetMs)
    }

    fun rewind(offsetMs: Long = 5000L) {
        seekRelative(-offsetMs)
    }

    fun seekTo(positionMs: Long) = runOnMainThread {
        val target = positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L))
        player.seekTo(target)
        updateState { it.copy(positionMs = target) }
    }

    fun seekRelative(offsetMs: Long) = runOnMainThread {
        val current = player.currentPosition
        val duration = if (player.duration > 0) player.duration else 100_000L
        val target = (current + offsetMs).coerceIn(0L, duration)
        player.seekTo(target)
        updateState { it.copy(positionMs = target) }
    }

    fun toggleShuffle() = runOnMainThread {
        val newShuffle = !nowPlayingState.value.shuffleEnabled
        player.shuffleModeEnabled = newShuffle
        updateState { it.copy(shuffleEnabled = newShuffle) }
    }

    fun toggleRepeatMode() = runOnMainThread {
        val newMode = (nowPlayingState.value.repeatMode + 1) % 3
        player.repeatMode = when (newMode) {
            1 -> Player.REPEAT_MODE_ONE
            2 -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        updateState { it.copy(repeatMode = newMode) }
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive) {
                val pos = player.currentPosition.coerceAtLeast(0L)
                val dur = player.duration.coerceAtLeast(0L)
                updateState {
                    it.copy(
                        positionMs = pos,
                        durationMs = if (dur > 0) dur else it.durationMs
                    )
                }
                delay(250)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    fun release() = runOnMainThread {
        stopPositionUpdates()
        mediaSession.release()
        player.release()
    }

    companion object {
        @Volatile
        private var instance: AudioEngine? = null

        fun getInstance(context: Context): AudioEngine {
            return instance ?: synchronized(this) {
                instance ?: AudioEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}
