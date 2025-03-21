package com.example.picktimeapp.data.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path

interface ApiService {
    // 사용자 인증 관련 API
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("auth/signup")
    suspend fun signup(@Body signupRequest: SignupRequest): SignupResponse

    // 레슨 관련 API
    @GET("lessons")
    suspend fun getLessons(): List<LessonResponse>

    @GET("lessons/{lessonId}")
    suspend fun getLessonDetail(@Path("lessonId") lessonId: String): LessonDetailResponse
}

// 요청/응답 데이터 클래스들
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val userId: String)

data class SignupRequest(val name: String, val email: String, val password: String)
data class SignupResponse(val success: Boolean, val message: String)

data class LessonResponse(val id: String, val title: String, val description: String, val thumbnailUrl: String)
data class LessonDetailResponse(val id: String, val title: String, val content: String, val videoUrl: String)