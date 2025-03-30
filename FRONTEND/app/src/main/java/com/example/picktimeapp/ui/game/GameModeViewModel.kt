package com.example.picktimeapp.ui.game

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.data.model.GameListsResponse
import com.example.picktimeapp.network.GameListsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameListsViewModel @Inject constructor(
    private val gameListsApi: GameListsApi
) : ViewModel() {

    // 게임 리스트 상태
    var gameLists by mutableStateOf<List<GameListsResponse>>(emptyList())
        private set

    // 로딩 여부나 에러 메시지
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchGameLists()
    }

    fun fetchGameLists() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = gameListsApi.getGameLists()
                if (response.isSuccessful) {
                    response.body()?.let {
                        gameLists = it
                        errorMessage = null
                    } ?: run {
                        errorMessage = "데이터 못받아옴"
                    }
                } else {
                    errorMessage = "서버 오류: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "네트워크 오류: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}