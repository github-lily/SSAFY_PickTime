package com.example.picktimeapp.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.data.model.PasswordCheckRequest
import com.example.picktimeapp.network.PasswordConfirmApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordCheckViewModel @Inject constructor(
    private val userApi: PasswordConfirmApi) : ViewModel() {

    fun checkPassword(
        password: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = userApi.checkPassword(PasswordCheckRequest(password))
                if (response.isSuccessful) {
                    onSuccess()
                }else {
                    onFailure()
                }
            }catch (e: Exception) {
                onFailure()
            }
        }
    }

}