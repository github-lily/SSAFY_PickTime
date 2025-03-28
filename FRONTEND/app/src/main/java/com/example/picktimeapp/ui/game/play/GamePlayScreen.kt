package com.example.picktimeapp.ui.game.play

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.theme.Brown40
import com.example.picktimeapp.ui.theme.Brown60
import com.example.picktimeapp.ui.theme.Brown80
import com.example.picktimeapp.ui.theme.Gray50

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
fun PauseDialogCustom(
    screenWidth: Dp,
    onDismiss: () -> Unit,
    onExit: () -> Unit
) {
        Dialog(onDismissRequest = { onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {

        BoxWithConstraints(
            modifier = Modifier
                .width(screenWidth * 0.45f)
                .padding(horizontal = screenWidth * 0.02f) // 좌우 여백
        ) {
            val maxWidthDp = maxWidth
            val cornerRadius = maxWidthDp * 0.04f
            val titleFontSize = (maxWidthDp * 0.06f).value.sp
            val subFontSize = (maxWidthDp * 0.035f).value.sp
            val buttonHeight = maxWidthDp * 0.12f
            val buttonFontSize = (maxWidthDp * 0.04f).value.sp

            Box(
                modifier = Modifier
                    .width(maxWidthDp * 1.0f) // 진짜 너비 조절은 여기!
                    .wrapContentHeight()
                    .background(Color.White, shape = RoundedCornerShape(cornerRadius))
                    .padding(maxWidthDp * 0.06f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 타이틀
                    Text(
                        text = buildAnnotatedString {
                            append("정말로 ")
                            withStyle(
                                style = SpanStyle(
                                    color = Brown40,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("종료")
                            }
                            append("하시겠습니까?")
                        },
                        fontSize = titleFontSize,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(maxWidthDp * 0.01f))

                    // 서브 텍스트
                    Text(
                        text = "지금까지의 내용들은 저장되지 않습니다.",
                        fontSize = subFontSize,
                        color = Gray50
                    )

                    Spacer(modifier = Modifier.height(maxWidthDp * 0.08f))

                    // 버튼 영역
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(maxWidthDp * 0.04f)
                    ) {
                        // 이어하기 버튼
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE6D3B3)
                            ),
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                        ) {
                            Text(
                                text = "이어하기",
                                color = Brown80,
                                fontSize = buttonFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 종료하기 버튼
                        Button(
                            onClick = onExit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Brown40
                            ),
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                        ) {
                            Text(
                                text = "종료하기",
                                color = Color.White,
                                fontSize = buttonFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
