package com.example.picktimeapp.data.repository

import com.example.picktimeapp.data.model.PickDay
import com.example.picktimeapp.data.model.PickDayResponse
import com.example.picktimeapp.data.model.UserInfo

class MyPageRepository {

    fun getUserInfo(): UserInfo {
        return UserInfo(
            username = "99minj0731@naver.com",
            name = "민동",
            level = 1
        )
    }

    fun getPickDays(): PickDayResponse {
        val mockDays = listOf(
            PickDay("2025-02-11", 3),
            PickDay("2025-02-12", 0),
            PickDay("2025-02-13", 2),
            PickDay("2025-02-14", 1),
        )
        return PickDayResponse(
            continued = 30,
            pickDays = mockDays
        )
    }
}