package com.example.picktimeapp.ui.practice
// step3 코드 전환 연습

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.camera.CameraPreview
import com.example.picktimeapp.ui.components.PauseDialogCustom
import com.example.picktimeapp.ui.components.PracticeTopBar
import com.example.picktimeapp.ui.nav.Routes
import com.example.picktimeapp.ui.theme.Brown20
import com.example.picktimeapp.ui.theme.Gray90
import com.example.picktimeapp.ui.theme.TitleFont
import kotlinx.coroutines.delay

@Composable
fun PracticeChordChangeScreen(
    stepId: Int,
    navController: NavController,
    viewModel: PracticeStepViewModel = hiltViewModel()
) {
    val stepData = viewModel.stepData.value
    val song = stepData?.song
    val error = viewModel.errorMessage.value

    val chordBlocks: List<Triple<String, Long, Long>> = remember(song) {
        buildList {
            var currentBeat = 0
            song?.chordProgression?.forEach { measure ->
                measure.chordBlocks.forEach { chord ->
                    if (chord != "X") {
                        val hitTime = (currentBeat * (60_000 / (song.bpm))).toLong()
                        val appearTime = hitTime - ((song.timeSignature.split("/")[0].toFloatOrNull() ?: 4f) * 60_000 / song.bpm).toLong()
                        add(Triple(chord, appearTime, hitTime))
                    }
                    currentBeat++
                }
            }
        }
    }

    LaunchedEffect(stepId) {
        viewModel.fetchPracticeStep(stepId)
    }

    fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return (1 - fraction) * start + fraction * stop
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val density = LocalDensity.current

        val showPauseDialog = remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                PracticeTopBar(
                    titleText = "코드연습",
                    onPauseClick = { showPauseDialog.value = true }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
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
                        text = "잘했어요! 이번엔 박자에 맞게 연주해볼까요?",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray90,
                        fontWeight = FontWeight.Normal,
                        fontFamily = TitleFont,
                        fontSize = (screenWidth * 0.020f).value.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.guitar_practice_neck),
                        contentDescription = "기타 프렛보드",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(screenHeight * 0.40f),
                        contentScale = ContentScale.FillBounds
                    )

                    Box(modifier = Modifier.fillMaxSize()) {
                        var musicTime by remember { mutableStateOf(0L) }
                        LaunchedEffect(Unit) {
                            while (true) {
                                musicTime += 16L
                                delay(16L)
                            }
                        }

                        val judgeLineX = screenWidth.value * 0.2f
                        val startX = screenWidth.value + (screenWidth.value * 0.1f)

                        chordBlocks.forEach { (chordName, appearTime, hitTime) ->
                            val progress = ((musicTime - appearTime) / (hitTime - appearTime).toFloat()).coerceIn(0f, 1f)
                            val currentX = lerp(startX, judgeLineX, progress)

                            if (musicTime in appearTime..hitTime) {
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
                }

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
                        onClick = { navController.navigate(Routes.PRACTICE_LIST) },
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
