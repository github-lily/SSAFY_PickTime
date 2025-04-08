package com.example.picktimeapp.data.model

data class FingerDetectionResponse(
    val detectionDone: Boolean,
    val fingerPositions: Map<String, FingerPositionData>
)

data class FingerPositionData(
    val fretboard: Int?,
    val string: Int?
)
