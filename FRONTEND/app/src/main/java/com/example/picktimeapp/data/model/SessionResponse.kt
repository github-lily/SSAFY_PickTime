package com.example.picktimeapp.data.model

import com.google.gson.annotations.SerializedName

data class SessionResponse(
    @SerializedName("session_id")
    val sessionId: String
)