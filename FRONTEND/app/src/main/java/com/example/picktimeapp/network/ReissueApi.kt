package com.example.picktimeapp.network

import retrofit2.Response
import retrofit2.http.POST

interface ReissueApi {
    @POST("reissue")
    suspend fun reissueToken(): Response<Unit> // accessToken은 Header로, refreshToken은 쿠키로 자동 포함
}
