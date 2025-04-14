package com.example.picktimeapp.ui.practice
// 기린이 나와서 코드 뭐할건지 알려주는 페이지
// step 1,2 에서만 나옴

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
import androidx.compose.ui.unit.*
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.example.picktimeapp.ui.components.PauseDialogCustom
import com.example.picktimeapp.ui.components.PracticeTopBar
import com.example.picktimeapp.ui.nav.Routes
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay


@Composable
fun PracticeChordInfoScreen(
    navController: NavController,
    stepId: Int,
    viewModel: PracticeStepViewModel = hiltViewModel()
) {

    val stepData = viewModel.stepData.value
    val chords = stepData?.chords.orEmpty()


    // ✅ API 최초 호출
    LaunchedEffect(stepId) {
        viewModel.fetchPracticeStep(stepId)
    }

    // ✅ 3초 후 ChordCheckScreen으로 이동
    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate("practicechordcheck/$stepId") {
            popUpTo("practicechordinfo/$stepId") { inclusive = true }
        }
    }

    val chordName = if (chords.isNotEmpty()) chords.first().chordName else "로딩 중..."


    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // 일시정지 모달
        val showPauseDialog = remember { mutableStateOf(false) }


        Scaffold(
            containerColor = DarkGreen10,
            topBar = {
               PracticeTopBar(
                   titleText = "코드연습",
                   onPauseClick = { showPauseDialog.value = true }
               )
            },
            content = { innerPadding ->
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
//                            .padding(screenWidth * 0.02f),
                            .weight(1f),
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
                                            append(chordName)
                                        }
                                        append("코드를 배워볼게요")
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = screenWidth * 0.03f, bottom = screenHeight * 0.03f)
                    ) {
                        IconButton(
                            onClick = { navController.navigate("practicechordcheck/$stepId") },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(y = (-screenHeight * 0.01f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "다음으로",
                                modifier = Modifier.size(screenWidth * 0.2f),
                                tint = Gray90
                            )
                        }
                    }

                    // 일시정지 모달
                    if (showPauseDialog.value) {
                        PauseDialogCustom(
                            screenWidth = screenWidth,
                            onDismiss = { showPauseDialog.value = false },
                            onExit = {
                                showPauseDialog.value = false
                                navController.navigate(Routes.PRACTICE_LIST) {
                                    popUpTo(Routes.PRACTICE_LIST) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        )
    }
}


