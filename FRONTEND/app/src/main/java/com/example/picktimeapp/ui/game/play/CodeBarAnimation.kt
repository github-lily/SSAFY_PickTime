//package com.example.picktimeapp.ui.game.play
//시도 1 = 실패
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.size
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.delay
//
//@Composable
//fun CodeBarAnimation(
//    bpm: Int,
//    chordBlocks: List<String>,
//    screenWidth: Dp
//) {
//    val blockWidth = screenWidth * 0.15f  // 코드 바 하나 크기
//    val startOffset = screenWidth        // 오른쪽 밖에서 시작
//    val targetOffset = screenWidth * 0.2f // 흰 줄 위치 (예시)
//    val endOffset = -blockWidth // 완전히 왼쪽 바깥으로
//
//    val animatedOffset = remember { Animatable(startOffset.value) }
//
//    LaunchedEffect(chordBlocks) {
//        val beatDurationMs = (60_000 / bpm).toInt()
//
//        for (block in chordBlocks) {
//            // animate 한 박자 이동
//            animatedOffset.snapTo(startOffset.value)
//            animatedOffset.animateTo(
//                endOffset.value,
//                animationSpec = tween(durationMillis = beatDurationMs)
//            )
//            delay(beatDurationMs.toLong())  // 다음 박자를 위해 대기
//        }
//    }
//
//    // 코드 바 UI
//    Box(
//        modifier = Modifier
//            .offset { IntOffset(animatedOffset.value.dp.roundToPx(), 30) } // Y 위치는 조정 가능
//            .size(blockWidth, 40.dp)
//            .background(Color(0xFFE3CBA5), shape = MaterialTheme.shapes.medium),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = chordBlocks.firstOrNull() ?: "",
//            fontWeight = FontWeight.Bold,
//            color = Color.Black
//        )
//    }
//}