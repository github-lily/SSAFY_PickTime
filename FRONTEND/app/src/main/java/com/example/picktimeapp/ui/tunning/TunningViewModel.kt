package com.example.picktimeapp.ui.tunning

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TunningViewModel @Inject constructor() : ViewModel(){

    // 기린 이미지의 Y 오프셋 (초기값: 250dp로 아래에 숨겨둠)
    //val girinOffsetY = Animatable(900f) // Animatable은 Float 단위!
    val girinOffsetX = Animatable(-50f) // Animatable은 Float 단위!

    private val _targetOffsetY = mutableStateOf(900f) // dp 기준 float
    val targetOffsetY: State<Float> = _targetOffsetY

    fun stretchNeck(by: Float = 100f) {
        _targetOffsetY.value -= by
    }

}