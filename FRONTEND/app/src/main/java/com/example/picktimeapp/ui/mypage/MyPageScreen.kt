package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.picktimeapp.R
import androidx.compose.runtime.getValue

@Composable
fun MyPageScreen(viewModel: MyPageViewModel) {

    // collectAsState (StateFlow를 UI와 연결하는 방법)
    // - StateFlow를 실시간으로 구독해서 값이 바뀌면 자동으로 업데이트 됨, 리액트의 useState + useEffect
    val userInfo by viewModel.userInfo.collectAsState()
    val pickDayData by viewModel.pickDayData.collectAsState()

    Row (
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        //Painter - 이미지를 화면에 그릴 준비를 하는 타입
        val profileImage: Painter  = painterResource(
            id = when (userInfo?.level) {
                1 -> R.drawable.profile_level_1
                2 -> R.drawable.profile_level_2
                3 -> R.drawable.profile_level_3
                4 -> R.drawable.profile_level_4
                5 -> R.drawable.profile_level_5
                6 -> R.drawable.profile_level_6
                else -> R.drawable.profile_level_1

            }

        )
        Image(
            painter = profileImage,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(150.dp)
                .padding(end = 24.dp),
            // contentScale.Crop - 지정한 사이즈에서 넘치면 자르기
            contentScale = ContentScale.Crop
        )


        Column(
            // Row 안에서 세로 방향 가운데 정렬
            modifier = Modifier
                .align(Alignment.CenterVertically)
        ){
            Text(
                text = "피크타임",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "${pickDayData?.continued ?: 30}일 연속 유지중",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
            // 유저 정보 출력하기
            userInfo?.let {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Button(
                onClick = {},
                modifier = Modifier.padding(top = 16.dp)
            ){
                Text(text = "회원정보 수정")
            }

        }


    }
}

@Preview (showBackground = true)
@Composable
fun MyPageScreenPreview(){
    val previewViewModel = MyPageViewModel()
    MyPageScreen(viewModel = previewViewModel)
}