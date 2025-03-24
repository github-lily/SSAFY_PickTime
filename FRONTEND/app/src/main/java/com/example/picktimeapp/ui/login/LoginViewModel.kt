package com.example.picktimeapp.ui.login


import androidx.lifecycle.ViewModel
import com.example.picktimeapp.network.LoginApi
import com.example.picktimeapp.network.LoginRequest
import com.example.picktimeapp.network.LoginResponse
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


import android.util.Log

val request = LoginRequest(username = "test@email.com", password = "1234")



@HiltViewModel
class LoginViewModel @Inject constructor(private val loginApi: LoginApi) : ViewModel() {
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

    var loginResult = mutableStateOf<LoginResponse?>(null)
    var errorMessage = mutableStateOf<String?>(null)

    fun login() {
        viewModelScope.launch {
            try {
                val response = loginApi.login(
                    LoginRequest(
                        username = email.value,
                        password = password.value
                    )
                )
                loginResult.value = response
                Log.d("LoginViewModel", "로그인 성공: ${response.username}")
            } catch (e: Exception) {
                errorMessage.value = "로그인 실패: ${e.message}"
                Log.e("LoginViewModel", "로그인 실패", e)
            }
        }
    }
}

