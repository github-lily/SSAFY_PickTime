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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.camera.CameraPreview
import com.example.picktimeapp.ui.theme.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward

import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavController
import com.example.picktimeapp.ui.components.PracticeTopBar
import com.example.picktimeapp.ui.nav.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeChordPressScreen(navController: NavController) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val density = LocalDensity.current

        Scaffold(
            topBar = {
                PracticeTopBar(
                    titleText = "코드연습",
                    onPauseClick = { /* 일시정지 동작 */ }
                )}
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 상단 - 피드백 박스
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .align(Alignment.CenterHorizontally)
                        .height(screenHeight * 0.1f)
                        .padding(top = screenHeight * 0.01f)
                        .background(Brown20, shape = RoundedCornerShape(screenHeight * 0.035f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G코드를 눌러볼까요?",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray90,
                        fontWeight = FontWeight.Normal,
                        fontFamily = TitleFont,
                        fontSize = (screenWidth * 0.020f).value.sp
                    )
                }

                // 🎯 코드명 + 스피커 + 코드 이미지 (가운데 영역)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        // 왼쪽: 코드명 + 스피커 아이콘
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier
                                .padding(end = screenWidth * 0.04f , top=screenHeight * 0.02f)
                                .align(Alignment.Top)
                        ) {
                            Text(
                                text = "G",
                                fontSize = (screenWidth * 0.04f).value.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = TitleFont,
                                color = Gray90,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            IconButton(onClick = {
                                // TODO: 코드 사운드 재생 로직

                            },
                                modifier = Modifier.size(56.dp)
                                ) {
                                Image(
                                    painter = painterResource(id = R.drawable.speaker),
                                    contentDescription = "코드 사운드",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // 오른쪽: 코드 이미지
                        Image(
                            painter = painterResource(id = R.drawable.code_g),
                            contentDescription = "G 코드 이미지",
                            modifier = Modifier
                                .height(screenHeight * 0.4f),
                            contentScale = ContentScale.Fit
                        )
                    }
                }



                // 하단 - 카메라 프리뷰 + 다음 버튼
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = screenWidth * 0.03f, bottom = screenHeight * 0.03f)
                ) {
                    CameraPreview(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset {
                                with(density) {
                                    IntOffset(x = (-screenWidth * 0.05f).toPx().toInt(), y = 0)
                                }
                            }
                            .size(
                                width = screenWidth * 0.20f,
                                height = screenHeight * 0.20f
                            )
                            .clip(RoundedCornerShape(12.dp))
                    )

                    IconButton(
                        onClick = { navController.navigate(Routes.PRACTICE_STEP_4) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "다음으로",
                            modifier = Modifier.size(screenWidth * 0.2f),
                            tint = Gray90
                        )
                    }
                }
            }
        }
    }
}
