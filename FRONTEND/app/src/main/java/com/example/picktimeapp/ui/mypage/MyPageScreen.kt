package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.picktimeapp.ui.theme.Brown40
import com.example.picktimeapp.ui.theme.DarkGreen10
import com.example.picktimeapp.ui.theme.Gray70
import com.example.picktimeapp.ui.theme.Pretendard
import com.example.picktimeapp.ui.theme.TitleFont

@Composable
fun MyPageScreen(viewModel: MyPageViewModel, navController: NavController) {

    // collectAsState (StateFlow를 UI와 연결하는 방법)
    // - StateFlow를 실시간으로 구독해서 값이 바뀌면 자동으로 업데이트 됨, 리액트의 useState + useEffect
    val userInfo by viewModel.userInfo.collectAsState()
    val pickDayData by viewModel.pickDayData.collectAsState()
    val fullpickDays = viewModel.getFullPickDayList()

    Surface (
        modifier = Modifier.fillMaxSize(),
        color = DarkGreen10
    ) {
        //BoxWithConstraints - 화면의 최대 너비 / 높이 값을 알아낼 수 있게 해주는 컴포저블
        BoxWithConstraints (
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 400.dp, top = 130.dp, end = 32.dp, bottom = 24.dp),
            contentAlignment = Alignment.TopCenter
        ){
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            //텍스트는 sp 형태라서 Float.sp로 바꿔주는 value를 붙여야함
            val textSizeLarge = screenWidth.value * 0.05f // 약 5%
            val textSizeSmall = screenWidth.value * 0.025f
            val buttonWidth = screenWidth * 0.15f
            val buttonHeight = screenHeight * 0.06f
            val buttonFontSize = screenWidth.value * 0.015f

            Column (
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 상단영역
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 100.dp, end = 100.dp)
                        .fillMaxWidth()
                ) {

                    // 이미지 영역
                    Box(
                        modifier = Modifier
                            .weight(0.38f) // 전체의 35%차지
                            .aspectRatio(1f) //정사각형 비율을 유지한다.
                            .background(color = Color.White, shape = CircleShape)
                            .border(width = 8.dp, color = Brown40, shape = CircleShape),
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

                    // 이미지와 오른쪽 텍스트 영역의 간격
                Spacer(modifier = Modifier.width(10.dp))

                    // 사진 오른쪽 영역
                    Column(
                        modifier = Modifier
                            .weight(0.6f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "피크타임",
                            fontSize = textSizeLarge.sp,
                            fontFamily = TitleFont,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "${pickDayData?.continued ?: 30}일 연속 유지중",
                            fontFamily = TitleFont,
                            fontSize = textSizeLarge.sp,
                        )

                        Spacer(modifier = Modifier.height(screenHeight * 0.07f))

                        // 유저 정보 출력하기
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            userInfo?.let {
                                Text(
                                    text = it.name,
                                    fontFamily = TitleFont,
                                    fontSize = textSizeSmall.sp,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                )
                                IconButton(
                                    onClick = {
                                        navController.navigate("editNickname")
                                    },
                                    modifier = Modifier
                                        .padding(start = 15.dp)
                                        .size(30.dp)
                                ) {
                                    Icon(
//                                        painter = painterResource(id = R.drawable.edit),
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Nickname",
                                    )
                                }
                            }
                        }

                        // 비밀번호 수정 버튼
                        Button(
                            onClick = {
                                navController.navigate("passwordCheck")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x33E0CDA8),
                                contentColor = Gray70
                            ),
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .width(buttonWidth)
                                .height(buttonHeight)
                        ){
                            Text(
                                text = "비밀번호 수정",
                                fontSize = buttonFontSize.sp,
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                }

                // 피크데이와 프로필 사이 간격
                Spacer(modifier = Modifier.height(screenHeight * 0.1f))

                // 프크데이
                Box (
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column (
                        modifier = Modifier
                            .padding(start = 24.dp)
                            .align(Alignment.CenterStart),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "PickDays",
                            fontFamily = TitleFont,
                            fontWeight = FontWeight.Normal,
                            fontSize = (screenWidth.value * 0.025f).sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                        PickDaysGrid(
                            pickDays = fullpickDays,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(screenHeight * 0.25f)
                        )
                    }
                }
            }



        }

    }
}

@Preview (showBackground = true)
@Composable
fun MyPageScreenPreview(){
    val previewViewModel = MyPageViewModel()
    val navController = rememberNavController()

    MyPageScreen(viewModel = previewViewModel,navController = navController )
}