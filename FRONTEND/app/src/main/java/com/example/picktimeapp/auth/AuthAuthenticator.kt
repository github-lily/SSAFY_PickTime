package com.example.picktimeapp.auth

import android.util.Log
import com.example.picktimeapp.network.ReissueApi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class AuthAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val reissueApi: ReissueApi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 401 응답이 이미 재시도된 요청이라면 중단
        if (response.request.header("Authorization") != null && responseCount(response) >= 1) {
            return null
        }

        val newAccessToken = runBlocking {
            try {
                val reissueResponse = reissueApi.reissueToken()
                val token = reissueResponse.headers()["Authorization"]
                if (!token.isNullOrBlank()) {
                    tokenManager.updateAccessToken(token)
                    return@runBlocking token
                }
                Log.d("AuthAuthenticator", "재발급 응답 코드: ${reissueResponse.code()}")
                Log.d("AuthAuthenticator", "Authorization 헤더: ${reissueResponse.headers()["Authorization"]}")

            } catch (e: Exception) {
                Log.e("Authenticator", "토큰 재발급 실패: ${e.message}")
            }
            null
        }

        return newAccessToken?.let {
            response.request.newBuilder()
                .header("Authorization", it)
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }
}
