package com.example.picktimeapp.network

// API 통신 데이터 정의

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

data class LoginRequest(
    val username: String,
    val password: String
)

//data class LoginResponse(
//    val username: String,
//    val name: String,
//    val level: Int
//)
//
//interface LoginApi {
//    @POST("login")
//    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
//}

interface LoginApi {
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<Unit>  // ✅ 응답 바디 없음
}