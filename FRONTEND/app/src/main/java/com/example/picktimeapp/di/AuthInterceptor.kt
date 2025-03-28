package com.example.picktimeapp.di

import android.util.Log
import com.example.picktimeapp.auth.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.getAccessToken().first() }
        val originalRequest = chain.request()

        // 👇 login 요청에는 Authorization 헤더를 붙이지 않음
        if (originalRequest.url.encodedPath.endsWith("/login")) {
            return chain.proceed(originalRequest)
        }

        val newRequest = chain.request().newBuilder().apply {
            token?.let {
                Log.d("AuthInterceptor", "실제 붙이는 토큰: $it")
                addHeader("Authorization", "$it")
                // Content-type 헤더 추가하기
                addHeader("Content-Type", "application/json")
            }
        }.build()

        return chain.proceed(newRequest)
    }
}
