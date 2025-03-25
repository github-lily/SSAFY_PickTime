package com.example.picktimeapp.ui.signup

import androidx.lifecycle.ViewModel
import com.example.picktimeapp.network.SignUpApi
import com.example.picktimeapp.network.SignUpRequest
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import android.util.Log
import retrofit2.Response


@HiltViewModel
class SignupViewModel @Inject constructor(private val signUpApi : SignUpApi) : ViewModel() {

    // 입력값 상태관리
    var name = mutableStateOf("")
        private set

    var email = mutableStateOf("")
        private set

    var password = mutableStateOf("")
        private set

    var passwordcheck = mutableStateOf("")
        private set

    // 비밀번호 암호화 설정
    var isPasswordVisible = mutableStateOf(false)
        private set

    var isPasswordCheckVisible = mutableStateOf(false)
        private set

    // 변경 감지
    fun onNameChanged(newName: String) {
        name.value = newName

    }

    fun onEmailChanged(newEmail: String) {
        email.value = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        password.value = newPassword
    }

    fun onPasswordCheckChanged(newPasswordCheck: String) {
        passwordcheck.value = newPasswordCheck
    }

    // 비밀번호 암호화 토글
    fun togglePasswordVisibility() {
        isPasswordVisible.value = !isPasswordVisible.value
    }

    fun togglePasswordCheckVisibility() {
        isPasswordCheckVisible.value = !isPasswordCheckVisible.value
    }


    var signUpResult = mutableStateOf<Response<Unit>?>(null)
    var errorMessage = mutableStateOf<String?>(null)

    fun signup() {
        viewModelScope.launch {
            try {
                val response = signUpApi.login(
                    SignUpRequest(
                        name = name.value,
                        username = email.value,
                        password = password.value
                    )
                )
                signUpResult.value = response
                Log.d("SignUpViewModel", "회원가입 성공")
            } catch (e: Exception) {
                errorMessage.value = "회원가입 실패: ${e.message}"
                Log.e("SignUpViewModel", "회원가입 실패", e)
            }
        }
    }
}

