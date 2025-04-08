//package com.example.picktimeapp.ui.practice
//// 스텝 4(노래 나오는 부분)
//
//import android.util.Log
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowForward
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//import com.example.picktimeapp.R
//import com.example.picktimeapp.ui.camera.CameraPreview
//import com.example.picktimeapp.ui.components.PauseDialogCustom
//import com.example.picktimeapp.ui.components.PracticeTopBar
//import com.example.picktimeapp.ui.game.play.getChordImageRes
//import com.example.picktimeapp.ui.nav.Routes
//import com.example.picktimeapp.ui.theme.Brown20
//import com.example.picktimeapp.ui.theme.Gray90
//import com.example.picktimeapp.ui.theme.TitleFont
//import kotlinx.coroutines.delay
//
//@Composable
//fun PracticeMusicScreenn(
//    stepId: Int,
//    navController: NavController,
//    viewModel: PracticeStepViewModel = hiltViewModel()
//) {
//    val stepData = viewModel.stepData.value
//    val song = stepData?.song
//    val organizedChords = song?.organizedChords ?: emptyList() // 내가 렌더링해야하는 코드
//
//    val allChords = remember(song) {
//        song?.chordProgression?.flatMap { it.chordBlocks } ?: emptyList()
//    }
//
//    // 각 코드당 시간 계산하기
//    val durationPerChord = remember {
//        if (allChords.isNotEmpty()){
//            (song?.durationSec?.toFloat() ?: 1f) / allChords.size
//        } else {
//            1f
//        }
//    }
//
//    // 애니메이션을 위한 경과 시간 상태
//    var elapsedTime by remember { mutableStateOf(0f) }
//
//    // 현재 코드 인덱스
//    val currentChordIndex = remember { mutableStateOf(0) }
//
//    // 시간에 따라 현재 코드 인덱스 업데이트 하기
//    LaunchedEffect(allChords, song?.durationSec) {
//        val startTime = System.currentTimeMillis()
//        while (currentChordIndex.value < allChords.size) {
//            val now = System.currentTimeMillis()
//            val elapsed = (now - startTime) / 1000f
//            elapsedTime = elapsed
//
//            val newIndex = (elapsed / durationPerChord).toInt()
//            if (newIndex != currentChordIndex.value && newIndex < allChords.size) {
//                currentChordIndex.value = newIndex
//                Log.d("PracticeMusicScreen", "🎸 코드 진행중: ${allChords[newIndex]}")
//            }
//            delay(16) // 60fps
//        }
//    }
//
//    LaunchedEffect(stepId) {
//        viewModel.fetchPracticeStep(stepId)
//    }
//
//    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
//        val screenWidth = maxWidth
//        val screenHeight = maxHeight
//        val density = LocalDensity.current
//
//        val showPauseDialog = remember { mutableStateOf(false) }
//
//        Scaffold(
//            topBar = {
//                PracticeTopBar(
//                    titleText = "",
//                    onPauseClick = { showPauseDialog.value = true }
//                )
//            }
//        ) { innerPadding ->
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .align(Alignment.CenterHorizontally)
//                        .fillMaxWidth(0.85f)
//                        .height(screenHeight * 0.1f)
//                        .padding(top = screenHeight * 0.01f)
//                        .background(Brown20, shape = RoundedCornerShape(screenHeight * 0.035f)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "잘했어요! 이번엔 박자에 맞게 연주해볼까요?",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = Gray90,
//                        fontWeight = FontWeight.Normal,
//                        fontFamily = TitleFont,
//                        fontSize = (screenWidth * 0.020f).value.sp
//                    )
//                }
//
//                // 코드 애니메이션 쪽
//                Box(
//                    modifier = Modifier
//                        .weight(1f)
//                        .fillMaxWidth(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.guitar_practice_neck),
//                        contentDescription = "기타 프렛보드",
//                        modifier = Modifier
//                            .align(Alignment.Center)
//                            .fillMaxWidth()
//                            .height(screenHeight * 0.40f),
//                        contentScale = ContentScale.FillBounds
//                    )
//                    PracticeChordSlidingBar(
//                        allChords = allChords,
//                        organizedChords = organizedChords,
//                        elapsedTime = elapsedTime,
//                        totalDuration = song?.durationSec?.toFloat() ?: 1f,
//                        screenWidth = screenWidth,
//                        screenHeight = screenHeight
//                    )
//                }
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(end = screenWidth * 0.03f, bottom = screenHeight * 0.03f)
//                ) {
//                    CameraPreview(
//                        modifier = Modifier
//                            .align(Alignment.BottomCenter)
//                            .offset {
//                                with(density) {
//                                    IntOffset(x = (screenWidth * 0.3f).toPx().toInt(), y = 0)
//                                }
//                            }
//                            .size(
//                                width = screenWidth * 0.20f,
//                                height = screenHeight * 0.20f
//                            )
//                            .clip(RoundedCornerShape(12.dp))
//                    )
//
//                    IconButton(
//                        onClick = { navController.navigate(Routes.PRACTICE_LIST) },
//                        modifier = Modifier
//                            .align(Alignment.BottomEnd)
//                            .padding(bottom = 8.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowForward,
//                            contentDescription = "다음으로",
//                            modifier = Modifier.size(screenWidth * 0.2f),
//                            tint = Gray90
//                        )
//                    }
//                }
//
//                if (showPauseDialog.value) {
//                    PauseDialogCustom(
//                        screenWidth = screenWidth,
//                        onDismiss = { showPauseDialog.value = false },
//                        onExit = {
//                            showPauseDialog.value = false
//                            navController.navigate(Routes.PRACTICE_LIST) {
//                                popUpTo(Routes.PRACTICE_LIST) { inclusive = true }
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun PracticeChordSlidingBar(
//    allChords: List<String>,
//    organizedChords: List<String>,
//    elapsedTime: Float,
//    totalDuration: Float,
//    screenWidth: Dp,
//    screenHeight: Dp
//) {
//    val totalWidthPx = with(LocalDensity.current) { (allChords.size * 140).dp.toPx() }
//    val progressRatio = elapsedTime / totalDuration
//    val offsetX = -progressRatio * totalWidthPx
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(screenHeight * 0.4f),
//        contentAlignment = Alignment.TopCenter
//    ) {
//        Row(
//            modifier = Modifier
//                .offset { IntOffset(offsetX.toInt(), 0) }
//                .wrapContentWidth()
//        ) {
//            allChords.forEach { chord ->
//                Box(
//                    modifier = Modifier
//                        .width(140.dp)
//                        .height(120.dp)
//                        .padding(horizontal = 6.dp),
//                    contentAlignment = Alignment.TopCenter
//                ) {
//                    if (chord in organizedChords) {
//                        Box(
//                            modifier = Modifier
//                                .background(Color(0xFFD9C2A5), RoundedCornerShape(12.dp))
//                                .padding(vertical = 24.dp, horizontal = 20.dp)
//                        ) {
//                            Text(
//                                text = chord,
//                                color = Color.Black,
//                                fontWeight = FontWeight.Bold,
//                                fontSize = 28.sp
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
