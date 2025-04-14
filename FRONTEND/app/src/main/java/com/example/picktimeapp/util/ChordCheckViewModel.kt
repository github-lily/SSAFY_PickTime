package com.example.picktimeapp.util

// 손 위치 비교용 로직
// 앱 시작 후 최초 한 번 guitar_chord_fingerings_standard.json을 읽어와서 메모리에 저장
// 이후 finger_positions와 비교할 때 참조용으로 사용

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.data.model.FingerPositionData
import com.example.picktimeapp.network.ChordDetectApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Named


@HiltViewModel
class ChordCheckViewModel @Inject constructor(
    application: Application,
    @Named("AI") private val chordDetectApi: ChordDetectApi
) : AndroidViewModel(application) {

    val context = getApplication<Application>().applicationContext

    // 기준 코드 정보
    private val standardMap = Utils.loadStandardChordMap(application.applicationContext)

    // 현재 코드명 (ex. G, C)
    var currentChordName by mutableStateOf("")
        private set

    // 서버 응답: 손 위치 판별 결과
    var isCorrect by mutableStateOf(false)
        private set

    // 연결 상태에 따른 메시지 출력
    var feedbackMessage by mutableStateOf("")
        private set


    // 음성 결과 저장
    var audioResult: Boolean? = null


    // detection 상태 관리
    private val _detectionDone = mutableStateOf(false)
    val detectionDone: State<Boolean> = _detectionDone

    private val _fingerPositions = mutableStateOf<Map<String, FingerPositionData>?>(null)
    val fingerPositions: State<Map<String, FingerPositionData>?> = _fingerPositions

    private val _correctChord = mutableStateOf<Boolean?>(null)
    val correctChord: State<Boolean?> = _correctChord

    private var cameraAnalyzer: CameraFrameAnalyzer? = null

    fun setCameraAnalyzer(analyzer: CameraFrameAnalyzer) {
        this.cameraAnalyzer = analyzer
    }

    fun getCameraAnalyzer(): CameraFrameAnalyzer? = cameraAnalyzer


    fun setChordName(name: String) {
        currentChordName = name
    }

    // 피드백 메시지 업데이트
    fun updateFeedbackMessage(message: String) {
        feedbackMessage = message
    }

    // 영상 + 음성 결과 최종 판단
    fun tryFinalCheck() {
        val fingers = fingerPositions.value
        val audioOk = audioResult
        Log.d("ChordCheck", "🎯 판별 시도 → audioResult: $audioOk, currentChord: $currentChordName, fingers : $fingers")

        if (fingers != null && audioOk != null) {
            val expected = standardMap[currentChordName] ?: return
            val comparisonResult = checkFingerMatch(expected, fingers)
            Log.d("ChordCheck", "✔️ tryFinalCheck 실행됨 / audioResult=$audioResult, fingers=${fingerPositions.value}, chord=$currentChordName")


            if (comparisonResult && audioOk) {
                isCorrect = true
                feedbackMessage = "정확히 연주했어요!"
                Log.d("ChordCheck", "🎯 판별 결과 → audioResult: $audioOk, currentChord: $currentChordName")

            } else {
                isCorrect = false
                showSequentialFeedback(expected, fingers)
            }
        }
    }

    // AI 응답 관리
    fun handleAiResponse(
        fingerPositions: Map<String, FingerPositionData>?,
        detectionDoneFromServer: Boolean, // 탐지완료
    ) {
        if (!detectionDoneFromServer) {
            _detectionDone.value = false
            feedbackMessage = "기타 인식이 끊겼어요. 손을 떼고 화면에 기타가 모두 보이도록 해주세요."
            isCorrect = false
            return
        }

        // 연결 다시 정상됨
        if (!detectionDone.value && detectionDoneFromServer) {
            _detectionDone.value = true

            // ✨ 기타는 인식됐지만 오디오가 감지되기 전이면 메시지 출력 보류
            if (audioResult != null) {
                feedbackMessage = "다시 연주해 보세요."
            }

            isCorrect = false
            return
        }



        // 👉 손 위치 저장 (판별은 tryFinalCheck)
        _fingerPositions.value = fingerPositions
        tryFinalCheck()

    }

    // 음성 매칭하는 함수
    fun checkAudioMatch(estimatedChord: String) {
        val isMatch = estimatedChord.equals(currentChordName, ignoreCase = true)
        audioResult = isMatch
        Log.d("ChordCheck", "음성 매칭 결과 : $audioResult")

        tryFinalCheck()
    }

    // 기준 손가락 중 2개 이상 정확히 맞으면 true
    private fun checkFingerMatch(
        expected: Map<String, FingerPositionData>,
        actual: Map<String, FingerPositionData>
    ): Boolean {
        var matchedCount = 0

        expected.forEach { (finger, expectedPos) ->
            val actualPos = actual[finger]
            val result = if (actualPos != null &&
                actualPos.fretboard == expectedPos.fretboard &&
                actualPos.string == expectedPos.string
            ) {
                matchedCount++
                Log.d("ChordCheck", "✅ $finger 번 손가락 일치 (fret=${actualPos.fretboard}, string=${actualPos.string})")
                true
            } else {
                Log.d("ChordCheck", "❌ $finger 번 손가락 불일치 | 예상: (fret=${expectedPos.fretboard}, string=${expectedPos.string}) vs 실제: ${actualPos?.fretboard}, ${actualPos?.string}")
                false
            }
        }

        Log.d("ChordCheck", "🎯 총 일치 손가락 수: $matchedCount → ${if (matchedCount >= 2) "정답 처리됨" else "오답 처리됨"}")
        return matchedCount >= 1
    }



    // 피드백을 담은 리스트(하나씩 시간차로 보여질 예정)
    private fun makeFeedbackList(
        expected: Map<String, FingerPositionData>,
        actual: Map<String, FingerPositionData>
    ): List<String> {
        val messages = mutableListOf<String>()

        for ((finger, correct) in expected) {
            val actualPos = actual[finger]

            if (actualPos == null || actualPos.fretboard == null || actualPos.string == null) {
                Log.d("ChordCheck", "⚠️ $finger 번 손가락 데이터 없음 또는 null")
                messages.add("${finger}번 손가락이 인식되지 않았어요")
                continue
            }

            if (actualPos.string != correct.string) {
                Log.d("ChordCheck", "❗ $finger 번 줄 번호 불일치 → 예상: ${correct.string}, 실제: ${actualPos.string}")
                messages.add("${finger}번 손가락을 ${correct.string}번 줄로 옮겨주세요")
            }

            if (actualPos.fretboard != correct.fretboard) {
                Log.d("ChordCheck", "❗ $finger 번 프렛 불일치 → 예상: ${correct.fretboard}, 실제: ${actualPos.fretboard}")
                messages.add("${finger}번 손가락을 ${correct.fretboard}프렛으로 옮겨주세요")
            }
        }

        if (messages.isEmpty()) {
            Log.d("ChordCheck", "👍 피드백 메시지 없음 (모두 일치 또는 인식 불가)")
        }

        return messages.ifEmpty { listOf("조금만 더 가까이 보여주세요") }
    }

    // 피드백 메시지를 시간차를 두고 보여주는 함수
    fun showSequentialFeedback(
        expected: Map<String, com.example.picktimeapp.data.model.FingerPositionData>,
        actual: Map<String, com.example.picktimeapp.data.model.FingerPositionData>
    ) {
        viewModelScope.launch {
            val messages = makeFeedbackList(expected, actual)
            isCorrect = false
            for ((i, message) in messages.take(3).withIndex()) {
                feedbackMessage = message
                delay(3000)
            }
        }



    }
}
