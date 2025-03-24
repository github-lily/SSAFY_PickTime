package com.example.picktimeapp.network

// API 통신 데이터 정의

import retrofit2.http.Body
import retrofit2.http.POST

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
    @POST("login")  // 예: "auth/login"
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
