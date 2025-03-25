package com.example.picktimeapp.ui.login


import androidx.lifecycle.ViewModel
import com.example.picktimeapp.network.LoginApi
import com.example.picktimeapp.network.LoginRequest
//import com.example.picktimeapp.network.LoginResponse
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


import android.util.Log
import com.example.picktimeapp.auth.TokenManager





@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginApi: LoginApi,
    private val tokenManager: com.example.picktimeapp.auth.TokenManager
) : ViewModel() {

    //    초기세팅
    fun reset() {
        email.value = ""
        password.value = ""
//        loginResult.value = null
        errorMessage.value = null
        isPasswordVisible.value = false

    }

    var email = mutableStateOf("")
        private set

    var password = mutableStateOf("")
        private set

    var isPasswordVisible = mutableStateOf(false)
        private set

    fun onEmailChanged(newEmail: String) {
        email.value = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        password.value = newPassword
    }

    fun togglePasswordVisibility() {
        isPasswordVisible.value = !isPasswordVisible.value
    }

    //    var loginResult = mutableStateOf<String?>(null)
    var errorMessage = mutableStateOf<String?>(null)

    fun login() {
        viewModelScope.launch {
            try {
                Log.d(
                    "LoginViewModel",
                    "📦 로그인 요청 바디: username='${email.value}', password='${password.value}'"
                )


                val response = loginApi.login(
                    username = email.value,
                    password = password.value
                )

                if (response.isSuccessful) {
                    val token = response.headers()["Authorization"]

                    if (token != null) {
                        tokenManager.saveAccessToken(token)
//                        loginResult.value = null  // 이후 필요 시 사용자 정보 가져오는 구조로 확장
                        Log.d("LoginViewModel", "✅ 로그인 성공 - 토큰: $token")
                    } else {
                        errorMessage.value = "토큰이 없습니다."
                        Log.e("LoginViewModel", "❌ 로그인 성공했지만 토큰 없음")
                    }
                } else {
                    val error = response.errorBody()?.string()
                    errorMessage.value = "로그인 실패: ${response.code()}"
                    Log.e("LoginViewModel", "❌ 로그인 실패 - 코드: ${response.code()}, 바디: $error")
                }

            } catch (e: Exception) {
                errorMessage.value = "로그인 실패: ${e.message}"
                Log.e("LoginViewModel", "❌ 로그인 실패 - 예외 발생: ${e.message}", e)
            }
        }
    }
}