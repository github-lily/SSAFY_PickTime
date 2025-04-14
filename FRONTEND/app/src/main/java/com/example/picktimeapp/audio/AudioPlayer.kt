package com.example.picktimeapp.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log

/**
 * AudioTrack을 사용하여 오디오 데이터를 스트리밍 방식으로 플레이백하는 클래스.
 */
class AudioPlayer {
    private var audioTrack: AudioTrack? = null
    private val sampleRate = 44100
    // AudioTrack을 위한 최소 버퍼 사이즈 계산
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    /**
     * AudioTrack 초기화 및 플레이백 시작.
     */
    fun start() {
        if (audioTrack == null) {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        }
        audioTrack?.play()
    }

    /**
     * AudioTrack에 오디오 데이터를 write하고 결과를 로그로 출력.
     */
    fun playAudioData(audioData: ShortArray) {
        val result = audioTrack?.write(audioData, 0, audioData.size)
        Log.d("AudioPlayer", "write result: $result")
    }

    /**
     * AudioTrack 중지 및 자원 해제.
     */
    fun stop() {
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}
