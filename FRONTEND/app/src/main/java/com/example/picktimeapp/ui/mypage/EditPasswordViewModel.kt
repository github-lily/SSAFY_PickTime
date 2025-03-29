package com.example.picktimeapp.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.picktimeapp.data.model.PasswordUpdateRequest
import com.example.picktimeapp.network.PasswordUpdateApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPasswordViewModel @Inject constructor(
    private val passwordUpdateApi: PasswordUpdateApi
): ViewModel() {
    fun updatePassword(
        newPassword: String,
        onSuccess: () -> Unit,
//        onFailure: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = passwordUpdateApi.updatePassword(PasswordUpdateRequest(newPassword))
                if (response.isSuccessful) {
                    onSuccess()
                }else {
//                    onFailure()
                }
            } catch (e: Exception) {
//                onFailure()
            }
        }
    }
}