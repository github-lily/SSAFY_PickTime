package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.picktimeapp.data.model.PickDay
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp

@Composable
fun PickDaysGrid(
    pickDays: List<PickDay>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
) {

    BoxWithConstraints (modifier = modifier) {

        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val boxSize = screenWidth * 0.018f
        val textSize = screenWidth.value * 0.016f
        val columnSpacing = screenWidth * 0.002f
        val rowSpacing = screenWidth * 0.002f
        val dayLabelSpacing = screenWidth * 0.01f

        Row (
            modifier = Modifier
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ){
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                daysOfWeek.forEach {day ->
                    Text(
                        text = day,
                        fontSize = textSize.sp,
                        modifier = Modifier.height(screenHeight * 0.085f)
                    )
                }
            }
            // 요일이랑 잔디 사이 간격주기
            Spacer(modifier = Modifier.width(dayLabelSpacing))

            // 잔디 그리드 만들기 일단 45주만 출력
            val columns = List(45) { columnIndex ->
                List(7) { rowIndex ->
                    val index = columnIndex * 7 + rowIndex
                    pickDays.getOrNull(index)
                }
            }

            // 잔디 그리드 출력하기 (가로로 배치 )
            Row {
                columns.forEach { column ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(rowSpacing),
                        modifier = Modifier
                            .padding(end = columnSpacing)
                    ) {
                        column.forEach { pickDay ->
                            if (pickDay != null) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(boxSize)
                                ) {
                                    PickDayBox(
                                        pickDay = pickDay,
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(boxSize))
                            }
                        }
                    }
                }
            }
        }

    }
}

//@Composable
//fun TooltipBalloon(
//    pickDay: PickDay,
//    fontSize: TextUnit,
//    modifier: Modifier = Modifier
//) {
//    Box(
//        modifier = modifier
//            .background(
//                color = Color(0xFF996633),
//            )
//    ) {
//        Text(
//            text = "${pickDay.pickCount}회 pick - ${pickDay.completedDate}",
//            color = Color.White,
//            fontSize = fontSize,
//        )
//    }
//}
