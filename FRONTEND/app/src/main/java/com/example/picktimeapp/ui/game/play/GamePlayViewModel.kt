package com.example.picktimeapp.ui.game.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.auth.TokenManager
import com.example.picktimeapp.data.model.GamePlayResponse
import com.example.picktimeapp.data.model.GameScoreRequest
import com.example.picktimeapp.network.GameListsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamePlayViewModel @Inject constructor(
    private val api: GameListsApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _gameData = MutableStateFlow<GamePlayResponse?>(null)
    val gameData: StateFlow<GamePlayResponse?> = _gameData

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 게임 시작할 때
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

    // 게임 끝날 때
    fun sendGameResult(songId: Int,score: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val request =  GameScoreRequest(score = score)

                android.util.Log.d("GamePlayViewModel", "🔼 API 호출 시작: score = $score")

                api.postCompletedGame(songId, request)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "게임 결과 전송 실패: ${e.message}"
                android.util.Log.e("GamePlayViewModel", "❌ API 호출 실패: ${e.message}")
            }
        }
    }



}
