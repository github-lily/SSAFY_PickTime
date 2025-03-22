package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@Composable
fun MyPageScreen(viewModel: MyPageViewModel) {

    // collectAsState (StateFlow를 UI와 연결하는 방법)
    // - StateFlow를 실시간으로 구독해서 값이 바뀌면 자동으로 업데이트 됨, 리액트의 useState + useEffect
    val userInfo by viewModel.userInfo.collectAsState()
    val pickDayData by viewModel.pickDayData.collectAsState()
    val fullpickDays = viewModel.getFullPickDayList()

    Surface (
        modifier = Modifier.fillMaxSize(),
        color =  Color(0xFFFFFDF8)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column (
                modifier = Modifier
                    .widthIn(max = 1000.dp)
                    .fillMaxSize()
                    .padding(24.dp)
            ){
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(color = Color.White, shape = CircleShape)
                            .border(width = 4.dp, color = Color(0xFFA57145), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ){
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
                                .fillMaxSize()
                                .clip(CircleShape),
                            // contentScale.Crop - 지정한 사이즈에서 넘치면 자르기
                            contentScale = ContentScale.Crop
                        )

                    }
                    Spacer(modifier = Modifier.width(24.dp))

                    Column(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x33E0CDA8),
                                contentColor = Color(0xFF66656C)
                            ),
                            modifier = Modifier.padding(top = 16.dp)
                        ){
                            Text(text = "회원정보 수정")
                        }
                    }
                }
                // 피크데이 잔디심기
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "PickDays",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                PickDaysGrid(
                    pickDays = fullpickDays,
                    modifier = Modifier
                        .fillMaxSize()
                        .height(240.dp)
                )

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