package com.example.picktimeapp.ui.tunning

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

@Composable
fun AudioVisualizerBar(
    viewModel: TuningViewModel,
    modifier: Modifier = Modifier,
    selectedIndex: Int
) {
    val frequency = viewModel.frequencyState.value
    val standardFrequencies = listOf(146.83, 110.0, 82.41, 196.0, 246.94, 329.63)

    // 고정된 전체 표시 범위: target을 중심으로 ±10Hz (총 20Hz)
    val displayRangeHz = 20.0
    val (minFreq, maxFreq) = if (selectedIndex in standardFrequencies.indices) {
        val target = standardFrequencies[selectedIndex]
        (target - displayRangeHz / 2) to (target + displayRangeHz / 2)
    } else {
        0.0 to 500.0
    }

    // 주파수 값을 0~1 범위의 fraction으로 변환
    val fraction = ((frequency - minFreq) / (maxFreq - minFreq))
        .toFloat()
        .coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 500)
    )

    // hit area: ±1Hz 고정 (각 스트링에서 동일한 비율로 표시됨)
    val hitRange = 1.0
    val (hitMinFreq, hitMaxFreq) = if (selectedIndex in standardFrequencies.indices) {
        val target = standardFrequencies[selectedIndex]
        (target - hitRange) to (target + hitRange)
    } else {
        0.0 to -1.0
    }

    val tuningBarPainter = painterResource(id = R.drawable.tunning_bar)
    val imageRatio = with(tuningBarPainter.intrinsicSize) {
        if (height > 0) width / height else 1f
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // 튜닝 바 이미지 영역
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight()
                .aspectRatio(imageRatio)
        ) {
            Image(
                painter = tuningBarPainter,
                contentDescription = null,
                modifier = Modifier.matchParentSize()
            )

            BoxWithConstraints(Modifier.matchParentSize()) {
                val canvasWidthPx = constraints.maxWidth.toFloat()
                val canvasHeightPx = constraints.maxHeight.toFloat()

                // 막대 높이 계산 (아래에서 위로 채워짐)
                val barHeightPx = canvasHeightPx * animatedFraction
                val barTopPx = canvasHeightPx - barHeightPx

                Canvas(modifier = Modifier.matchParentSize()) {
                    // hit area 그리기: 전체 표시 범위 대비 hit area의 위치 및 크기 계산
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
                            color = Color(0xFFF9D952).copy(alpha = 0.2f),
                            topLeft = Offset(x = 0f, y = topY),
                            size = Size(width = canvasWidthPx, height = rectHeight)
                        )
                    }

                    // 투명 막대: 필요시 여기에 색상을 추가 가능
                    drawRect(
                        color = Color.Transparent,
                        topLeft = Offset(x = 0f, y = barTopPx),
                        size = Size(width = canvasWidthPx, height = barHeightPx + 50)
                    )
                }

                // 기린 이미지: 막대의 윗부분에 고정 배치
                val giraffePainter = painterResource(id = R.drawable.tunning_girin)
                val giraffeTopDp = with(LocalDensity.current) { barTopPx.toDp() }
                val giraffeOffsetX = (-30).dp

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
