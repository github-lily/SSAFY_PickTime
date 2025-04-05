package com.example.picktimeapp.ui.guitarposition

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.picktimeapp.ui.camera.CameraPreview
import com.example.picktimeapp.ui.camera.CameraPermissionScreen
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.components.PracticeTopBar
import com.example.picktimeapp.ui.theme.*
import androidx.navigation.NavController


import androidx.compose.material3.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import com.example.picktimeapp.ui.components.PauseDialogCustom
import com.example.picktimeapp.ui.nav.Routes

@Composable
fun GameGuitarPositionScreen(
    navController: NavController,
    gameId : Int
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val showPauseDialog = remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                PracticeTopBar(
                    titleText = "",
                    onPauseClick = { showPauseDialog.value = true }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 상단 피드백 박스
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
                        text = "화면 안에 기타가 잘 보이도록 맞춰주세요!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray90,
                        fontWeight = FontWeight.Normal,
                        fontFamily = TitleFont,
                        fontSize = (screenWidth * 0.020f).value.sp
                    )
                }
                Spacer(modifier = Modifier.height(screenHeight * 0.05f))


                // 중간 영역(카메라)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    // 카메라 + 오버레이
                    CameraPermissionScreen {
                        // 고정된 9:16 비율로 설정
                        val aspectRatio = 16f / 9f

                        // 사용 가능한 최대 높이와 너비 계산
                        val screenWidth = LocalConfiguration.current.screenWidthDp.dp * 0.9f
                        val screenHeight = LocalConfiguration.current.screenHeightDp.dp * 0.6f

                        // 너비를 기준으로 높이 계산
                        val widthBasedHeight = screenWidth / aspectRatio

                        // 높이를 기준으로 너비 계산
                        val heightBasedWidth = screenHeight * aspectRatio

                        // 화면에 맞는 최적의 크기 선택
                        val (finalWidth, finalHeight) = if (widthBasedHeight <= screenHeight) {
                            // 너비 기준으로 맞추기
                            Pair(screenWidth, widthBasedHeight)
                        } else {
                            // 높이 기준으로 맞추기
                            Pair(heightBasedWidth, screenHeight)
                        }

                        Box(
                            modifier = Modifier
                                .width(finalWidth)
                                .height(finalHeight)
                                .clip(RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            CameraPreview(modifier = Modifier .matchParentSize())

                            Image(
                                painter = painterResource(id = R.drawable.guitar_overlay),
                                contentDescription = "기타 위치 안내 이미지",
                                contentScale = ContentScale.FillBounds, // 카메라에 맞게 사진 강제로 채우기
                                modifier = Modifier
                                    .matchParentSize()
                                    .align(Alignment.Center)
                            )




                        }
                    }
                }

                // 다음 버튼
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = screenWidth * 0.03f, bottom = screenHeight * 0.03f)
                ) {
                    IconButton(
                        onClick = { navController.navigate("game/${gameId}") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(y = (-screenHeight * 0.01f))
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
                            navController.navigate(Routes.GAME) {
                                popUpTo(Routes.GAME) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }

}

