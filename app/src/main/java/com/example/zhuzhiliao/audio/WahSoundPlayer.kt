package com.example.zhuzhiliao.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.zhuzhiliao.R

/**
 * Plays the user-provided wah recording bundled as a local app resource.
 * SoundPool keeps the short clip ready for repeated orbit completions without
 * starting a new MediaPlayer for every event.
 */
class WahSoundPlayer(context: Context) {
    private val soundPool: SoundPool
    private val soundId: Int
    @Volatile private var loaded = false
    @Volatile private var released = false

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .setMaxStreams(3)
            .build()
        soundPool.setOnLoadCompleteListener { _, loadedSoundId, status ->
            if (loadedSoundId == soundId && status == 0) loaded = true
        }
        soundId = soundPool.load(context.applicationContext, R.raw.zhuzhiliao_wah, 1)
    }

    fun play() {
        if (!released && loaded) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun dispose() {
        if (released) return
        released = true
        loaded = false
        soundPool.release()
    }
}
