package com.example.picktimeapp.network

import com.example.picktimeapp.data.model.UserInfo
import retrofit2.Response
import retrofit2.http.GET


// 마이페이지
interface UserApi {
    @GET("user")
    suspend fun getUserInfo(): Response<UserInfo>
}