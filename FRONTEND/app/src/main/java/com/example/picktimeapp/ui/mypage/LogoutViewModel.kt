package com.example.picktimeapp.ui.mypage


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.auth.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    fun logout(onFinished: () -> Unit) {
        viewModelScope.launch {
            tokenManager.clearToken()
            Log.d("LogoutViewModel", " 토큰 삭제 완료")

            // 확인용: 삭제된 토큰 가져와보기
            val token = tokenManager.getAccessToken().firstOrNull()
            Log.d("LogoutViewModel", "현재 토큰 상태: $token")
            onFinished()
        }
    }
}