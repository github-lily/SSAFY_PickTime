package com.example.picktimeapp.data.model

import com.google.gson.annotations.SerializedName

data class FingerDetectionResponse(
    @SerializedName("detection_done")
    val detectionDone: Boolean,

    @SerializedName("finger_positions")
    val fingerPositions: Map<String, FingerPositionData>
)

data class FingerPositionData(
    val fretboard: Int?,
    val string: Int?
)
