package com.example.picktimeapp.util

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}