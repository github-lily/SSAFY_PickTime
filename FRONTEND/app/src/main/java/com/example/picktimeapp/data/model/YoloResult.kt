package com.example.picktimeapp.data.model

sealed class YoloResult {
    data class Class(val classId: Int, val confidence: Float): YoloResult()
    object None : YoloResult()
}
