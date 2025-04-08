package com.example.picktimeapp.ui.game.play

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.components.PauseDialogCustom
import com.example.picktimeapp.ui.theme.Brown40
import com.example.picktimeapp.ui.theme.Brown80
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import com.example.picktimeapp.ui.camera.CameraPreview
import com.example.picktimeapp.ui.components.ScoreDialogCustom
import com.example.picktimeapp.ui.nav.Routes
import com.example.picktimeapp.util.ChordCheckViewModel

@Composable
fun GamePlayScreen(
    navController: NavController,
    songId: Int,
    chordCheckViewModel: ChordCheckViewModel = hiltViewModel()
) {
    val viewModel : GamePlayViewModel = hiltViewModel()

    // 노래 불러오기 위해
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }

    // 일시정시 버튼을 눌렀을 때
    val isPaused = remember { mutableStateOf(false) }

    // 현재 멈춤을 눌렀는지 안눌렀는지 확인할 변수
    val (showPauseDialog, setShowPauseDialog) = remember { mutableStateOf(false) }

    // 게임 끝났을 때
    var hasSentResult by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    LaunchedEffect(songId) {
        viewModel.loadGamePlay(songId)
    }

    BoxWithConstraints (
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                clip = false
            }
            .padding(top = 20.dp, bottom = 20.dp)
    ){
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val density = LocalDensity.current

        // 게임 데이터 불러오기
        val gameData = viewModel.gameData.collectAsState().value
        // 모든 코드 가지고오기
        val chordProgression = gameData?.chordProgression ?: emptyList()
        // 위에 제목 띄우기
        val title = gameData?.title

        DisposableEffect(Unit) {
            onDispose {
                Log.d("GamePlay", "🧹 mediaPlayer 정리")
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            }
        }

        // 코드들 일단 싹 다 불러오기
        val allChords = remember(chordProgression) { chordProgression.flatMap { it.chordBlocks }}

        // 경과 시간 상태 추가
        var elapsedTime by remember { mutableStateOf(0f) }

        // 일시정지한 시간 상태 감지용
        var pauseOffset by remember { mutableStateOf(0L) }

        // 마지막 일시정지 시작 시간 감지용
        var pauseStartTime by remember { mutableStateOf<Long?>(null) }

        // 코드 몇 초동안 보여야하는지 계산하기
        val durationPerNoteSec = remember(chordProgression, gameData?.durationSec) {
            val totalNotes = allChords.size
            (gameData?.durationSec?.toFloat() ?: 1f) / totalNotes
        }

        // 현재 코드 몇 번째인지
        val currentChordIndex = remember { mutableStateOf(0) }

        //비교 결과를 저장할 구조
        val correctnessList = remember { mutableStateListOf<Boolean>() }

        // 노래 재생하도록 하기
        LaunchedEffect(gameData?.songUri) {
            if (gameData?.songUri != null) {
                try {
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.reset() // ⭐ reset으로 초기화 먼저!
                        mediaPlayer.setDataSource(context, Uri.parse(gameData.songUri))
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
                    Log.d("GamePlay", "⏸ 일시정지됨")
                }
            } else {

                pauseStartTime?.let {
                    // 멈춰 있던 시간 누적
                    pauseOffset += System.currentTimeMillis() - it
                }
                pauseStartTime = null

                try {
                    mediaPlayer.start()
                    Log.d("GamePlay", "▶️ 이어재생됨")
                } catch (e: Exception) {
                    Log.e("GamePlay", "❌ 이어재생 실패: ${e.message}")
                }
            }
        }

        // 시간 계산해서 현재 코드 몇 번쨰인지 업데이트 및 경과 시간 추적
        LaunchedEffect(allChords, gameData?.durationSec) {
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
                            // ✅ 일단 기본으로 false 추가해보기
                            val currentChord = allChords[newIndex]
                            if (currentChord != "X") {
                                correctnessList.add(false)
                                Log.d("GamePlayScreen", "🎯 코드 바뀜! index=$newIndex, 코드=$currentChord → false 추가됨")
                                Log.d("GamePlayScreen", "🧠 AI에게 요청할 코드: $currentChord")
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

                score = 2
                Log.d("GamePlayScreen", "🎯 게임 끝났습니다. 점수 = $score")
                viewModel.sendGameResult(songId, score) {
                    showScoreDialog = true
                }
            }
        }

        // 🔥 X를 제외한 실제 코드 2개 가져오기
        val (current, next) = getNextVisibleChords(allChords, currentChordIndex.value, 2)

        Column (modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                clip = false
            }
        ) {
            TopBar(
                onPauseClick = {
                    setShowPauseDialog(true)
                    isPaused.value = true
                },
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                title = title,
                modifier = Modifier
                    .zIndex(3f)
            )

            // 코드 애니메이션 쪽
            Box (modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .graphicsLayer {
                    clip = false  // overflow 허용!
                }
            ){
                Spacer(Modifier.height(screenHeight * 0.05f))

                GuitarImage(
                    imageRes = R.drawable.guitar_neck,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    modifier = Modifier.zIndex(1f)
                )

                if (gameData != null) {
                    SlidingCodeBar(
                        screenWidth = screenWidth,
                        currentIndex = currentChordIndex.value,
                        elapsedTime = elapsedTime,
                        totalDuration = gameData.durationSec.toFloat(),
                        chordProgression = gameData.chordProgression,
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 코드 나오는 쪽
                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    ChordSection(
                        currentChord = current,
                        nextChord = next,
                        modifier = Modifier,
//                            .padding(start = screenWidth * 0.05f),
                        imageSize = screenWidth * 0.25f,
                        screenWidth = screenWidth
                    )
                }

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
                            modifier = Modifier
                                .size(
                                    width = screenWidth * 0.20f,
                                    height = screenHeight * 0.20f
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .zIndex(999f),
                            viewModel = chordCheckViewModel
                        )
                    }
                }
            }


            // 팝업창 띄우기
            if (showPauseDialog) {
                PauseDialogCustom(
                    screenWidth = screenWidth,
                    // 이어하기
                    onDismiss = {
                        setShowPauseDialog(false)
                        isPaused.value = false },
                    // 종료하기
                    onExit = {
                        setShowPauseDialog(false)
                        navController.navigate("game")
                    }
                )
            }
            if (showScoreDialog) {
                ScoreDialogCustom(
                    score = score,
                    screenWidth = screenWidth,
                    onDismiss = {
                        showScoreDialog = false
                        navController.navigate("game/$songId") {
                            popUpTo("game/$songId") { inclusive = true } // 현재 화면 제거 후 재시작하겠다.
                        }
                    },
                    onExit = {
                        showScoreDialog = false
                        navController.navigate(Routes.GAME) {
                            popUpTo("game/$songId") { inclusive = true } // 현재 화면 제거
                        }
                    }
                )
            }
        }
    }
}

// 위에 상단 버튼
@Composable
fun TopBar(
    onPauseClick: () -> Unit,
    screenWidth: Dp,
    screenHeight: Dp,
    title: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = screenWidth * 0.02f)
    ) {
        if (!title.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = screenHeight * 0.005f)
                    .zIndex(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // 왼쪽 아이콘
                Image(
                    painter = painterResource(id = R.drawable.ic_music), // 너가 사용할 아이콘 리소스 ID
                    contentDescription = "Music Icon Left",
                    modifier = Modifier.size(screenWidth * 0.02f)
                )

                Spacer(modifier = Modifier.width(20.dp))

                // 타이틀
                Text(
                    text = title,
                    fontSize = (screenWidth * 0.02f).value.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.width(20.dp))

                // 오른쪽 아이콘
                Image(
                    painter = painterResource(id = R.drawable.ic_music), // 같은 아이콘 사용
                    contentDescription = "Music Icon Right",
                    modifier = Modifier.size(screenWidth * 0.02f)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenWidth * 0.02f)
                .align(Alignment.CenterEnd), // 정렬 기준
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
        ) {
            // 멈춤 버튼
            Image(
                painter = painterResource(id = R.drawable.pause_btn),
                contentDescription = "Pause",
                modifier = Modifier
                    .size(screenWidth * 0.03f)
                    .clickable { onPauseClick() }
            )
        }


    }
}

// 기타 넥 이미지
@Composable
fun GuitarImage(imageRes: Int, screenWidth: Dp, screenHeight: Dp,modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = "Guitar Neck",
        modifier = Modifier
            .offset(x = -screenWidth * 0.1f)   // 왼쪽으로 조금 이동
            .height(screenHeight * 0.55f)
            .scale(1.8f)
    )
}

// 코드 나오는 부분
@Composable
fun ChordSection(
    modifier: Modifier = Modifier,
    imageSize: Dp,
    screenWidth: Dp,
    currentChord: String?,
    nextChord: String?,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = screenWidth * 0.05f), // 좌우 여백
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            if(!currentChord.isNullOrBlank() && currentChord != "X") {
                // 왼쪽 코드
                ChordBlock(
                    title = currentChord,
                    imageRes = getChordImageRes(currentChord),
                    imageSize = imageSize,
                    titleColor = Brown80,
                    isHighlighted = true,
                    screenWidth = screenWidth
                )
            }
        }

        Spacer(modifier = Modifier.width(screenWidth * 0.04f))

        Box {
            if(!nextChord.isNullOrBlank() && nextChord != "X") {
                ChordBlock(
                    title = nextChord,
                    imageRes = getChordImageRes(nextChord),
                    imageSize = imageSize,
                    titleColor = Brown40,
                    screenWidth = screenWidth,
                    modifier = Modifier.alpha(0.5f)
                )
            }
        }
    }
}

@Composable
fun getChordImageRes(chord: String): Int {
    return when (chord) {
        "G" -> R.drawable.code_g
        "C" -> R.drawable.code_c
        "D" -> R.drawable.code_d
        "A" -> R.drawable.code_a
        "B" -> R.drawable.code_b
        "E" -> R.drawable.code_e
        "F" -> R.drawable.code_f

        "G7" -> R.drawable.code_g7
        "C7" -> R.drawable.code_c7
        "D7" -> R.drawable.code_d7
        "A7" -> R.drawable.code_a7
        "B7" -> R.drawable.code_b7
        "E7" -> R.drawable.code_e7
        "F7" -> R.drawable.code_f7

        "Cm" -> R.drawable.code_cm
        "Dm" -> R.drawable.code_dm
        "Em" -> R.drawable.code_em
        "Fm" -> R.drawable.code_fm
        "Gm" -> R.drawable.code_gm
        "Am" -> R.drawable.code_am
        "Bm" -> R.drawable.code_bm

        "Cm7" -> R.drawable.code_cm7
        "Dm7" -> R.drawable.code_dm7
        "Em7" -> R.drawable.code_em7
        "Fm7" -> R.drawable.code_fm7
        "Gm7" -> R.drawable.code_gm7
        "Am7" -> R.drawable.code_am7
        "Bm7" -> R.drawable.code_bm7

        "CM7" -> R.drawable.code_cm7
        "DM7" -> R.drawable.code_dbigm7
        "EM7" -> R.drawable.code_ebigm7
        "FM7" -> R.drawable.code_fbigm7
        "GM7" -> R.drawable.code_gbigm7
        "AM7" -> R.drawable.code_abigm7
        "BM7" -> R.drawable.code_bbigm7

        "F#m" -> R.drawable.code_fsm
        "C#m" -> R.drawable.code_csm
        "F#m7" -> R.drawable.code_fsm7
        "Dsus4" -> R.drawable.code_dsus4
        "Asus4" -> R.drawable.code_asus4
        "Cadd9" -> R.drawable.code_cadd9
        "Gadd9" -> R.drawable.code_gadd9
        "Fmaj7" -> R.drawable.code_fmaj7
        "Emaj7" -> R.drawable.code_emaj7
        "G#m7" -> R.drawable.code_gsm7
        "C#m7" -> R.drawable.code_csm7



        else -> R.drawable.code_c
    }
}

@Composable
fun getNextVisibleChords(allChords: List<String>, fromIndex: Int, count: Int): List<String?> {
    val result = mutableListOf<String?>()
    var index = fromIndex

    while (index < allChords.size && result.size < count) {
        val chord = allChords[index]
        if (chord != "X") {
            result.add(chord)
        }
        index++
    }

    // 부족하면 null로 채움
    while (result.size < count) {
        result.add(null)
    }

    return result
}

@Composable
fun ChordBlock(
    title: String,
    imageRes: Int,
    imageSize: Dp,
    titleColor: Color,
    isHighlighted: Boolean = false,
    screenWidth: Dp,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.Start, modifier = modifier) {
        Text(
            text = title,
            modifier = Modifier.padding(start = screenWidth * 0.02f),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = (screenWidth * 0.04f).value.sp,
                fontWeight = FontWeight.Bold
            ),
            color = titleColor
        )
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Chord Diagram: $title",
            modifier = Modifier
                .size(imageSize)
        )
    }
}