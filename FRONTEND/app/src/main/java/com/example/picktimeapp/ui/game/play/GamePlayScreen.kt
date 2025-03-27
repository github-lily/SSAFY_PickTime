package com.example.picktimeapp.ui.game.play

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.theme.Brown40
import com.example.picktimeapp.ui.theme.Brown80

@Composable
fun GamePlayScreen(navController: NavController) {

    // 현재 멈춤을 눌렀는지 안눌렀는지 확인할 변수
    val (showPauseDialog, setShowPauseDialog) = remember { mutableStateOf(false) }

    BoxWithConstraints (
        modifier = Modifier
            .fillMaxWidth()
            // 위에 여백
            .padding(top = 40.dp, bottom = 40.dp)
    ){
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        Column (modifier = Modifier.fillMaxSize()) {

            // 상단
            TopBar(
                //popBackStack 은 뒤로갈 수 있음
                onBackClick = {navController.popBackStack()},
                //클릭하면 팝업창을 보여줍니다.
                onPauseClick = { setShowPauseDialog(true)},
                screenWidth = screenWidth
            )

            GuitarImage(
                imageRes = R.drawable.guitar_neck,
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )

            Box(modifier = Modifier
                .fillMaxSize()
            ) {
                ChordSection(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(end = screenWidth * 0.3f),
                    imageSize = screenWidth * 0.25f,
                    screenWidth = screenWidth
                )
            }
            if (showPauseDialog) {
                PauseDialog(
                    onDismiss = { setShowPauseDialog(false) },
                    onExit = {
                        setShowPauseDialog(false)
                        // TODO: 나가기 처리 (예: navController.popBackStack())
                    }
                )
            }
        }
    }
}


// 위에 상단 버튼
@Composable
fun TopBar(
    onBackClick: () -> Unit,
    onPauseClick: () -> Unit,
    screenWidth: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = screenWidth * 0.02f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 뒤로 가기 버튼
        Image(
            painter = painterResource(id = R.drawable.back_icon), // ← 여기에 뒤로가기 이미지 아이콘 넣기
            contentDescription = "Back",
            modifier = Modifier
                .size(screenWidth * 0.02f)
                .clickable { onBackClick() }
        )

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
fun GuitarImage(imageRes: Int, screenWidth: Dp, screenHeight: Dp) {
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
            imageSize = imageSize * 0.8f,
            titleColor = Brown40,
            alpha = 0.6f,
            screenWidth = screenWidth
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
    alpha: Float = 1.0f,
    isHighlighted: Boolean = false,
    screenWidth: Dp
) {
    val fontSize = if (isHighlighted) (screenWidth * 0.04f).value.sp else (screenWidth * 0.03f).value.sp

    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = title,
            modifier = Modifier.padding(start = screenWidth * 0.02f),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = fontSize,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
            ),
            color = titleColor

        )
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Chord Diagram: $title",
            modifier = Modifier
                .size(imageSize)
                .alpha(alpha)
        )
    }
}

// 팝업창
@Composable
fun PauseDialog(
    onDismiss: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("일시정지") },
        text = { Text("게임을 계속하시겠습니까?") },
        confirmButton = {
            Text(
                text = "계속하기",
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onDismiss() }
            )
        },
        dismissButton = {
            Text(
                text = "종료하기",
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onExit() }
            )
        }
    )
}
