package com.example.picktimeapp.data.model

import android.os.Message

//김민정이 타입 정해놓는 파일

data class PickDay (
    val completedDate: String,
    val pickCount: Int
)

data class PickDayResponse(
    val continued: Int,
    val pickDays: List<PickDay>
)

// 사용자 정보
data class UserInfo (
    val username: String, //이메일
    val name: String, // 진짜 이름
    val level: Int //레벨에 따라서 이미지 다르게 렌더링
)

// 닉네임 업데이트
data class UpdateNameRequest(
    val name: String
)

// 비밀번호 확인 요청
data class PasswordCheckRequest(
    val password: String
)


