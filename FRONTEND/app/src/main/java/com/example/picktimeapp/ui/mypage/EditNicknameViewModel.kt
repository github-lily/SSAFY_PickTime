package com.example.picktimeapp.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.data.model.UpdateNameRequest
import com.example.picktimeapp.data.model.UserInfo
import com.example.picktimeapp.network.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditNicknameViewModel @Inject constructor(
    private val userApi: UserApi
) : ViewModel() {

    // 닉네임 상태 나오게 하기
    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname

    //API 로딩 여부
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 에러 메시지
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    // 사용자 닉네임 렌더링하기
    fun loadUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            val response = userApi.getUserInfo()
            if (response.isSuccessful) {
                val userInfo: UserInfo? = response.body()
                _nickname.value = userInfo?.name ?: "닉네임이 비어있음"
            } else {
                _errorMessage.value = "닉네임 가져오기 실패 2"
            }
            _isLoading.value = false
        }
    }

    // 사용자 닉네임 변경하기
    fun updateNickname(newName: String, onSuccess:() -> Unit) {
        viewModelScope.launch {
            val request = UpdateNameRequest(name = newName)
            val response = userApi.updateUserName(request)

            if(response.isSuccessful) {
                onSuccess()
            } else {
                _errorMessage.value = "닉네임 수정 실패"
            }
        }
    }

    // 닉네임 입력할 때마다 호출
    fun onNicknameChange(newName: String) {
        _nickname.value = newName
    }
}