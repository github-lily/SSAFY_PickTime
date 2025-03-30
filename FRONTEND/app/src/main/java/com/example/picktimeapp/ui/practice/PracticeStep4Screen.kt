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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picktimeapp.network.ChordBlock
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.picktimeapp.ui.components.PracticeTopBar
import com.example.picktimeapp.ui.nav.Routes

import androidx.navigation.NavController
import com.example.picktimeapp.ui.components.PauseDialogCustom
import com.example.picktimeapp.ui.components.PracticeTopBar



@Composable
fun PracticeStep4Screen(
    stepId: Int,
    navController: NavController,
    viewModel: PracticeStepViewModel = hiltViewModel()
) {


    val song = viewModel.songData.value
    val error = viewModel.errorMessage.value

    // 코드 노트를 한 줄로 펴기
    val chordBlocks: List<ChordBlock> = song?.chordProgression
        ?.flatMap { it.chordBlocks } ?: emptyList()

    // 파라미터로 stepId 전달해주기
    LaunchedEffect(key1 = stepId) {
        viewModel.fetchStepSong(stepId)
    }

    //lerp
    fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return (1 - fraction) * start + fraction * stop
    }


    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // 일시정지
        val showPauseDialog = remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                PracticeTopBar(
                    titleText = "코드연습",
//                    iconSize = (screenWidth * 0.05f).coerceAtLeast(32.dp),
                    onPauseClick = {
                        showPauseDialog.value = true
                    }
                )
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

                // 노래 정보(API 테스트용)
//                if (song != null) {
//                    Text(
//                        text = "${song.title} by ${song.artist}",
//                        modifier = Modifier.align(Alignment.TopCenter),
//                        color = Color.Black
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//
//                if (error != null) {
//                    Text(
//                        text = error,
//                        modifier = Modifier.align(Alignment.TopCenter),
//                        color = Color.Red
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                }


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


                // 🎸 코드 노트 반복 출력 (프레임 기반 시간 동기화 방식)
                Box(modifier = Modifier.fillMaxSize()) {
//                    val context = LocalContext.current
//                    val mediaPlayer = remember {
//                        MediaPlayer().apply {
//                            setDataSource(context, Uri.parse(song?.songUri ?: ""))
//                            prepare()
//                        }
//                    }

                    var musicTime by remember { mutableStateOf(0L) }
                    LaunchedEffect(Unit) {
//                        mediaPlayer.start()
                        while (true) {
//                            musicTime = mediaPlayer.currentPosition.toLong()
                            musicTime += 16L
                            delay(16L) // 60fps
                        }
                    }

                    val judgeLineX = screenWidth.value * 0.2f // 🎯 정타선 위치
                    val startX = screenWidth.value + (screenWidth.value * 0.1f) // 시작 위치

                    val bpm = song?.bpm ?: 120
                    val beatDurationMs = 60_000 / bpm
                    val screenTravelBeats = song?.timeSignature?.split("/")?.getOrNull(0)?.toFloatOrNull() ?: 4f

                    // 모든 코드 블록을 실제 등장 순서대로 정리
                    val allChordBlocks = remember(song) {
                        buildList {
                            var currentBeat = 0
                            song?.chordProgression?.forEach { measure ->
                                measure.chordBlocks.forEach { chord ->
                                    if (chord.name != "X") {
                                        val hitTime = currentBeat * beatDurationMs
                                        val appearTime = hitTime - (screenTravelBeats * beatDurationMs).toLong()
                                        add(Triple(chord.name, appearTime, hitTime))
                                    }
                                    currentBeat += chord.durationBeats
                                }
                            }
                        }
                    }

                    allChordBlocks.forEach { (chordName, appearTime, hitTime) ->
                        val progress = ((musicTime - appearTime) / (hitTime - appearTime).toFloat()).coerceIn(0f, 1f)
                        val currentX = lerp(startX, judgeLineX, progress)

                        if (musicTime in appearTime.toLong()..hitTime.toLong()) {
                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            currentX.toInt(),
                                            (screenHeight * 0.3f).value.toInt()
                                        )
                                    }
                                    .size(
                                        width = screenWidth * 0.07f,
                                        height = screenHeight * 0.40f
                                    )
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Brown20),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chordName,
                                    color = Color.White,
                                    fontSize = (screenWidth * 0.040f).value.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }



                // 카메라 프리뷰
                CameraPreview(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset {
                            with(density) {
                                IntOffset(x = (screenWidth * 0.3f).toPx().toInt(), y = 0)
                            }
                        }
                        .padding(40.dp)
                        .size(
                            width = screenWidth * 0.20f,
                            height = screenHeight * 0.20f
                        )
                        .clip(RoundedCornerShape(12.dp))
                )


                // 일시정지 모달
                if (showPauseDialog.value) {
                    PauseDialogCustom(
                        screenWidth = screenWidth,
                        onDismiss = { showPauseDialog.value = false },
                        onExit = {
                            showPauseDialog.value = false
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(Routes.WELCOME) { inclusive = true }
                            }
                        }
                    )
                }

            }
        }
    }
}
