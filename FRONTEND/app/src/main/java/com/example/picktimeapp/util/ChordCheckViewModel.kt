package com.example.picktimeapp.util

// 손 위치 비교용 로직
// 앱 시작 후 최초 한 번 guitar_chord_fingerings_standard.json을 읽어와서 메모리에 저장
// 이후 finger_positions와 비교할 때 참조용으로 사용

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext


// "correct_chord" 판별 + 피드백 메시지 데이터 타입
data class ChordCheckResult(
    val correct: Boolean,
    val feedback: List<String>
)


@HiltViewModel
class ChordCheckViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    // 앱 시작 후 최초 한 번 guitar_chord_fingerings_standard.json을 읽어와서 메모리에 저장
    private val _standardMap = mutableStateOf<Map<String, Map<String, FingerPosition>>>(emptyMap())
    val standardMap: State<Map<String, Map<String, FingerPosition>>> = _standardMap

    init {
        loadStandardChordMap()
    }

    private fun loadStandardChordMap() {
        _standardMap.value = Utils.loadStandardChordMap(context)
    }

    // "correct_chord" 판별 + 피드백 메시지 생성 함수
    fun checkFingerPositions(
        chordName: String,
        detected: Map<String, FingerPosition>  // finger_positions
    ): ChordCheckResult {

        // 기준 JSON에서 해당 코드의 손가락 운지
        val standardChord = standardMap.value[chordName] ?: return ChordCheckResult(false, listOf("기준 코드 없음"))

        val feedback = mutableListOf<String>()
        var isCorrect = true

        for ((finger, correctPos) in standardChord) {
            val userPos = detected[finger]
            if (userPos == null) {
                feedback.add("${finger}번 손가락이 감지되지 않았어요")
                isCorrect = false
                continue
            }

            if (userPos.fretboard != correctPos.fretboard) {
                feedback.add("${finger}번 손가락을 ${correctPos.fretboard}프렛으로 옮기세요")
                isCorrect = false
            }

            if (userPos.string != correctPos.string) {
                feedback.add("${finger}번 손가락을 ${correctPos.string}번 줄로 옮기세요")
                isCorrect = false
            }
        }

        return ChordCheckResult(correct = isCorrect, feedback = if (isCorrect) listOf("잘했어요!") else feedback)
    }

}

