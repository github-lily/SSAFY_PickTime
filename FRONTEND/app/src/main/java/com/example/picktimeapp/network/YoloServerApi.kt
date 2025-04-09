package com.example.picktimeapp.network

import com.example.picktimeapp.data.model.FingerPositionData
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface YoloServerApi {
    // ✅ 프레임 한 장 전송 (탐지 시도용)
    @Multipart
    @POST("detect/position") // URL은 정확히 맞게 나중에 수정
    suspend fun sendFrame(
        @Part image: MultipartBody.Part
    ): YoloPositionResponse

    // ✅ 프레임 여러 장 전송 (코드 판단용)
    @Multipart
    @POST("detect/frames")
    suspend fun sendFrames(
        @Part images: List<MultipartBody.Part>
    ): YoloPositionResponse
}

// ✅ 응답 데이터 클래스
data class YoloPositionResponse(
    val detectionDone: Boolean,
    val fingerPositions: Map<String, FingerPositionData>?
)

