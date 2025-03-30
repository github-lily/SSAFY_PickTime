package com.example.picktimeapp.audio

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
    modifier: Modifier = Modifier
) {
    // 1) ViewModel에서 주파수 관찰
    val frequency = viewModel.frequencyState.value

    // 2) 시각화 범위 (예: 0Hz ~ 500Hz)
    val minFreq = 0.0
    val maxFreq = 500.0

    // 3) 0..1 사이 fraction 계산
    val fraction = ((frequency - minFreq) / (maxFreq - minFreq))
        .toFloat()
        .coerceIn(0f, 1f)

    // 4) 막대 애니메이션
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 300)
    )

    // BoxWithConstraints를 사용해 부모의 실제 크기(픽셀)를 얻음
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        // 부모의 폭/높이 (px 단위)
        val parentWidthPx = constraints.maxWidth.toFloat()
        val parentHeightPx = constraints.maxHeight.toFloat()

        // 막대 높이(px)
        val barHeightPx = parentHeightPx * animatedFraction
        // 막대 윗변 y 좌표(px) = 전체높이 - 막대높이
        val barTopPx = parentHeightPx - barHeightPx

        // (A) Canvas에 “빨간 막대” 그리기
        //     - Canvas의 size도 결국 parentWidthPx, parentHeightPx와 동일
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(x = 0f, y = barTopPx),
                size = Size(width = parentWidthPx, height = barHeightPx)
            )
        }

        // (B) 기린 이미지
        //     기린 머리가 막대 윗변(barTopPx)와 만날 수 있도록, 기린의 상단을 barTopPx에 위치시킴
        val giraffePainter = painterResource(id = R.drawable.tunning_girin)

        // barTopPx를 DP로 변환
        val giraffeTopDp = with(LocalDensity.current) { barTopPx.toDp() }
        val giraffeOffsetX = -45.dp

        // 기린의 top을 barTopDp 지점에 배치
        Image(
            painter = giraffePainter,
            contentDescription = "Giraffe with bar",
            modifier = Modifier
                .align(Alignment.TopCenter)       // 부모의 상단 중앙을 기준(anchor)으로 삼음
                .offset(x = giraffeOffsetX, y = giraffeTopDp)         // 거기서부터 아래로 barTopDp만큼 이동
        )
    }
}
