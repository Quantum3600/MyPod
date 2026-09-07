package com.bytekoders.mypod.playback

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return AudioEngine.getInstance(this).mediaSession
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
