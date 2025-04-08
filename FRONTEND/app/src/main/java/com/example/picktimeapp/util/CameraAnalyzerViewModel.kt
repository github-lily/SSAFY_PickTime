package com.example.picktimeapp.util

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.picktimeapp.network.ChordDetectApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraAnalyzerViewModel @Inject constructor(
    private val chordDetectApi: ChordDetectApi
) : ViewModel() {
    fun analyzeFrame(bitmap: Bitmap) {
        // 서버 전송, 분석 결과 처리 등 로직
    }
}