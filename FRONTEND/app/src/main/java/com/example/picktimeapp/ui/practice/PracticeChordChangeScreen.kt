package com.example.picktimeapp.ui.practice


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.components.PauseDialogCustom
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.example.picktimeapp.audio.AudioComm
import com.example.picktimeapp.controller.AudioCaptureController
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
import kotlinx.coroutines.launch


@Composable
fun PracticeChordChangeScreen(
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

    // 점수 계산 중 모달 상태
    var isCalculatingScore by remember { mutableStateOf(false) }

    // 오디오 이벤트 등록
    LaunchedEffect(Unit) {
        chordCheckViewModel.getCameraAnalyzer()?.let { analyzer ->
            AudioCaptureController(analyzer, chordCheckViewModel)
            AudioComm.startAudioProcessing()
            AudioComm.audioCaptureOn()
        }
    }


    // 일시정시 버튼을 눌렀을 때
    val isPaused = remember { mutableStateOf(false) }

    // 현재 멈춤을 눌렀는지 안눌렀는지 확인할 변수
    val showPauseDialog = remember { mutableStateOf(false) }

    // 게임 끝났을 때
    var hasSentResult by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var stepThreeScore by remember { mutableStateOf(0) }


    // 다음 스테이지 이동
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val stepViewModel: PracticeStepViewModel = hiltViewModel()


    // 현재 코드
    var currentChord by remember { mutableStateOf<String?>(null) }

    // 맞힌 노트 개수 계산
    var correctCount by remember { mutableStateOf(0) }

    // 점수 증가 중복 방지
    var lastScoredChord by remember { mutableStateOf<String?>(null) }


    // 코드 정답여부 확인
    LaunchedEffect(Unit) {
        snapshotFlow { Pair(chordCheckViewModel.isCorrect, currentChord) }
            .collect { (correct, chord) ->
                if (chord != null) {
                    if (correct && chord != lastScoredChord) {
                        correctCount++
                        lastScoredChord = chord
                        Log.d("Practice", "✅ 정답 코드 = $chord, 누적 점수 = $correctCount")
                    } else if (!correct) {
                        Log.d("Practice", "❌ 오답 또는 미완: 코드 = $chord, 현재 점수 = $correctCount")
                    }
                }
            }
    }


    LaunchedEffect(stepId) {
        viewModel.fetchPracticeStep(stepId)
        Log.d("PracticeChordChangeScreen", "✅ 현재 전달된 stepId: $stepId")
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

        // 악보코드들 일단 싹 다 불러오기
        val allChords = remember(stepData) {
            stepData?.chords?.map { it.chordName } ?: emptyList()
        }

        // 경과 시간 상태 추가
        var elapsedTime by remember { mutableStateOf(0f) }

        // 일시정지한 시간 상태 감지용
        var pauseOffset by remember { mutableStateOf(0L) }

        // 마지막 일시정지 시작 시간 감지용
        var pauseStartTime by remember { mutableStateOf<Long?>(null) }

        // 코드 몇 초동안 보여야하는지 계산하기
        val durationPerNoteSec = 2f

        // 현재 코드 몇 번째인지
        val currentChordIndex = remember { mutableStateOf(0) }

        // 맞힌 노트 개수 계산
        var correctCount by remember { mutableStateOf(0) }

        val repeatCount = 3
        val repeatedChords = remember(allChords) {
            List(repeatCount) { allChords }.flatten()       // [A, B, A, B, A, B]
        }
        val totalDuration = repeatedChords.size * 2f

        LaunchedEffect(allChords) {
            Log.d("PracticeChordChange", "🎸 allChords = $allChords")

            if (allChords.isEmpty()) {
                Log.e("PracticeChordChange", "⚠️ allChords가 비어 있음. 반복 생성 스킵")
                return@LaunchedEffect
            }

        }

        // 만약 일시정시 버튼을 눌렀다면
        LaunchedEffect(isPaused.value) {
            if (isPaused.value) {
                pauseStartTime = System.currentTimeMillis()
            } else {

                pauseStartTime?.let {
                    // 멈춰 있던 시간 누적
                    pauseOffset += System.currentTimeMillis() - it
                }
                pauseStartTime = null

                try {
                    Log.d("연습모드", "▶️ 이어재생됨")
                } catch (e: Exception) {
                    Log.e("연습모드", "❌ 이어재생 실패: ${e.message}")
                }
            }
        }

        // 시간 계산해서 현재 코드 몇 번쨰인지 업데이트 및 경과 시간 추적
        LaunchedEffect(repeatedChords, isStarted.value) {
            if (!isStarted.value || repeatedChords.isEmpty()) return@LaunchedEffect

            val startTime = System.currentTimeMillis()

            while (currentChordIndex.value <= repeatedChords.lastIndex) {

                // 만약 일시정지 버튼을 누르지 않은 상태라면 진행시킨다.
                if (!isPaused.value) {
                    val now = System.currentTimeMillis()
                    val current = (now - startTime - pauseOffset) / 1000f // pause 시간 빼기!!
                    elapsedTime = current

                    val newIndex = (current / durationPerNoteSec).toInt()

                    if (newIndex < repeatedChords.size && newIndex != currentChordIndex.value) {
                        currentChordIndex.value = newIndex

                        val newChord = repeatedChords[newIndex]

                            if (newChord != "X") {
                            chordCheckViewModel.setChordName(newChord)  // ✅ 코드 설정
                            currentChord = newChord
                            Log.d("PracticeChordChange", "🧠 코드 전달됨: $newChord")
                                Log.d("PracticeMusicScreen", "🎯 코드 바뀜! index=$newIndex, 코드=$currentChord → false 추가됨")
                                Log.d("PracticeMusicScreen", "🧠 AI에게 요청할 코드: $currentChord")
                            }
                        }
                    // 마지막 코드까지 도달했을 때 종료
                    if (!hasSentResult  && elapsedTime >= totalDuration) {
                        hasSentResult = true
                        isCalculatingScore = true

                        delay(1000) // ✅ 판별 반영을 기다리는 시간 (1초)



                        val totalCount = allChords.count { it != "X" } // 실제 연습한 코드 개수
                        val rawScore = if (totalCount > 0) ((correctCount.toFloat() / totalCount) * 100).toInt() else 0

                        stepThreeScore = when (rawScore) {
                            in 0..30 -> 1
                            in 31..70 -> 2
                            in 71..100 -> 3
                            else -> 0
                        }


                        Log.d("GamePlayScreen", "정답 개수 = $correctCount / 전체 = $totalCount")
                        Log.d("GamePlayScreen", "점수 계산 결과 → rawScore = $rawScore, 점수 = $stepThreeScore")

                        isCalculatingScore = false
                        showScoreDialog = true
                        Log.d("PracticePlayScreen", "🎯 연습모드3 끝났습니다. 점수 = $stepThreeScore")

                        viewModel.sendPracticeFourResult(stepId, stepThreeScore,
                            onSuccess = {
                                Log.d("PracticeStep3", "✅ 결과 전송 완료 - $stepThreeScore")
                                showScoreDialog = true
                            },
                            onError = { errorMsg ->
                                Log.e("PracticeStep3", "❌ 결과 전송 중 오류 발생: $errorMsg")
                            }
                        )
                        break
                    }
                }
                delay(16)
            }
        }



        Scaffold (
            topBar = {
                PracticeTopBar(
                    titleText = "박자에 맞춰 연주하기",
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
                        text = "잘했어요! 이번엔 박자에 맞게 연주해볼까요?",
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

                    // 일단 코드 박스는 준비가 끝나면 하자
                        SlidingCodeBar3(
                            screenWidth = screenWidth,
                            currentIndex = currentChordIndex.value,
                            elapsedTime = elapsedTime,
                            totalDuration = totalDuration, // 일단 4초로 정해
                            chords = repeatedChords, // ex: ["G", "D"]
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(top = screenHeight * 0.14f)
                                .zIndex(2f)
                                .graphicsLayer {
                                    clip = false
                                }
                        )

                }

                // 하단 쪽
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = screenWidth * 0.03f),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.End
                ) {

               Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    CameraPreview(
                        chordCheckViewModel = chordCheckViewModel,
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
                        onClick = {
                            coroutineScope.launch {
                                stepViewModel.fetchPracticeStep(stepId + 1)
                                delay(200) // API 응답 시간 고려

                                // 다음 stepType에 따라 페이지 이동 분기
                                val nextStepType = stepViewModel.stepData.value?.stepType
                                when (nextStepType) {
                                    1 -> navController.navigate("practicechordinfo/${stepId + 1}") {
                                        popUpTo("practicechordinfo/${stepId + 1}") {
                                            inclusive = true
                                        }
                                    }

                                    2 -> navController.navigate("practicechordchange/${stepId + 1}") {
                                        popUpTo("practicechordchange/${stepId + 1}") {
                                            inclusive = true
                                        }
                                    }

                                    3 -> navController.navigate("practicemusic/${stepId + 1}") {
                                        popUpTo("practicemusic/${stepId + 1}") { inclusive = true }
                                    }

                                    // stepType을 못불러오면 커리큘럼 페이지로 이동
                                    else -> {
                                        navController.navigate("practicelist") {
                                            popUpTo("practicelist") { inclusive = true }
                                        }
                                        Log.e("StepNav", "알 수 없는 stepType: $nextStepType")
                                    }
                                }
                            }
                        },
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


                // 팝업창 띄우기 ------------------------------
                if (showPauseDialog.value) {
                    PauseDialogCustom(
                        screenWidth = screenWidth,
                        // 이어하기
                        onDismiss = {
                            showPauseDialog.value = false
                            isPaused.value = false },
                        // 종료하기 -> 스텝 다음으로 넘어가야함
                        onExit = {
                            showPauseDialog.value = false
                            navController.navigate(Routes.PRACTICE_LIST) {
                                popUpTo(Routes.PRACTICE_LIST) { inclusive = true }
                            }
                        }
                    )
                }

                // 점수창 띄우기 ------------------------------
                if (showScoreDialog) {
                    NextScoreDialogCustom(
                        score = stepThreeScore,
                        screenWidth = screenWidth,
                        onDismiss = {
                            showScoreDialog = false
                            navController.navigate("practicechordchange/$stepId") {
                                popUpTo("practicechordchange/$stepId") { inclusive = true } // 현재 화면 제거 후 재시작하겠다.
                            }
                        },
                        onExit = {
                            showScoreDialog = false
                            coroutineScope.launch {
                                stepViewModel.fetchPracticeStep(stepId + 1)
                                delay(200) // API 응답 시간 고려

                                // 다음 stepType에 따라 페이지 이동 분기
                                val nextStepType = stepViewModel.stepData.value?.stepType
                                when (nextStepType) {
                                    1 -> navController.navigate("practicechordinfo/${stepId + 1}") {
                                        popUpTo("practicechordinfo/${stepId + 1}") {
                                            inclusive = true
                                        }
                                    }

                                    2 -> navController.navigate("practicechordchange/${stepId + 1}") {
                                        popUpTo("practicechordchange/${stepId + 1}") {
                                            inclusive = true
                                        }
                                    }

                                    3 -> navController.navigate("practicemusic/${stepId + 1}") {
                                        popUpTo("practicemusic/${stepId + 1}") { inclusive = true }
                                    }
                                    
                                    // stepType을 못불러오면 커리큘럼 페이지로 이동
                                    else -> {
                                        navController.navigate("practicelist") {
                                            popUpTo("practicelist") { inclusive = true }
                                        }
                                        Log.e("StepNav", "알 수 없는 stepType: $nextStepType")
                                    }
                                }
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

        // 점수 계산 중 표시 모달
        if (isCalculatingScore) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)), // 반투명 배경
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.girini_score),
                        contentDescription = "점수계산중",
                        modifier = Modifier
                            .size(600.dp) // 크기는 원하는 대로 조절
                    )
                }
        }
//        if (isCalculatingScore) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.5f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Image(
//                        painter = painterResource(id = R.drawable.girini_score),
//                        contentDescription = "점수 계산 중",
//                        modifier = Modifier.size(150.dp)
//                    )
//
//                }
//            }
//        }

    }
}
