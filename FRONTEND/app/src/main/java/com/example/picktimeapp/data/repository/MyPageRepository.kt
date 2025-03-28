package com.example.picktimeapp.data.repository

import android.util.Log
import com.example.picktimeapp.data.model.UserInfo
import com.example.picktimeapp.network.UserApi
import javax.inject.Inject

class MyPageRepository @Inject constructor(
    private val userApi: UserApi
) {
    suspend fun getUserInfo(): UserInfo? {
        return try {
            val response = userApi.getUserInfo()
            Log.d("MyPageRepo", "응답 전체: $response")
            Log.d("MyPageRepo", "응답 바디: ${response.body()}")
            Log.d("MyPageRepo", "응답 성공 여부: ${response.isSuccessful}")
            Log.d("MyPageRepo", "에러 바디: ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                response.body()
            }else null
        } catch (e: Exception){
            Log.e("MyPageRepo", "예외 발생: ${e.message}")
            null
        }
    }
}