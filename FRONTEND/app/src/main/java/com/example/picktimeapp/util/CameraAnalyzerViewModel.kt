package com.example.picktimeapp.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.audio.AudioComm
import com.example.picktimeapp.data.model.FingerDetectionResponse
import com.example.picktimeapp.network.ChordDetectApi
import com.example.picktimeapp.network.YoloPositionResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class CameraAnalyzerViewModel @Inject constructor(
    @Named("AI") private val chordDetectApi: ChordDetectApi,
    @ApplicationContext private val context : Context
) : ViewModel() {

    val positionDetected = mutableStateOf(false)
    private val TAG = "CameraAnalyzerViewModel"

    init {
        // 클래스 초기화 시 sessionIdCoroutineScope(Dispatchers.IO).launch 체크 후 없으면 요청

        CoroutineScope(Dispatchers.IO).launch {
            val sessionId = getSessionId(context)
            Log.d(TAG, "초기 sessionId: $sessionId")
            if (sessionId.isNullOrBlank()) {
                Log.d(TAG, "세션 없음 → 서버에 요청 시작")
                requestSessionIdAndSave()
            } else {
                Log.d(TAG, "이미 세션 있음: $sessionId")
            }
        }
    }

    fun analyzeFrame(
        bitmap: Bitmap,
        context: Context,
        onResult: (FingerDetectionResponse) -> Unit
    ) {
        viewModelScope.launch {
            val sessionId: String = getSessionId(context) ?: return@launch

            try {
                val imagePart = Utils.bitmapToMultipart(bitmap)
                val response = chordDetectApi.sendFrame(
                    sessionId = sessionId, file = imagePart
                )

                if (response.detectionDone == true) {
                    positionDetected.value = true
                    AudioComm.audioCaptureOn()
                    Log.d("AI", "Position 인식 성공")
                    Log.d("CameraAnalyzerViewModel", "Position 인식 성공 : ${response.detectionDone}")
                } else {
                    Log.e("CameraAnalyzerViewModel", "Position 인식 실패 : ${response.detectionDone}")
                }

                onResult(response)

            } catch (e: Exception) {
                Log.e("AI", "통신 오류: ${e.message}")
            }
        }
    }

    // 여러장 보내기
    fun analyzeFrames(
        parts: List<MultipartBody.Part>,
        context: Context,
        onResult: (FingerDetectionResponse) -> Unit
    ) {
        viewModelScope.launch {
            val sessionId = getSessionId(context)
            if (sessionId == null) {
                Log.e("ANALYZE", "세션 ID가 null입니다.")
                return@launch
            }

            try {
                val response = chordDetectApi.sendFrames(sessionId, parts)
                Log.d("ANALYZE", "5장 분석 결과: $response")
                onResult(response)
            } catch (e: Exception) {
                Log.e("ANALYZE", "5장 분석 중 오류: ${e.message}")
            }
        }
    }


    fun requestSessionIdAndSave() {
        viewModelScope.launch {
            try {
                val response = chordDetectApi.init()
                if (response.isSuccessful) {
                    val sessionId = response.body()?.sessionId
                    if (!sessionId.isNullOrBlank()) {
                        saveSessionId(context, sessionId)
                        Log.d("SESSION", "새로운 세션 저장됨: $sessionId")
                    }
                } else {
                    Log.e("SESSION", "세션 요청 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SESSION", "세션 요청 중 예외 발생: ${e.message}")
            }
        }
    }

    //private val deletionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    fun deleteSession(context: Context){
        GlobalScope.launch(Dispatchers.IO) {
            val sessionId = getSessionId(context) ?: return@launch
            chordDetectApi.stop(sessionId)
            clearDataStore(context)
            Log.d("SESSION", "세션 삭제")
        }
    }

    

}