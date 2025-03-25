package com.example.picktimeapp.ui.mypage

import androidx.lifecycle.ViewModel
import com.example.picktimeapp.data.model.PickDay
import com.example.picktimeapp.data.model.PickDayResponse
import com.example.picktimeapp.data.model.UserInfo
import com.example.picktimeapp.data.repository.MyPageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

// ViewModel - 리액트의 useEffect + useState, 데이터를 가져와서 상태를 설정하고 관리하는 중간 관리자 역할
class MyPageViewModel: ViewModel() {

    //private - 내부 전용 이라는 뜻, 이 변수나 함수는 이 클래스 안에서만 사용할 수 있음을 명시, 데이터 보호 + 안전성 및 실수로 외부에서 상태를 변경하지 못하게 하려고!
    private val repository = MyPageRepository()

    // MutableStateFlow - 리액트의 useState()
    // asStateFlow - 외부에서는 읽기만 가능하게 상태 보호
    // StateFlow - state, 리턴된 상태값
    // _userInfo - 상태 변경!! [state, setState] 에서 setState 같은 것 !

    // 회원정보 상태 관리
    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo.asStateFlow()

    // 피크데이 상태 관리
    private val _pickDayData = MutableStateFlow<PickDayResponse?>(null)
    val pickDayData:StateFlow<PickDayResponse?> = _pickDayData.asStateFlow()

    //init - 리액트의 useEffect(() => {...}), 컴포넌트가 마운트될 때 한 번 실행됨.
    init {
        //모델 뷰가 처음 만들어질 때 로딩
        loadUserInfo()
        loadPickDays()
    }

    // load~~ - 리액트의 fetch + setState(), API 호출 후 상태를 업데이트하는 함수
    private fun loadUserInfo(){
        _userInfo.value = repository.getUserInfo()
    }
    private fun loadPickDays(){
        _pickDayData.value = repository.getPickDays()
    }

    //잔디 105개로 만들기
    fun getFullPickDayList(): List<PickDay> {
        val realData = pickDayData.value?.pickDays ?: emptyList()

        val today = LocalDate.now()
        val startDate = today.minusDays(314)

        return (0..315).map { i ->
            val date = startDate.plusDays(i.toLong())
            val dataForDate = realData.find { it.completedDate == date.toString() }

            PickDay(
                completedDate = date.toString(),
                pickCount = dataForDate?.pickCount ?: 0
            )
        }
    }
}

