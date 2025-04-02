package com.example.picktimeapp.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.network.PracticeStepApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import android.util.Log
import com.example.picktimeapp.network.PracticeStepResponse
import com.example.picktimeapp.network.SongResponse



@HiltViewModel
class PracticeStepViewModel @Inject constructor(
    private val practiceStepApi: PracticeStepApi
) : ViewModel() {

    private val _stepData = mutableStateOf<PracticeStepResponse?>(null)
    val stepData: State<PracticeStepResponse?> = _stepData

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    fun fetchPracticeStep(stepId: Int) {
        viewModelScope.launch {
            Log.d("PracticeStep", "stepId=$stepId → 호출 시작")
            try {
                val response = practiceStepApi.getPracticeStep(stepId)
                if (response.isSuccessful) {
                    _stepData.value = response.body()
                    Log.d("PracticeStep", "chords = ${_stepData.value?.chords}")

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


