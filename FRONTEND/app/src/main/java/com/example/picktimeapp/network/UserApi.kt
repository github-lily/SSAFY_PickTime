package com.example.picktimeapp.network
// 마미페이지에서부터 발생하는 모든 api 인터페이스 모음

import com.example.picktimeapp.data.model.PasswordCheckRequest
import com.example.picktimeapp.data.model.PickDayResponse
import com.example.picktimeapp.data.model.UpdateNameRequest
import com.example.picktimeapp.data.model.UserInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST


// 마이페이지 회원 조회 및 닉네임 수정
interface UserApi {
    @GET("user")
    suspend fun getUserInfo(): Response<UserInfo>

    @PATCH("user")
    suspend fun updateUserName(@Body request: UpdateNameRequest): Response<Unit>
}

// 마이페이지의 피크타임
interface PickTimeApi {
    @GET("completed-activities")
    suspend fun getPickDays(): Response<PickDayResponse>
}

// 마이페이지 비밀번호 확인
interface PasswordConfirmApi {
    @POST("user/password")
    suspend fun checkPassword(@Body request: PasswordCheckRequest): Response<Unit>
}


