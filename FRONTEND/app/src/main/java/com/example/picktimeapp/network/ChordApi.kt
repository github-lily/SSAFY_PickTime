package com.example.picktimeapp.network

import com.example.picktimeapp.data.model.FingerDetectionResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChordDetectApi{

    @POST("detect/{sessionId}")
    suspend fun detectChord(
        @Path("sessionId") sessionId: String
    ): Response<FingerDetectionResponse>

    @GET("test")
    suspend fun test() : Response<ResponseBody>

}