package com.example.picktimeapp.audio

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.tunning.TuningViewModel

@Composable
fun AudioVisualizerBar(
    viewModel: TuningViewModel,
    modifier: Modifier = Modifier,
    selectedIndex: Int
) {
    val frequency = viewModel.frequencyState.value
    val standardFrequencies = listOf(146.83, 110.0, 82.41, 196.0, 246.94, 329.63)

    // 동적 주파수 범위 설정
    val (minFreq, maxFreq) = if (selectedIndex in standardFrequencies.indices) {
        val h = standardFrequencies[selectedIndex]
        (h * 0.2) to (h * 1.2)
    } else {
        0.0 to 500.0
    }

    // 0..1 사이 fraction 계산 (애니메이션용)
    val fraction = ((frequency - minFreq) / (maxFreq - minFreq))
        .toFloat()
        .coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 300)
    )

    // Hit Area: ±5Hz 범위
    val hitRange = 5.0
    val (hitMinFreq, hitMaxFreq) = if (selectedIndex in standardFrequencies.indices) {
        val centerFreq = standardFrequencies[selectedIndex]
        (centerFreq - hitRange) to (centerFreq + hitRange)
    } else {
        0.0 to -1.0
    }

    val tuningBarPainter = painterResource(id = R.drawable.tunning_bar)
    val imageRatio = with(tuningBarPainter.intrinsicSize) {
        if (height > 0) width / height else 1f
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // 튜닝 바 이미지를 일정 크기로 제한 (세로 전체, 비율 유지)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight()
                .aspectRatio(imageRatio)
        ) {
            // 튜닝 바 이미지
            Image(
                painter = tuningBarPainter,
                contentDescription = null,
                modifier = Modifier.matchParentSize()
            )

            // Canvas: 이미지와 동일한 크기로 겹침
            BoxWithConstraints(Modifier.matchParentSize()) {
                val canvasWidthPx = constraints.maxWidth.toFloat()
                val canvasHeightPx = constraints.maxHeight.toFloat()

                // 막대 높이 계산 (아래에서 위로 채워짐)
                val barHeightPx = canvasHeightPx * animatedFraction
                val barTopPx = canvasHeightPx - barHeightPx

                Canvas(modifier = Modifier.matchParentSize()) {
                    // Hit Area 그리기
                    if (hitMaxFreq > hitMinFreq) {
                        val fractionLow = ((hitMinFreq - minFreq) / (maxFreq - minFreq))
                            .toFloat().coerceIn(0f, 1f)
                        val fractionHigh = ((hitMaxFreq - minFreq) / (maxFreq - minFreq))
                            .toFloat().coerceIn(0f, 1f)
                        val yLowPx = canvasHeightPx * (1f - fractionLow)
                        val yHighPx = canvasHeightPx * (1f - fractionHigh)
                        val topY = yHighPx.coerceAtMost(yLowPx)
                        val rectHeight = yLowPx - topY

                        drawRect(
                            // 변경된 색상: #F9D952 → 0xFFF9D952
                            color = Color(0xFFF9D952).copy(alpha = 0.2f),
                            topLeft = Offset(x = 0f, y = topY),
                            size = Size(width = canvasWidthPx, height = rectHeight)
                        )
                    }

                    // 막대 (투명 처리; 필요시 색상 변경 가능)
                    drawRect(
                        color = Color.Transparent,
                        topLeft = Offset(x = 0f, y = barTopPx),
                        size = Size(width = canvasWidthPx, height = barHeightPx)
                    )
                }

                // 기린 이미지 배치: 막대의 윗변에 맞춰 움직임
                val giraffePainter = painterResource(id = R.drawable.tunning_girin)
                val giraffeTopDp = with(LocalDensity.current) { barTopPx.toDp() }
                val giraffeOffsetX = (-45).dp

                Image(
                    painter = giraffePainter,
                    contentDescription = "Giraffe with bar",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = giraffeOffsetX, y = giraffeTopDp)
                )
            }
        }
    }
}
