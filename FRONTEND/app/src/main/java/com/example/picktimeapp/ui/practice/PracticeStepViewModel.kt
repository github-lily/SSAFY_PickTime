package com.example.picktimeapp.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.network.PracticeApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import android.util.Log
import com.example.picktimeapp.network.SongResponse

@HiltViewModel
class PracticeStepViewModel @Inject constructor(
    private val practiceApi: PracticeApi
) : ViewModel() {

    private val _songData = mutableStateOf<SongResponse?>(null)
    val songData: State<SongResponse?> = _songData

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    fun fetchStepSong(stepId: Int) {
        viewModelScope.launch {
            try {
                val response = practiceApi.getPracticeStep(stepId)
                if (response.isSuccessful) {
                    _songData.value = response.body()?.song
                } else {
                    _errorMessage.value = "오류: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류: ${e.message}"
                Log.e("PracticeStepViewModel", "API 호출 실패", e)
            }
        }
    }
}
