package com.example.picktimeapp.ui.tunning

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.picktimeapp.audio.AudioAnalyzerYIN
import com.example.picktimeapp.audio.AudioCapture
import com.example.picktimeapp.audio.AudioPlayer
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class TuningViewModel @Inject constructor() : ViewModel() {

    // 6줄에 해당하는 목표 주파수 (Standard Tuning)
    // 인덱스: 0=D3, 1=A2, 2=E2, 3=G3, 4=B3, 5=E4
    private val standardFrequencies = listOf(
        146.83, // D3
        110.00, // A2
        82.41,  // E2
        196.00, // G3
        246.94, // B3
        329.63  // E4
    )
    private val standardNoteNames = listOf(
        "D3", "A2", "E2", "G3", "B3", "E4"
    )

    // 현재 사용자가 선택한 문자열(페그) 인덱스 (기본값 -1: 미선택)
    private var selectedStringIndex = -1

    // 현재 목표 주파수 (선택된 줄의 표준 주파수)
    private var targetFrequency = 0.0

    // UI 표시용: 현재 감지된 주파수와 노트
    private val _frequencyState = mutableStateOf(0.0)
    val frequencyState: State<Double> = _frequencyState

    private val _noteName = mutableStateOf("버튼을 클릭하여 튜닝을 시작하세요!")
    val noteName: State<String> = _noteName

    // ★ 추가: 튜닝 피드백 (ex: "E2 - 음이 높습니다")
    private var targetNoteName = ""

    private val _tuningFeedback = mutableStateOf("")
    val tuningFeedback: State<String> = _tuningFeedback

    private var chordDetectedForThisStroke = false
    private val resetDelayMs = 500L
    private var resetJob: Job? = null

//    private val audioCapture = AudioCapture { audioData ->
//        val rms = calculateRMS(audioData)
//        if (rms < amplitudeThreshold) {
//            // RMS 낮아지면 리셋 타이머 시작
//            resetJob?.cancel()
//            resetJob = CoroutineScope(Dispatchers.Default).launch {
//                delay(resetDelayMs)
//                chordDetectedForThisStroke = false
//            }
//            return@AudioCapture
//        }
//
//        // 스트로크 중(높은 RMS)이고 아직 분석 안 했으면
//        if (!chordDetectedForThisStroke) {
//            chordDetectedForThisStroke = true
//            resetJob?.cancel()
//
//            val frames = AudioAnalyzerFFTv4.processMultipleFramesAfterAttack(audioData)
//            val detectedChord = AudioAnalyzerFFTv4.aggregateChordResults(frames)
//            showChord(detectedChord ?: "No chord detected.")
//        }
//    }

    private val audioCapture = AudioCapture(4096) { audioData ->

        val rms = calculateRMS(audioData)
        if (rms < amplitudeThreshold) {
            Log.e("TUNNING", "소리가 작아요ㅡ")
            return@AudioCapture
        }
        // 튜닝 처리
        val newFreq = AudioAnalyzerYIN.analyzeFrequency(audioData)
        processFrequency(newFreq)

        // 코드 처리
//        val frames = AudioAnalyzerFFTv3.processMultipleFramesAfterAttack(audioData)
//        val detectedChord = AudioAnalyzerFFTv3.aggregateChordResults(frames)
//        var result = "No chord detected."
//        if (detectedChord != null) {
//            result = detectedChord
//        }
//        showChord(result)

   }

    private val audioPlayer = AudioPlayer()

    // 임계값/디바운싱 등은 그대로 두거나, 수정 가능
    private val amplitudeThreshold = 300.0
    private var lastUpdateTime = 0L
    private val updateIntervalMillis = 300L // 예: 300ms 단위로 업데이트
    private val frequencyUpdateThreshold = 2.0 // 예: 2Hz 이내면 무시

    // RMS 계산 함수
    private fun calculateRMS(audioData: ShortArray): Double {
        if (audioData.isEmpty()) return 0.0
        var sumSquares = 0.0
        for (sample in audioData) {
            sumSquares += sample * sample
        }
        return kotlin.math.sqrt(sumSquares / audioData.size)
    }

    /**
     * YIN으로 새 주파수를 구했을 때 호출됨.
     * - targetFrequency와 비교해 "음이 낮습니다", "음이 높습니다" 등을 로그에 남김.
     */
    private fun processFrequency(newFreq: Double) {
        val currentTime = System.currentTimeMillis()
        lastUpdateTime = currentTime

        // 새로운 주파수 값은 항상 업데이트
        _frequencyState.value = newFreq

        // 유효한 주파수(예: 0보다 큰 값)일 때만 noteName 업데이트
        if (newFreq > 0.0) {
            val name = AudioAnalyzerYIN.frequencyToNoteName(newFreq)
            _noteName.value = name

            // 목표 주파수와 비교 후 튜닝 피드백 업데이트
            if (targetFrequency > 0 && targetNoteName.isNotEmpty()) {
                val difference = newFreq - targetFrequency
                val tolerance = 1.0
                _tuningFeedback.value = when {
                    difference > tolerance -> "음이 높습니다."
                    difference < -tolerance -> "음이 낮습니다."
                    else -> "음이 맞습니다."
                }
//                _tuningFeedback.value = when {
//                    difference > tolerance -> "$targetNoteName - 음이 높습니다."
//                    difference < -tolerance -> "$targetNoteName - 음이 낮습니다."
//                    else -> "$targetNoteName - 음이 맞습니다."
//                }
            }
        }
        // 만약 newFreq가 0.0 이하이면 기존 noteName 값을 유지 (업데이트하지 않음)
    }

    private fun showChord(chord: String) {
        _noteName.value = chord;
    }

    /**
     * 특정 줄(0~5)에 해당하는 튜닝을 시작
     */
    fun startStringTuning(stringIndex: Int) {
        // 혹시 이미 캡처 중이면 stop 후 재시작
        stopAudioProcessing()

        selectedStringIndex = stringIndex
        targetFrequency = standardFrequencies[stringIndex]
        targetNoteName = standardNoteNames[stringIndex]

        audioCapture.startRecording()

        // 초기 피드백
        _tuningFeedback.value = "$targetNoteName - 튜닝을 시작합니다."
        //Log.d("TuningViewModel", "튜닝 시작: index=$stringIndex, targetFreq=$targetFrequency")
    }

    /**
     * 오디오 캡쳐 + 재생 중지
     */
    fun stopAudioProcessing() {
        audioCapture.stopRecording()
        //audioCapture.stopRecording()
        _tuningFeedback.value = "" // or "튜닝 중지됨"
        selectedStringIndex = -1
        targetFrequency = 0.0
        targetNoteName = ""

        //audioPlayer.stop()
        //Log.d("TuningViewModel", "튜닝 중지")
    }
}
