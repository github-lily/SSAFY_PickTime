package com.example.picktimeapp.ui.practice

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.navigation.NavController
import com.example.picktimeapp.ui.components.PracticeTopBar
import com.example.picktimeapp.ui.nav.Routes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeChordInfoScreen(navController: NavController) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        // 👉 Scaffold 상단 TopBar
        Scaffold(
            containerColor = DarkGreen10,
            topBar = {
               PracticeTopBar(
                   titleText = "코드연습",
                   onPauseClick = { /* 일시정지 기능 */ }
               )
            },
            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(screenWidth * 0.02f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 왼쪽 영역
                        BoxWithConstraints(
                            modifier = Modifier
                                .weight(1f)
                                .padding(innerPadding)
                        ) {
                            val screenWidth = maxWidth
                            val baseWidth = 800.dp // 기준 너비 (예: 800dp 기준으로 디자인)
                            val scale = screenWidth / baseWidth

                            // 💬 말풍선 + 텍스트
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .align(Alignment.CenterEnd)
                                    .offset(x = screenWidth * 0.15f)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.talkballon),
                                    contentDescription = "말풍선",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                )

                                Text(
                                    text = buildAnnotatedString {
                                        append("이번 시간에는\n")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("G코드")
                                        }
                                        append("를 배워볼게요")
                                    },
                                    fontFamily = TitleFont,
                                    fontSize = 40.sp * scale,      // ✅ 화면 크기 반응형
                                    lineHeight = 60.sp * scale,    // ✅ 화면 크기 반응형
                                    color = Gray90,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                        .wrapContentSize(Alignment.Center)
                                )
                            }
                        }

                        // 오른쪽 영역
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            // 🦒 기린 이미지
                            Image(
                                painter = painterResource(id = R.drawable.girin_teacher),
                                contentDescription = "기린 선생님",
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = screenWidth * 0.06f, top = screenWidth * 0.01f)
                                    .fillMaxHeight(0.6f)
                            )
                        }
                    }

                    // 다음 버튼
                    IconButton(
                        onClick = { navController.navigate(Routes.PRACTICE_CHORDPRESS) }, // ✅ 다음 화면으로 이동
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = screenWidth * 0.03f,
                                bottom = screenHeight * 0.03f
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "다음으로",
                            modifier = Modifier.size(screenWidth * 0.2f), // 아이콘 크기 조절
                            tint = Gray90 // 아이콘 색 (원하는 색상으로 바꿔도 돼요)
                        )
                    }

                }
            }
        )
    }
}


