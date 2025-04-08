package com.example.picktimeapp.ui.practice
// 소리 내볼까요 패이지 음성인식 스텝2

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavController
import com.example.picktimeapp.ui.components.PauseDialogCustom
import com.example.picktimeapp.ui.components.PracticeTopBar
import com.example.picktimeapp.ui.nav.Routes
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picktimeapp.util.ChordImageMap



@Composable
fun PracticeChordListenScreen(
    navController: NavController,
    stepId : Int,
    viewModel: PracticeStepViewModel = hiltViewModel()
) {

    val stepData = viewModel.stepData.value
    val chords = stepData?.chords.orEmpty()

    val chordName = if (chords.isNotEmpty()) chords.first().chordName else "로딩 중..."


    // 소리 설정
    val chordSoundUri = if (chords.isNotEmpty()) chords.first().chordSoundUri else ""

    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }


    // ✅ API 호출
    LaunchedEffect(stepId) {
        viewModel.fetchPracticeStep(stepId)
    }


    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val density = LocalDensity.current

        // 일시정지 모달
        val showPauseDialog = remember { mutableStateOf(false) }


        Scaffold(
            topBar = {
                PracticeTopBar(
                    titleText = "코드연습",
                    onPauseClick = { showPauseDialog.value = true }
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
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.85f)
                        .height(screenHeight * 0.1f)
                        .padding(top = screenHeight * 0.01f)
                        .background(Brown20, shape = RoundedCornerShape(screenHeight * 0.035f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "잘했어요! 이번엔 소리를 내볼까요?",
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
                                text = chordName,
                                fontSize = (screenWidth * 0.04f).value.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = TitleFont,
                                color = Gray90,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            IconButton(
                                onClick = {
                                    val chordSoundUri = viewModel.stepData.value?.chords?.firstOrNull()?.chordSoundUri
                                    if (!chordSoundUri.isNullOrBlank()) {
                                        try {
                                            val uri = Uri.parse(chordSoundUri) // ✅ 여기 추가
                                            mediaPlayer.reset()
                                            mediaPlayer.setDataSource(context, uri) // ✅ context 기반으로 설정
                                            mediaPlayer.prepare()
                                            mediaPlayer.start()
                                        } catch (e: Exception) {
                                            Log.e("ChordPress", "사운드 재생 실패: ${e.message}")
                                        }
                                    } else {
                                        Log.w("ChordPress", "chordSoundUri가 null 또는 비어 있음")
                                    }
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


                        // 이미지 가져오기
                        val chordImageResId = ChordImageMap.getResId(chordName)

                        // 오른쪽: 코드 이미지
                        if (chordImageResId != 0) {
                            Image(
                                painter = painterResource(id = chordImageResId),
                                contentDescription = "$chordName 코드 이미지",
                                modifier = Modifier
                                    .height(screenHeight * 0.4f),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(text = "이미지 없음: $chordName")
                        }
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
                            .align(Alignment.BottomCenter)
                            .offset {
                                with(density) {
                                    IntOffset(x = (screenWidth * 0.3f).toPx().toInt(), y = 0)
                                }
                            }
                            .size(
                                width = screenWidth * 0.20f,
                                height = screenHeight * 0.20f
                            )
                            .clip(RoundedCornerShape(12.dp))
                    )

                    IconButton(
                        onClick = { navController.navigate("${Routes.PRACTICE_CHORDCHANGE}/$stepId") },
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

                // 일시정지 버튼
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
    }
}
