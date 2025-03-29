package com.example.picktimeapp.data.model
//김민정이 타입 정해놓는 파일

data class PickDay (
    val completedDate: String,
    val pickCount: Int
)

data class PickDayResponse(
    val continued: Int,
    val pickDays: List<PickDay>
)

data class UserInfo (
    val username: String, //이메일
    val name: String, // 진짜 이름
    val level: Int //레벨에 따라서 이미지 다르게 렌더링
)

data class UpdateNameRequest(
    val name: String
)
