package com.example.picktimeapp.ui.practice

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.camera.CameraPreview
import com.example.picktimeapp.ui.theme.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.BoxWithConstraints


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeStep4Screen() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight


        Scaffold(
            topBar = {
                Column(modifier = Modifier.padding(top = screenHeight * 0.02f)) {
                    CenterAlignedTopAppBar(
                        modifier = Modifier.height(screenHeight * 0.1f),
                        title = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "연습모드",
                                    fontSize = (screenWidth * 0.025f).value.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = TitleFont,
                                    color = Gray90
                                )
                            }
                        },
                        actions = {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(screenWidth * 0.05f) // 아이콘 들어갈 공간 확보!
                                    .padding(end = screenWidth * 0.02f),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { /* 일시정지 동작 */ },
                                    modifier = Modifier
                                        .fillMaxSize() // 박스 크기 꽉 채우기
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.pause_btn),
                                        contentDescription = "Pause",
                                    )
                                }
                            }
                        }

                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                // 피드백 텍스트 박스
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = screenHeight * 0.03f)
                        .width(screenWidth * 0.8f)
                        .height(screenHeight * 0.07f)
                        .background(Brown20, shape = RoundedCornerShape(screenHeight * 0.035f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "잘했어요! 이번엔 박자에 맞게 연주해볼까요?",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray90,
                        fontWeight = FontWeight.Normal,
                        fontFamily = TitleFont,
                        fontSize = (screenWidth * 0.020f).value.sp
                    )
                }


                // 프렛보드 이미지
                Image(
                    painter = painterResource(id = R.drawable.guitar_practice_neck),
                    contentDescription = "기타 프렛보드",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = -screenHeight * 0.05f)
                        .fillMaxWidth()
                        .height(screenHeight * 0.40f),
                    contentScale = ContentScale.FillBounds
                )

                // 노트 (정적 위치 - 비율 기반)
                Box(
                    modifier = Modifier
                        .size(
                            width = screenWidth * 0.12f,
                            height = screenHeight * 0.06f
                        )
                        .align(Alignment.CenterEnd)
                        .padding(end = screenWidth * 0.18f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brown60),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        color = Color.White,
                        fontSize = (screenWidth * 0.045f).value.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 카메라 프리뷰
                CameraPreview(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(40.dp)
                        .size(
                            width = screenWidth * 0.20f,
                            height = screenHeight * 0.20f
                        )
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}
