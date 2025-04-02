package com.example.picktimeapp.ui.game.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.data.model.GamePlayResponse
import com.example.picktimeapp.network.GameListsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamePlayViewModel @Inject constructor(
    private val api: GameListsApi
) : ViewModel() {

    private val _gameData = MutableStateFlow<GamePlayResponse?>(null)
    val gameData: StateFlow<GamePlayResponse?> = _gameData

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadGamePlay(songId: Int) {
        viewModelScope.launch {
            try {
                val response = api.getGamePlay(songId)
                if (response.isSuccessful) {
                    _gameData.value = response.body()
                } else {
                    _errorMessage.value = "불러오기 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "에러: ${e.message}"
            }
        }
    }
}