package com.example.picktimeapp.ui.practice

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.picktimeapp.network.PracticeListApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.picktimeapp.data.model.UserInfo
import com.example.picktimeapp.network.StageItem
import com.example.picktimeapp.network.UserApi


@HiltViewModel
class PracticeListViewModel @Inject constructor(
    private val practiceListApi: PracticeListApi,
    private val userApi: UserApi,
) : ViewModel() {


    // User 정보 받아오는 API 부분
    private val _userInfo = mutableStateOf<UserInfo?>(null)
    val userInfo: State<UserInfo?> = _userInfo

    init {
        fetchUserInfo()
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            try {
                val response = userApi.getUserInfo()
                if (response.isSuccessful) {
                    _userInfo.value = response.body()
                }
            } catch (e: Exception) {
                // 에러 처리 필요 시 추가
            }
        }
    }
    var stageList by mutableStateOf<List<StageItem>>(emptyList())
        private set

    var clearRate by mutableStateOf(0.0)
        private set

    init {
        fetchPracticeStages()
    }

    private fun fetchPracticeStages() {
        viewModelScope.launch {
            try {
                val response = practiceListApi.getPracticeList()
                stageList = response.stages
                clearRate = response.clearRate
                Log.d("✅ DEBUG", "첫 번째 Stage의 Step 수: ${stageList.firstOrNull()?.steps?.size}")

            } catch (e: Exception) {
                Log.e("PracticeListViewModel", "Error fetching stages", e)
            }
        }
    }
}
