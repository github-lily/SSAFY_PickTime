package com.example.picktimeapp.ui.practice


import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.components.PauseDialogCustom
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.picktimeapp.ui.camera.CameraPreview
import com.example.picktimeapp.ui.components.NextScoreDialogCustom
import com.example.picktimeapp.ui.components.PracticeTopBar
import com.example.picktimeapp.ui.components.ScoreDialogCustom
import com.example.picktimeapp.ui.nav.Routes
import com.example.picktimeapp.ui.theme.Brown20
import com.example.picktimeapp.ui.theme.Gray90
import com.example.picktimeapp.ui.theme.TitleFont
import com.example.picktimeapp.util.ChordCheckViewModel
import kotlinx.coroutines.delay


@Composable
fun PracticeMusicScreen(
    stepId: Int,
    navController: NavController,
    viewModel: PracticeStepViewModel = hiltViewModel(),
    chordCheckViewModel: ChordCheckViewModel = hiltViewModel()
) {

    // 겟 레디하기
    val isStarted = remember { mutableStateOf(false) }
    var countdownNumber by remember { mutableStateOf<Int?>(3) }
    LaunchedEffect(Unit) {
        for (i in 3 downTo 1) {
            countdownNumber = i
            delay(1000)
        }
        countdownNumber = null // 끝나면 숫자 숨겨
        isStarted.value = true // 끝나면 시작하자
    }

    // 노래 불러오기 위해
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }



    // 일시정시 버튼을 눌렀을 때
    val isPaused = remember { mutableStateOf(false) }

    // 현재 멈춤을 눌렀는지 안눌렀는지 확인할 변수
    val showPauseDialog = remember { mutableStateOf(false) }

    // 게임 끝났을 때
    var hasSentResult by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var stepFourScore by remember { mutableStateOf(0) }

    // 맞힌 노트 개수 계산
    var correctCount by remember { mutableStateOf(0) }

    // 현재 코드
    var currentChord by remember { mutableStateOf<String?>(null) }

    // 점수 증가 중복 방지
    var lastScoredChord by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(stepId) {
        viewModel.fetchPracticeStep(stepId)
        Log.d("PracticeChordChangeScreen", "✅ 현재 전달된 stepId: $stepId")

    }

    // 코드 정답여부 확인
    LaunchedEffect(Unit) {
        snapshotFlow { Pair(chordCheckViewModel.isCorrect, currentChord) }
            .collect { (correct, chord) ->
                if (correct && chord != null && chord != lastScoredChord) {
                    correctCount++
                    lastScoredChord = chord
                    Log.d("Practice", "✅ 정답 코드 = $chord, 점수 = $correctCount")
                }
            }
    }


    BoxWithConstraints (
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                clip = false
            }
            .padding(bottom = 20.dp)
    ){
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // 데이터 불러오기
        val stepData = viewModel.stepData.value
        val song = stepData?.song

        // 악보코드들 일단 싹 다 불러오기
        val allChords = remember(song) {
            song?.chordProgression?.flatMap { it.chordBlocks } ?: emptyList()
        }
        

        DisposableEffect(Unit) {
            onDispose {
                Log.d("GamePlay", "🧹 mediaPlayer 정리")
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            }
        }

        // 경과 시간 상태 추가
        var elapsedTime by remember { mutableStateOf(0f) }

        // 일시정지한 시간 상태 감지용
        var pauseOffset by remember { mutableStateOf(0L) }

        // 마지막 일시정지 시작 시간 감지용
        var pauseStartTime by remember { mutableStateOf<Long?>(null) }

        // 코드 몇 초동안 보여야하는지 계산하기
        val durationPerNoteSec = remember(song?.durationSec) {
            val totalNotes = allChords.size
            (song?.durationSec?.toFloat() ?: 1f) / totalNotes
        }

        // 현재 코드 몇 번째인지
        val currentChordIndex = remember { mutableStateOf(0) }

        //비교 결과를 저장할 구조
        val correctnessList = remember { mutableStateListOf<Boolean>() }

        // 노래 재생하도록 하기
        LaunchedEffect(song?.songUri, isStarted.value) {
            if (song?.songUri != null && isStarted.value) {
                try {
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.reset() // ⭐ reset으로 초기화 먼저!
                        mediaPlayer.setDataSource(context, Uri.parse(song.songUri))
                        mediaPlayer.prepare()
                        mediaPlayer.start()
                        Log.d("GamePlay", "🎵 자동 재생 시작됨")
                    }
                } catch (e: Exception) {
                    Log.e("GamePlay", "❌ 자동 재생 실패: ${e.message}")
                }
            }
        }


        // 만약 일시정시 버튼을 눌렀다면
        LaunchedEffect(isPaused.value) {
            if (isPaused.value) {
                pauseStartTime = System.currentTimeMillis()
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    Log.d("연습모드", "⏸ 일시정지됨")
                }
            } else {

                pauseStartTime?.let {
                    // 멈춰 있던 시간 누적
                    pauseOffset += System.currentTimeMillis() - it
                }
                pauseStartTime = null

                try {
                    mediaPlayer.start()
                    Log.d("연습모드", "▶️ 이어재생됨")
                } catch (e: Exception) {
                    Log.e("연습모드", "❌ 이어재생 실패: ${e.message}")
                }
            }
        }

        // 시간 계산해서 현재 코드 몇 번쨰인지 업데이트 및 경과 시간 추적
        LaunchedEffect(allChords, song?.durationSec, isStarted.value) {
            if (!isStarted.value) return@LaunchedEffect

            val startTime = System.currentTimeMillis()
            val totalChords = allChords.size

            while (currentChordIndex.value <= allChords.size -1) {

                // 만약 일시정지 버튼을 누르지 않은 상태라면 진행시킨다.
                if (!isPaused.value) {
                    val now = System.currentTimeMillis()
                    val current = (now - startTime - pauseOffset) / 1000f // pause 시간 빼기!!
                    elapsedTime = current
                    val newIndex = (current / durationPerNoteSec).toInt()

                    if (newIndex < totalChords) {
                        if (newIndex != currentChordIndex.value) {
                            currentChordIndex.value = newIndex
                            val newChord = allChords[newIndex]
                            if (newChord != "X") {
                                chordCheckViewModel.setChordName(newChord)// 코드 네임 바꿈
                                currentChord = newChord
                                Log.d("PracticeMusicScreen", "🎯 코드 바뀜! index=$newIndex, 코드=$currentChord → false 추가됨")
                                Log.d("PracticeMusicScreen", "🧠 AI에게 요청할 코드: $currentChord")
                            }
                        }
                    } else {
                        break
                    }
                }

                kotlinx.coroutines.delay(16) // 약 60fps
            }
            // 마지막 코드까지 도달했을 때 종료
            if (!hasSentResult  && totalChords > 0 ) {
                hasSentResult = true

                val totalCount = allChords.count { it != "X" } // 실제 연습한 코드 개수
                val rawScore = if (totalCount > 0) ((correctCount.toFloat() / totalCount) * 100).toInt() else 0
                stepFourScore = when (rawScore) {
                    in 0..30 -> 1
                    in 31..70 -> 2
                    in 71..100 -> 3
                    else -> 0
                }

                showScoreDialog = true
                Log.d("PracticePlayScreen", "🎯 연습모드 끝났습니다. 점수 = $stepFourScore")
                viewModel.sendPracticeFourResult(stepId, stepFourScore,
                    onSuccess = {
                        Log.d("PracticeStep", "✅ 결과 전송 완료 - $stepFourScore")
                        showScoreDialog = true
                    },
                    onError = { errorMsg ->
                        Log.e("PracticeStep", "❌ 결과 전송 중 오류 발생: $errorMsg")
                    }
                )
            }
        }

        Scaffold (
            topBar = {
                PracticeTopBar(
                    titleText = "노래에 맞춰 연주하기",
                    onPauseClick = {
                        showPauseDialog.value = true
                        isPaused.value = true
                    }
                )
            }
        ) { innerPadding ->
            Column (modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .graphicsLayer {
                    clip = false
                }
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
                        text = "잘했어요! 이번엔 노래에 맞게 연주해볼까요?",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray90,
                        fontWeight = FontWeight.Normal,
                        fontFamily = TitleFont,
                        fontSize = (screenWidth * 0.020f).value.sp
                    )
                }

                // 코드 애니메이션 쪽
                Box (modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .graphicsLayer {
                        clip = false  // overflow 허용!
                    }
                ){
                    Spacer(Modifier.height(screenHeight * 0.05f))

                    GuitarImage2(
                        imageRes = R.drawable.guitar_neck,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        modifier = Modifier.zIndex(1f)
                    )

                    if (song != null) {
                        SlidingCodeBar2(
                            screenWidth = screenWidth,
                            currentIndex = currentChordIndex.value,
                            elapsedTime = elapsedTime,
                            totalDuration = song.durationSec.toFloat(),
                            chordProgression = song.chordProgression,
                            organizedChords = song.organizedChords ?: emptyList(),
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(top = screenHeight * 0.14f)
                                .zIndex(2f)
                                .graphicsLayer {
                                    clip = false
                                }
                        )
                    }
                }

                // 하단 쪽
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = screenWidth * 0.03f, vertical = screenHeight * 0.03f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // 카메라 나오는 쪽
                    Box(
                        modifier = Modifier
                            .padding(start = screenWidth * 0.02f, bottom = screenWidth * 0.015f, end = screenWidth * 0.02f)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            CameraPreview(
                                chordCheckViewModel = chordCheckViewModel,
                                modifier = Modifier
                                    .size(
                                        width = screenWidth * 0.20f,
                                        height = screenHeight * 0.20f
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .zIndex(999f)
                            )
                        }
                    }
                }


                // 팝업창 띄우기
                if (showPauseDialog.value) {
                    PauseDialogCustom(
                        screenWidth = screenWidth,
                        // 이어하기
                        onDismiss = {
                            showPauseDialog.value = false
                            isPaused.value = false },
                        // 종료하기
                        onExit = {
                            showPauseDialog.value = false
                            navController.navigate("practicelist")
                        }
                    )
                }
                if (showScoreDialog) {
                    NextScoreDialogCustom(
                        score = stepFourScore,
                        screenWidth = screenWidth,
                        onDismiss = {
                            showScoreDialog = false
                            navController.navigate("practice/$stepId") {
                                popUpTo("practice/$stepId") { inclusive = true } // 현재 화면 제거 후 재시작하겠다.
                            }
                        },
                        onExit = {
                            showScoreDialog = false
                            navController.navigate(Routes.PRACTICE_LIST) {
                                popUpTo(Routes.PRACTICE_LIST) { inclusive = true } // 현재 화면 제거
                            }
                        }
                    )
                }
            }
        }

        // 준비 UI 띄우기 ------------------------------
        if (countdownNumber != null) {
            val imageRes = when (countdownNumber) {
                3 -> R.drawable.girini_count_3
                2 -> R.drawable.girini_count_2
                1 -> R.drawable.girini_count_1
                else -> null
            }

            imageRes?.let {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)), // 반투명 배경
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = it),
                        contentDescription = "카운트다운",
                        modifier = Modifier
                            .size(600.dp) // 크기는 원하는 대로 조절
                    )
                }
            }
        }

    }
}

@Composable
fun GuitarImage2(imageRes: Int, screenWidth: Dp, screenHeight: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = "Guitar Neck",
        modifier = Modifier
            .padding(top = screenHeight * 0.07f)
            .offset(x = -screenWidth * 0.1f)   // 왼쪽으로 조금 이동
            .height(screenHeight * 0.55f)
            .scale(1.8f)
    )
}
