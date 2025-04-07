package com.example.picktimeapp.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface YoloServerApi {
    @Multipart
    @POST("detect/position")  // 수정해야함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    suspend fun sendFrame(
        @Part image: MultipartBody.Part
    ): Response<YoloPositionResponse>
}

data class YoloPositionResponse(
    val position: Boolean? = null,
    val chordSound: Boolean? = null
)
