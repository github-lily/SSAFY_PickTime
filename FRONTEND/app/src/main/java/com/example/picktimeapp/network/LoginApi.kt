package com.example.picktimeapp.network

// API 통신 데이터 정의

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val username: String,
    val name: String,
    val level: Int
)

interface LoginApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
