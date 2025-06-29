package com.example.picktimeapp.ui.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.picktimeapp.auth.TokenManager
import com.example.picktimeapp.data.model.PickDay
import com.example.picktimeapp.data.model.PickDayResponse
import com.example.picktimeapp.data.model.UserInfo
import com.example.picktimeapp.data.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// ViewModel - 리액트의 useEffect + useState, 데이터를 가져와서 상태를 설정하고 관리하는 중간 관리자 역할
@HiltViewModel
class MyPageViewModel @Inject constructor(
    //private - 내부 전용 이라는 뜻, 이 변수나 함수는 이 클래스 안에서만 사용할 수 있음을 명시, 데이터 보호 + 안전성 및 실수로 외부에서 상태를 변경하지 못하게 하려고!
    private val repository: MyPageRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    // MutableStateFlow - 리액트의 useState() asStateFlow - 외부에서는 읽기만 가능하게 상태 보호 StateFlow - state, 리턴된 상태값 _userInfo - 상태 변경!! [state, setState] 에서 setState 같은 것 !
    // 회원정보 상태 관리
    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo.asStateFlow()
    // 피크데이 상태 관리
    private val _pickDayData = MutableStateFlow<PickDayResponse?>(null)
    val pickDayData:StateFlow<PickDayResponse?> = _pickDayData.asStateFlow()

    //init - 리액트의 useEffect(() => {...}), 컴포넌트가 마운트될 때 한 번 실행됨.
    init {
        // 뷰모델이 생성되자마자 토큰을 받아서 API 호출!
        viewModelScope.launch {
            tokenManager.getAccessToken().collect { token ->
                Log.d("MyPageVM", "토큰 받아옴: $token")
                if (!token.isNullOrEmpty()) {
                    loadUserInfo()
                    loadPickDays()
                }
            }
        }
        //모델 뷰가 처음 만들어질 때 로딩 // 이 코드는 토큰이 반영되기 전에 실행이 돼서 전의 값이 나옴
//        loadPickDays()
    }
    private suspend fun loadUserInfo() {
        val result = repository.getUserInfo()
        Log.d("MyPageVM", "유저 정보 API 결과: $result")
        _userInfo.value = result
    }

    // load~~ - 리액트의 fetch + setState(), API 호출 후 상태를 업데이트하는 함수
    private fun loadPickDays(){
        viewModelScope.launch {
            val result = repository.getPickDays()
            Log.d("MyPageVM", "PickDays API 결과: $result" )
            _pickDayData.value = result
        }
    }

    //잔디 105개로 만들기

    fun getFullPickDayList(): List<PickDay> {
        val realData = pickDayData.value?.pickDays ?: emptyList()
        if (realData.isEmpty()) return emptyList()

        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateMap = realData.associateBy { it.completedDate }

        // 오늘 날짜 기준으로 315일 전(= 약 45주 전)
        val today = LocalDate.now()
        val startDate = today.minusDays(314) // 315일이면 총 45 * 7 = 315칸

        // 시작일을 무조건 그 주의 일요일로 맞춤 (잔디 왼쪽 정렬)
        val adjustedStart = startDate.with(java.time.DayOfWeek.SUNDAY)

        val fullDates = generateSequence(adjustedStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(today) }
            .toList()

        return fullDates.map { date ->
            val key = date.format(formatter)
            val original = dateMap[key]
            PickDay(
                completedDate = key,
                pickCount = original?.pickCount ?: 0
            )
        }
    }
}

