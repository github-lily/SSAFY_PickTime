package com.example.picktimeapp.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.picktimeapp.network.ChordDetectApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraAnalyzerViewModel @Inject constructor(
    private val chordDetectApi: ChordDetectApi
) : ViewModel() {

    fun analyzeFrame(bitmap: Bitmap) {
        viewModelScope.launch {
            val response = chordDetectApi.test()
            if (response.isSuccessful) {
                val result = response.body()?.string() ?: "null"
                Log.d("TEST_RESULT", result)
            } else {
                Log.e("TEST", "에러 응답: ${response.code()}")
            }
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