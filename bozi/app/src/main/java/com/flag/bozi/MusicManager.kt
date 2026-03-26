package com.flag.bozi

import android.content.Context
import android.media.MediaPlayer

object MusicManager {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    fun start(context: Context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.background_music)
            mediaPlayer?.isLooping = true
        }

        if (!isPlaying) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    fun stop() {
        mediaPlayer?.pause()
        isPlaying = false
    }

    fun release() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
    }

    fun isSoundOn(): Boolean = isPlaying
}
