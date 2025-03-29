package com.example.picktimeapp.network

import com.example.picktimeapp.data.model.PickDayResponse
import com.example.picktimeapp.data.model.UpdateNameRequest
import com.example.picktimeapp.data.model.UserInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH


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


