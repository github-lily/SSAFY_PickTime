package com.example.picktimeapp.data.model

import android.graphics.Bitmap

data class YoloBox(
    val classId: Int,
    val confidence: Float,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

data class YoloResult(
    val detections: List<YoloBox>,
    val bitmap: Bitmap? = null
)
