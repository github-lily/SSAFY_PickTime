package com.example.picktimeapp.data.model

data class PickDay (
    val completedDate: String,
    val pickCount: Int
)

data class PickDayResponse(
    val continued: Int,
    val pickDays: List<PickDay>
)