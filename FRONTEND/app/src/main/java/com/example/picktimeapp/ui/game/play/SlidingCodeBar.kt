package com.example.picktimeapp.ui.game.play
//시도 4: 하는 중
// 일단 5개만 보여주고 한 음의 시간을 계산하여 해당 시간이 지날 때마다 그 다음 음을 보여주는 슬라이드

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.picktimeapp.data.model.ChordMeasure

@Composable
fun SlidingCodeBar(
    screenWidth: Dp,
    durationSec: Int,
    chordProgression: List<ChordMeasure>,
    modifier: Modifier = Modifier
) {
    val blockWidth = screenWidth * 0.13f
    val blockHeight = screenWidth * 0.1f
    val density = LocalDensity.current
    val blockWidthPx = with(density) { blockWidth.toPx() }

    val allNotes = remember(chordProgression) {
        chordProgression.flatMap { it.chordBlocks }
    }

    val durationPerNoteSec = remember(chordProgression, durationSec) {
        val totalNotes = allNotes.size
        durationSec.toFloat() / totalNotes
    }

    var currentIndex by remember { mutableStateOf(0) }
    val offsetX = remember { Animatable(0f) }

    // 음마다 offset → animateTo로 왼쪽 이동 → 인덱스 증가 후 snapTo(0)
    LaunchedEffect(allNotes) {
        while (currentIndex < allNotes.size - 1) {
            offsetX.animateTo(
                targetValue = -blockWidthPx,
                animationSpec = tween(
                    durationMillis = (durationPerNoteSec * 1000).toInt(),
                    easing = LinearEasing
                )
            )

            currentIndex++
            offsetX.snapTo(0f)// 다음 움직임을 위한 준비
        }
    }

    val visibleNotes = allNotes.drop(currentIndex).take(6)

    Box(modifier = modifier.height(blockHeight).clipToBounds()) {
        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.toInt(), 0) } // 현재 5개를 왼쪽으로 움직임
                .padding(start = screenWidth * 0.12f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)

        ) {
            visibleNotes.forEachIndexed() { index, chord ->
                val alpha = remember(currentIndex) {
                    Animatable(if (index === 5) 0f else 1f)
                }

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
                }

                Box(
                    modifier = Modifier
                        .size(blockWidth, blockHeight)
                        .graphicsLayer { this.alpha = alpha.value }
                        .background(
                            if (chord == "X") Color.Transparent else Color(0xFFE3CBA5),
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (chord != "X") {
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
}
