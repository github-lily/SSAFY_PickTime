//package com.example.picktimeapp.data.model

//data class YoloBox(
//    val classId: Int,
//    val confidence: Float,
//    val centerX: Float,
//    val centerY: Float,
//    val width: Float,
//    val height: Float
//)
//
//data class YoloResult(
//    val detections: List<YoloBox>
//)


package com.example.picktimeapp.data.model

sealed class YoloResult {
    data class Class(val classId: Int, val confidence: Float): YoloResult()
    object None : YoloResult()
}

