package com.example.picktimeapp.audio

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// AudioEventListener 인터페이스 정의
interface AudioEventListener {
    fun onThresholdExceeded(audioData: ShortArray)
}

object AudioComm {

    const val amplitudeThreshold = 300.0
    private var targetChord = ""
    private var onReady = false;

    // 이벤트 리스너를 위한 변수
    var eventListener: AudioEventListener? = null

    private var chordDetectedForThisStroke = false
    private val resetDelayMs = 500L
    private var resetJob: Job? = null

    private val audioCapture = AudioCapture(4096) { audioData ->

        if(!onReady)return@AudioCapture

        val rms = calculateRMS(audioData)
        if (rms < amplitudeThreshold) {
            // RMS 낮아지면 리셋 타이머 시작
            resetJob?.cancel()
            resetJob = CoroutineScope(Dispatchers.Default).launch {
                delay(resetDelayMs)
                chordDetectedForThisStroke = false
            }
            return@AudioCapture
        }

        // root note 검출
//        val newFreq = AudioAnalyzerYIN.analyzeFrequency(audioData)
//        val rootNote = AudioAnalyzerYIN.frequencyToNoteName(newFreq)
//
//        if(targetChord == rootNote){
//
//        }

        // 임계치 이상일 경우 이벤트 호출하여 카메라 캡처 시작 요청 전달
        eventListener?.onThresholdExceeded(audioData)

    }

    // 타겟 코드 업데이트를 위한 public 메서드 추가
    fun updateTargetChord(chord: String) {
        targetChord = chord
    }

    fun audioCaptureOn(){
        onReady = true;
    }

    fun audioCaptureOff(){
        onReady = false;
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