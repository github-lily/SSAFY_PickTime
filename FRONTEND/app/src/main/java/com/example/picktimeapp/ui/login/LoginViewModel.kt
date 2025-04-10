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
import kotlin.coroutines.cancellation.CancellationException


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

    fun login(onSuccess: () -> Unit = {}, onFail: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = loginApi.login(
                    username = email.value,
                    password = password.value
                )

                if (response.isSuccessful) {
                    val token = response.headers()["Authorization"]
                    if (token != null) {
                        tokenManager.saveAccessToken(token)
                        Log.d("LoginViewModel", "✅ 로그인 성공 - 토큰: $token")
                        onSuccess() // 👉 로그인 성공 후 안전하게 화면 전환
                    } else {
                        val error = response.errorBody()?.string()
                        errorMessage.value = "회원정보를 찾을 수 없습니다."
                        Log.e("LoginViewModel", "❌ 로그인 실패 - 코드: ${response.code()}, 바디: $error")
                        onFail("회원정보를 찾을 수 없습니다.")
                    }
                } else {
                    errorMessage.value = "로그인 실패: 회원정보를 찾을 수 없습니다. ${response.code()}"
                    onFail("회원정보를 찾을 수 없습니다.")
                }

            } catch (e: CancellationException) {
                Log.w("LoginViewModel", "❗ 작업이 취소됨: ${e.message}")
                // 일부러 아무 처리 안 해도 됨 (정상적인 상황일 수도 있음)
            } catch (e: Exception) {
                errorMessage.value = "로그인 실패: ${e.message}"
                Log.e("LoginViewModel", "❌ 로그인 실패 - 예외 발생: ${e.message}", e)
                onFail("로그인 실패: ${e.message}")
            }
        }
    }
}