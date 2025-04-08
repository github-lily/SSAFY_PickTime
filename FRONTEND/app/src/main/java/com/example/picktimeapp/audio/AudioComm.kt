package com.example.picktimeapp.audio

import android.util.Log

object AudioComm {

    const val amplitudeThreshold = 700.0
    private var targetChord = ""

    private val audioCapture = AudioCapture(4096) { audioData ->

        val rms = calculateRMS(audioData)
        if (rms < amplitudeThreshold) {
            return@AudioCapture
        }

        // root note 검출
        val newFreq = AudioAnalyzerYIN.analyzeFrequency(audioData)
        val rootNote = AudioAnalyzerYIN.frequencyToNoteName(newFreq)

        if(targetChord.equals(rootNote)){

        }

    }

    // 타겟 코드 업데이트를 위한 public 메서드 추가
    fun updateTargetChord(chord: String) {
        targetChord = chord
    }


    fun startAudioProcessing() {
        // 혹시 이미 캡처 중이면 stop 후 재시작
        stopAudioProcessing()

        audioCapture.startRecording()

    }


    fun stopAudioProcessing() {
        audioCapture.stopRecording()

    }

    // RMS 계산 함수
    private fun calculateRMS(audioData: ShortArray): Double {
        if (audioData.isEmpty()) return 0.0
        var sumSquares = 0.0
        for (sample in audioData) {
            sumSquares += sample * sample
        }
        return kotlin.math.sqrt(sumSquares / audioData.size)
    }
}