package com.example.picktimeapp.network

import com.example.picktimeapp.data.model.FingerDetectionResponse
import com.example.picktimeapp.data.model.SessionResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ChordDetectApi{

    @POST("init")
    suspend fun init() : Response<SessionResponse>

    // ✅ 프레임 여러 장 전송 (코드 판단용)
    @Multipart
    @POST("detect/{sessionId}")
    suspend fun sendFrames(
        @Path("sessionId") sessionId: String,
        @Part images: List<MultipartBody.Part>
    ): FingerDetectionResponse


    @Multipart
    @POST("tracking/{sessionId}")
    suspend fun sendFrame(
        @Path("sessionId") sessionId: String,
        @Part image: MultipartBody.Part
    ): FingerDetectionResponse

    @GET("test")
    suspend fun test() : Response<ResponseBody>

}