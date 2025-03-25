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

    // 닉네임 점검
    private fun isAllKorean(text: String): Boolean {
        return text.matches(Regex("^[가-힣]*$"))
    }

    private fun isAllEnglish(text: String): Boolean {
        return text.matches(Regex("^[a-zA-Z]*$"))
    }


    // 이메일 형식 점검
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // 비밀번호 암호화 설정
    var isPasswordVisible = mutableStateOf(false)
        private set

    var isPasswordCheckVisible = mutableStateOf(false)
        private set

    // 변경 감지
    fun onNameChanged(newName: String) {
        val byteLength = newName.toByteArray(Charsets.UTF_8).size
        when {
            byteLength <= 21 -> {
                name.value = newName
            }
            else -> {
                // 아무것도 하지 않음 → 입력 무시됨
            }
        }
    }

    fun onEmailChanged(newEmail: String) {
        email.value = newEmail
        if (errorMessage.value == "올바른 이메일 형식을 입력해주세요." && isValidEmail(newEmail)) {
            errorMessage.value = null
        }
    }


    fun onPasswordChanged(newPassword: String) {
        password.value = newPassword
        if (errorMessage.value == "비밀번호가 일치하지 않습니다." && newPassword == passwordcheck.value) {
            errorMessage.value = null
        }
    }

    fun onPasswordCheckChanged(newPasswordCheck: String) {
        passwordcheck.value = newPasswordCheck
        if (errorMessage.value == "비밀번호가 일치하지 않습니다." && newPasswordCheck == password.value) {
            errorMessage.value = null
        }
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
        // 👇 이메일 형식이 올바른지 하는지 체크
        if (!isValidEmail(email.value)) {
            errorMessage.value = "올바른 이메일 형식을 입력해주세요."
            return
        }

        // 👇 비밀번호와 비밀번호 확인이 일치하는지 체크
        if (password.value != passwordcheck.value) {
            errorMessage.value = "비밀번호가 일치하지 않습니다."
            return // 일치하지 않으면 아래 코드 실행 안 하고 종료
        }

        viewModelScope.launch {
            try {
                val response = signUpApi.signup(
                    SignUpRequest(
                        name = name.value,
                        username = email.value,
                        password = password.value
                    )
                )

                if (response.isSuccessful) {
                    signUpResult.value = response
                    errorMessage.value = null
                    Log.d("SignUpViewModel", "회원가입 성공")
                } else {
                    errorMessage.value = when(response.code()) {
                        409 -> "이미 사용 중인 이메일입니다."
                        else -> "회원가입 실패 (코드 : ${response.code()}"
                    }
                    signUpResult.value = null
                    Log.w("SignUpViewModel", "회원가입 실패 - ${response.code()}")
                }
                signUpResult.value = response

            } catch (e: Exception) {
                errorMessage.value = "네트워크 오류: ${e.message}"
                signUpResult.value = null
                Log.e("SignUpViewModel", "회원가입 실패 - 예외 발생", e)
            }
        }
    }
}

