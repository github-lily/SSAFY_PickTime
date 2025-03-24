package com.example.picktimeapp.network

// API 통신 데이터 정의

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class SignUpRequest(
    val name : String,
    val username: String,
    val password: String
)


interface SignUpApi {
    @POST("user")
    suspend fun login(@Body request: SignUpRequest): Response<Unit>
}
