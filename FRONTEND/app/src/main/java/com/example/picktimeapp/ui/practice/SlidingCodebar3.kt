package com.example.picktimeapp.ui.practice

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.picktimeapp.network.ChordMeasure
import kotlinx.coroutines.launch

@Composable
fun SlidingCodeBar3(
    screenWidth: Dp,
    currentIndex: Int,  // 현재 인덱스 (코드 표시용)
    elapsedTime: Float, // 경과 시간, 음악이 재생된 시간 (애니메이션용)
    totalDuration: Float, // 전체 음악 길이
    chords: List<String>,
    modifier: Modifier = Modifier
) {
    val blockWidth = screenWidth * 0.13f
    val blockHeight = screenWidth * 0.1f
    val density = LocalDensity.current
    val blockWidthPx = with(density) { blockWidth.toPx() }

    val durationPerNoteSec = remember(chords, totalDuration) {
        totalDuration / chords.size
    } //모든 코드들 펼치기

    // 시간 기반으로 오프셋 계산, 현재 코드가 얼마나 진행됐는지
    val progressInCurrentBlock = (elapsedTime % durationPerNoteSec) / durationPerNoteSec
    val offsetX = -progressInCurrentBlock * blockWidthPx // 왼쪽으로 이동하는 것처럼 하기

    val visibleNotes = if (currentIndex < chords.size) {
        chords.drop(currentIndex).take(6)
    } else {
        emptyList()
    }

    Box(modifier = modifier.height(blockHeight).clipToBounds()) {
        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .padding(start = screenWidth * 0.12f),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            visibleNotes.forEachIndexed() { index, chord ->
                val alpha = remember(currentIndex) {
                    Animatable(if (index === 5) 0f else 1f)
                }

                // 처음 할 때 효과 주기
                val scale = remember { Animatable(1f) }
                val backgroundColor = remember { Animatable(Color(0xFFE3CBA5)) }

                LaunchedEffect(currentIndex, index) {
                    if (index == 5) {
                        alpha.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = 300, // 부드러운 페이드인
                                easing = LinearEasing
                            )
                        )
                    }

                    if (index == 0) {
                        scale.snapTo(1.3f)
                        backgroundColor.snapTo(Color.White)

                        // 동시에 실행
                        launch {
                            scale.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 300)
                            )
                        }
                        launch {
                            backgroundColor.animateTo(
                                targetValue = Color(0xFFFFF4DE),
                                animationSpec = tween(durationMillis = 300)
                            )
                        }
                    }
                }



                Box(
                    modifier = Modifier
                        .size(blockWidth, blockHeight)
                        .graphicsLayer {
                            this.alpha = alpha.value
                            this.scaleX = scale.value
                            this.scaleY = scale.value
                        }
                        .background(
                            backgroundColor.value,
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                        Text(
                            text = chord,
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                            color = Color.Black
                        )
                }
            }
        }
    }
}

