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
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.data.model.FingerPositionData
import com.example.picktimeapp.network.ChordDetectApi
import com.example.picktimeapp.network.YoloServerApi
import com.example.picktimeapp.util.Utils.bitmapToMultipart
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

    fun setChordName(name: String) {
        currentChordName = name
    }

    // AI 응답 관리
    fun handleAiResponse(
        fingerPositions: Map<String, FingerPositionData>?,
        detectionDoneFromServer: Boolean, // 탐지완료
        audioOk: Boolean
    ) {
        if (!detectionDoneFromServer) {
            _detectionDone.value = false
            feedbackMessage = "연결이 끊겼어요. 손을 떼고 화면에 기타가 모두 보이도록 해주세요."
            isCorrect = false
            return
        }

        // 연결 다시 정상됨
        if (!detectionDone.value && detectionDoneFromServer) {
            _detectionDone.value = true
            feedbackMessage = "다시 연주해 보세요."
            isCorrect = false
            return
        }

        // 기준 코드 정보와 비교
        val expected = standardMap[currentChordName] ?: return
        val comparisonResult = checkFingerMatch(expected, fingerPositions ?: emptyMap())

        if (comparisonResult && audioOk) {
            isCorrect = true
            feedbackMessage = "잘했어요!"
        } else {
            isCorrect = false
            showSequentialFeedback(expected, fingerPositions ?: emptyMap())
        }
    }

    private fun checkFingerMatch(
        expected: Map<String, FingerPositionData>,
        actual: Map<String, FingerPositionData>
    ): Boolean {
        return expected.all { (finger, pos) ->
            actual[finger]?.let {
                it.fretboard == pos.fretboard && it.string == pos.string
            } ?: false
        }
    }

    // 피드백을 담은 리스트(하나씩 시간차로 보여질 예정)
    private fun makeFeedbackList(
        expected: Map<String, FingerPositionData>,
        actual: Map<String, FingerPositionData>
    ): List<String> {
        val messages = mutableListOf<String>()

        for ((finger, correct) in expected) {
            val actualPos = actual[finger]
            if (actualPos == null) {
                messages.add("${finger}번 손가락이 인식되지 않았어요")
                continue
            }
            if (actualPos.string != correct.string) {
                messages.add("${finger}번 손가락을 ${correct.string}번 줄로 옮겨주세요")
            }
            if (actualPos.fretboard != correct.fretboard) {
                messages.add("${finger}번 손가락을 ${correct.fretboard}프렛으로 옮겨주세요")
            }
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

    // detection_done == false일 때, 프레임 1장을 서버에 보내 탐지 시도
    fun sendSingleFrame(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val sessionId = getSessionId(context)

                if(sessionId == null){
                    Log.e("세션", "세션 ID가 null입니다.")
                    return@launch
                }

                val part = bitmapToMultipart(bitmap,"image") // 아래 확장함수 참고
                val response = chordDetectApi.sendFrame(sessionId = sessionId, file = part)

                // 탐지 여부 상태 업데이트
                _detectionDone.value = response.detectionDone

                // 탐지 완료되었으면 결과 저장
                // 응답으로 detection_done 값과 finger_positions 수신
                if (response.detectionDone && response.fingerPositions != null) {
                    _fingerPositions.value = response.fingerPositions
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "1장 전송 오류: ${e.message}")
            }
        }
    }

    // 연주 시점에 10장 모아서 서버에 보내 정확한 코드 판별
    fun sendFrameList(bitmaps: List<Bitmap>) {
        viewModelScope.launch {
            try {
                val sessionId = getSessionId(context)

                if(sessionId == null){
                    Log.e("세션", "세션 ID가 null입니다.")
                    return@launch
                }

                val parts = bitmaps.mapIndexed { idx, bitmap ->
                    bitmapToMultipart(bitmap, "image$idx.jpg")
                }

                val response = chordDetectApi.sendFrames(sessionId = sessionId, files = parts)

                if (response.fingerPositions != null) {
                    _fingerPositions.value = response.fingerPositions

                    // 기준 코드 다시 불러오기
                    val expected = standardMap[currentChordName] ?: return@launch

                    // 여기서 기준 코드와 비교해서 결과 판단도 가능
                    // 응답으로 finger_positions 받아서 기준 코드와 비교
                    val result = checkFingerMatch(expected, fingerPositions.value ?: emptyMap())
                    _correctChord.value = result
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "10장 전송 오류: ${e.message}")
            }
        }
    }

}
