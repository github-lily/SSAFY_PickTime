package com.example.picktimeapp.ui.game.play

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun GamePlayScreen(
    navController: NavController,
    songId: Int
    ) {

    val viewModel : GamePlayViewModel = hiltViewModel()

    // 현재 멈춤을 눌렀는지 안눌렀는지 확인할 변수
    val (showPauseDialog, setShowPauseDialog) = remember { mutableStateOf(false) }

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

        println("✅ 전체 코드 개수: ${chordProgression.size}")
        println("✅ 코드 리스트: $chordProgression.chordBlocks")

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
                        durationSec = gameData.durationSec,
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
fun ChordSection(modifier: Modifier = Modifier, imageSize: Dp, screenWidth: Dp) {
    Row(
        modifier = modifier,
    ) {
        // 왼쪽 코드
        ChordBlock(
            title = "G",
            imageRes = R.drawable.code_g,
            imageSize = imageSize,
            titleColor = Brown80,
            isHighlighted = true,
            screenWidth = screenWidth
        )
        Spacer(modifier = Modifier.width(screenWidth * 0.05f))
        ChordBlock(
            title = "Am",
            imageRes = R.drawable.code_am,
            imageSize = imageSize,
            titleColor = Brown40,
            screenWidth = screenWidth,
            modifier = Modifier.alpha(0.5f)
        )
        // 여기에다가 사용자 영상 띄우기!!
    }
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
//    val fontSize = if (isHighlighted) (screenWidth * 0.04f).value.sp else (screenWidth * 0.02f).value.sp


    Column(horizontalAlignment = Alignment.Start, modifier = modifier) {
        Text(
            text = title,
            modifier = Modifier.padding(start = screenWidth * 0.02f),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = (screenWidth * 0.04f).value.sp,
//                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
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

