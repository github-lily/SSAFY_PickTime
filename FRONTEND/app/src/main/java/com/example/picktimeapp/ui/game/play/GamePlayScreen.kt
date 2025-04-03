package com.example.picktimeapp.ui.game.play

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.setValue
import com.example.picktimeapp.ui.components.ScoreDialogCustom
import com.example.picktimeapp.ui.nav.Routes

@Composable
fun GamePlayScreen(
    navController: NavController,
    songId: Int
) {
    val viewModel : GamePlayViewModel = hiltViewModel()
    // 노래 불러오기 위해
    val context = LocalContext.current
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

        // 게임 데이터 불러오기
        val gameData = viewModel.gameData.collectAsState().value
        // 모든 코드 가지고오기
        val chordProgression = gameData?.chordProgression ?: emptyList()

        // 음악 재생하기
        DisposableEffect(gameData?.songUri) {
            val mediaPlayer = MediaPlayer()
            if (gameData?.songUri != null) {
                try {
                    mediaPlayer.setDataSource(context, Uri.parse(gameData.songUri))
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // 🧹 컴포저블이 dispose될 때 음악도 정리
            onDispose {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            }
        }

        // 코드들 일단 싹 다 불러오기
        val allChords = remember(chordProgression) {
            chordProgression.flatMap { it.chordBlocks }
        }

        // 경과 시간 상태 추가
        var elapsedTime by remember { mutableStateOf(0f) }

        // 코드 몇 초동안 보여야하는지 계산하기
        val durationPerNoteSec = remember(chordProgression, gameData?.durationSec) {
            val totalNotes = allChords.size
            (gameData?.durationSec?.toFloat() ?: 1f) / totalNotes
        }

        // 현재 코드 몇 번째인지
        val currentChordIndex = remember { mutableStateOf(0) }

        // 시간 계산해서 현재 코드 몇 번쨰인지 업데이트 및 경과 시간 추적
        LaunchedEffect(allChords, gameData?.durationSec) {
            val startTime = System.currentTimeMillis()
            val totalChords = allChords.size

            while (currentChordIndex.value <= allChords.size -1) {
                val current = (System.currentTimeMillis() - startTime) / 1000f
                elapsedTime = current
                val newIndex = (current / durationPerNoteSec).toInt()
//                if (newIndex != currentChordIndex.value && newIndex < allChords.size) {
//                    currentChordIndex.value = newIndex
//                }
                if (newIndex < totalChords) {
                    if (newIndex != currentChordIndex.value) {
                        currentChordIndex.value = newIndex
//                        Log.d("GamePlayScreen", "📍 현재 인덱스 = $newIndex / 전체 = $totalChords")
                    }
                } else {
                    break
                }
                kotlinx.coroutines.delay(16) // 약 60fps
            }

            // 마지막 코드까지 도달했을 때 종료
            if (!hasSentResult  && totalChords > 0 ) {
                hasSentResult = true

                score = 3
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
                onPauseClick = { setShowPauseDialog(true)},
                screenWidth = screenWidth,
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

            // 코드 & 영상 나오는 쪽
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)
            ) {
                ChordSection(
                    currentChord = current,
                    nextChord = next,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(end = screenWidth * 0.3f),
                    imageSize = screenWidth * 0.25f,
                    screenWidth = screenWidth
                )
            }

            // 팝업창 띄우기
            if (showPauseDialog) {
                PauseDialogCustom(
                    screenWidth = screenWidth,
                    onDismiss = { setShowPauseDialog(false) },
                    onExit = {
                        setShowPauseDialog(false)
                        navController.popBackStack()
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = screenWidth * 0.02f),
        horizontalArrangement = Arrangement.Absolute.Right,
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
        modifier = modifier,
    ) {
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

        Spacer(modifier = Modifier.width(screenWidth * 0.05f))

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
        // 여기에다가 사용자 영상 띄우기!!
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
