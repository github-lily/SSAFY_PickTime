package com.example.picktimeapp.ui.game

import android.content.Context
import android.media.MediaPlayer

object PreviewSoundPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playSound(context: Context, url: String) {
        // 기존에 재생 중이던 음악 멈추기
        stopSound()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener {
                it.start()
            }
            setOnCompletionListener {
                stopSound() //재생이 끝나면 정리
            }
            prepareAsync()
        }
    }
    fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun toggleSound(context: Context, url: String) {
        if (isPlaying()) {
            stopSound()
        } else {
            playSound(context, url)
        }
    }
}