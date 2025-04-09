//package com.example.picktimeapp.ui.game.play
//시도 3: 실패
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.unit.Dp
//import com.example.picktimeapp.data.model.ChordMeasure
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//@Composable
//fun CodeBarSlider(
//    screenWidth: Dp,
//    durationSec: Int,
//    chordProgression: List<ChordMeasure>,
//    modifier: Modifier = Modifier
//) {
//    val blockWidth = screenWidth * 0.15f
//    val totalBlockCount = chordProgression.sumOf { it.chordBlocks.size }
//    val totalDistanceDp = blockWidth * totalBlockCount
//    val totalDurationMs = durationSec * 1000
//
//    val offsetX = remember { Animatable(0f) }
//
//    // 애니메이션 실행
//    LaunchedEffect(Unit) {
//        offsetX.snapTo(0f)
//        offsetX.animateTo(
//            targetValue = -totalDistanceDp.value,
//            animationSpec = tween(
//                durationMillis = totalDurationMs,
//                delayMillis = 0
//            )
//        )
//    }
//
//    // Row 자체에 오프셋과 클립 false 적용
//    Box(
//        modifier = modifier
//            .height(40.dp)
//            .fillMaxWidth()
//            .graphicsLayer {
//                clip = false // 자식 overflow 허용
//            }
//    ) {
//        Row(
//            modifier = Modifier
//                .width(totalDistanceDp + screenWidth) // 전체 길이 + 여유 공간
//                .offset { IntOffset(offsetX.value.dp.roundToPx(), 30) }
//                .graphicsLayer {
//                    clip = false
//                }
//        ) {
//            chordProgression.forEach { measure ->
//                measure.chordBlocks.forEach { chord ->
//                    Box(
//                        modifier = Modifier
//                            .size(blockWidth, 40.dp)
//                            .background(Color(0xFFE3CBA5), shape = MaterialTheme.shapes.medium),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = if (chord == "X") "" else chord,
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 20.sp,
//                            color = Color.Black
//                        )
//                    }
//                }
//            }
//        }
//    }
//}