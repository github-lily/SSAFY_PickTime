//package com.example.picktimeapp.ui.game.play
// 시도 2 : 실패
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Row
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
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.zIndex
//
//@Composable
//fun CodeBarGroup(
//    chordBlocks: List<String>,
//    measureDuration: Int,
//    screenWidth: Dp,
//    modifier: Modifier = Modifier
//) {
//    val blockWidth = screenWidth * 0.15f // 각 하나의 코드 길이
//    val measureWidth = blockWidth * 4 // 마디 하나에 4개 코드블럭
//    val totalDistance = (blockWidth * chordBlocks.size) + screenWidth
//    val totalDuration = durationSec * 1000
//한
//    println("✅ blockWidth: $blockWidth")
//    println("✅ totalDistance: $totalDistance")
//    println("✅ totalDuration: $totalDuration")
//
//    val animatedOffset = remember { Animatable(0f) }
//
//    LaunchedEffect(durationSec) {
//        println("✅ 애니메이션 시작")
//        animatedOffset.snapTo(0f)
//        animatedOffset.animateTo(
//            targetValue = -totalDistance.value,
//            animationSpec = tween(
//                durationMillis = totalDuration,
//                delayMillis = 0
//            )
//        )
//        println("✅ 애니메이션 끝")
//    }
//
//    Box(
//        modifier = modifier
//            .width(totalDistance + screenWidth)
//            .height(40.dp)
//            .offset { IntOffset(animatedOffset.value.dp.roundToPx(), 30) }
//            .zIndex(1f)
//    ) {
//        Row (modifier = Modifier.width(totalDistance)) {
//            chordBlocks.forEach { chord ->
//                Box(
//                    modifier = Modifier
//                        .width(blockWidth)
//                        .size(blockWidth, 40.dp)
//                        .background(Color(0xFFE3CBA5), shape = MaterialTheme.shapes.medium),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = if (chord == "X") "" else chord,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 20.sp,
//                        color = Color.Black
//                    )
//                }
//            }
//        }
//    }
//}
