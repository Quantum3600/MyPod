package com.trishit.mypod.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View

class ClickSoundPlayer(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0
    private var isLoaded: Boolean = false

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        loadClickSound()
    }

    private fun loadClickSound() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build()

            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0 && sampleId == clickSoundId) {
                    isLoaded = true
                }
            }

            val afd = context.assets.openFd("click.mp3")
            clickSoundId = pool.load(afd, 1)
            afd.close()
            soundPool = pool
        } catch (e: Exception) {
            Log.e("ClickSoundPlayer", "Failed to load click sound from assets", e)
        }
    }

    fun playClick(view: View?, soundEnabled: Boolean = true) {
        if (soundEnabled && isLoaded && clickSoundId != 0) {
            try {
                soundPool?.play(clickSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            } catch (e: Exception) {
                Log.e("ClickSoundPlayer", "Error playing click sound", e)
            }
        }

        // Trigger haptic feedback
        performHapticFeedback(view)
    }

    private fun performHapticFeedback(view: View?) {
        try {
            if (view != null) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } else if (vibrator != null && vibrator?.hasVibrator() == true) {
                @Suppress("MissingPermission")
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            clickSoundId = 0
            isLoaded = false
        } catch (_: Exception) {}
    }
}
