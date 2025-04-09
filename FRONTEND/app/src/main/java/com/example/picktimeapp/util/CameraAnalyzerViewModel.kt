package com.example.picktimeapp.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.data.model.FingerDetectionResponse
import com.example.picktimeapp.network.ChordDetectApi
import com.example.picktimeapp.network.YoloPositionResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
                requestSessionIdAndSave(context)
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
                    Log.d("AI", "Position 인식 성공")
                } else {
                    Log.e("AI", "Position 인식 실패")
                }

                onResult(response)

            } catch (e: Exception) {
                Log.e("AI", "통신 오류: ${e.message}")
            }
        }
    }

    fun analyzeFrames(parts:  List<MultipartBody.Part>, sessionId: String) {
        viewModelScope.launch {
            Log.d("ANALYZE", "호출")
            val response = chordDetectApi.sendFrames(sessionId = sessionId, files = parts)
            Log.d("ANALYZE", response.toString())
//            if (response.isSuccessful) {
//                val result = response.body()?.string() ?: "null"
//                Log.d("TEST_RESULT", result)
//            } else {
//                Log.e("TEST", "에러 응답: ${response.code()}")
//            }
        }
    }

    fun requestSessionIdAndSave(context: Context){
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

}