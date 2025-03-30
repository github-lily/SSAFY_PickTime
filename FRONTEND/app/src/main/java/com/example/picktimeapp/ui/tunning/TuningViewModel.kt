// TuningViewModel.kt
package com.example.picktimeapp.ui.tunning

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.picktimeapp.audio.AudioAnalyzerYIN
import com.example.picktimeapp.audio.AudioCapture
import com.example.picktimeapp.audio.AudioPlayer
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

@HiltViewModel
class TuningViewModel @Inject constructor() : ViewModel() {

    val girinOffsetX = Animatable(-50f)
    private val _targetOffsetY = mutableStateOf(900f)
    val targetOffsetY: State<Float> = _targetOffsetY

    private val _frequencyState = mutableStateOf(0.0)
    val frequencyState: State<Double> = _frequencyState

    private val _noteName = mutableStateOf("Unknown")
    val noteName: State<String> = _noteName

    private val audioPlayer = AudioPlayer()

    // RMS 임계값
    private val amplitudeThreshold = 300.0

    // 디바운싱
    private var lastUpdateTime = 0L
    private val updateIntervalMillis = 500L
    private val frequencyUpdateThreshold = 5.0

    // 버퍼 누적용
    private val ANALYSIS_BUFFER_SIZE = 4096
    private val accumulationBuffer = mutableListOf<Short>()

    private fun calculateRMS(audioData: ShortArray): Double {
        if (audioData.isEmpty()) return 0.0
        var sumSquares = 0.0
        for (sample in audioData) {
            sumSquares += sample * sample
        }
        return sqrt(sumSquares / audioData.size)
    }

    private fun processFrequency(newFreq: Double) {
        val currentTime = System.currentTimeMillis()
        // 디바운싱: 500ms 내에 또 들어오면 무시
//        if (currentTime - lastUpdateTime < updateIntervalMillis) return
//        if (abs(newFreq - _frequencyState.value) < frequencyUpdateThreshold) return

        lastUpdateTime = currentTime
        _frequencyState.value = newFreq
        _noteName.value = AudioAnalyzerYIN.frequencyToNoteName(newFreq)
        Log.d("TuningViewModel", "안정화된 분석 주파수: $newFreq Hz, 음: ${_noteName.value}")
    }

    private val audioCapture = AudioCapture { smallChunk ->
        // 1) 작은 chunk를 누적
        accumulationBuffer.addAll(smallChunk.toList())

        // 2) 4096 샘플 이상 쌓이면
        if (accumulationBuffer.size >= ANALYSIS_BUFFER_SIZE) {
            // 앞에서부터 4096개를 잘라 분석용으로 씀
            val analysisChunk = accumulationBuffer.take(ANALYSIS_BUFFER_SIZE).toShortArray()
            // 사용한 만큼 제거
            accumulationBuffer.subList(0, ANALYSIS_BUFFER_SIZE).clear()

            // 3) RMS 계산 & threshold 체크
            val rms = calculateRMS(analysisChunk)
            Log.d("TuningViewModel", "누적 버퍼 RMS: $rms")
            if (rms < amplitudeThreshold) {
                Log.d("TuningViewModel", "신호 세기 낮음 (RMS: $rms) -> 분석 스킵")
            } else {
                // 4) YIN 분석
                val newFreq = AudioAnalyzerYIN.analyzeFrequency(analysisChunk)
                processFrequency(newFreq)
            }
        }
    }

    fun startAudioProcessing() {
        audioPlayer.start()
        audioCapture.startRecording()
    }

    fun stopAudioProcessing() {
        audioCapture.stopRecording()
        audioPlayer.stop()
    }
}
