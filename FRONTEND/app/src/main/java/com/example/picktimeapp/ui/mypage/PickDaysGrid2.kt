//package com.example.picktimeapp.ui.mypage
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.BoxWithConstraints
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import com.example.picktimeapp.data.model.PickDay
//import androidx.compose.material3.Text
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.TextUnit
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import java.time.DayOfWeek
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//
//// 🔧 요일 인덱스를 Sunday 시작 기준으로 맞추는 확장 함수
//fun DayOfWeek.toSundayStartIndex(): Int = when (this) {
//    DayOfWeek.SUNDAY -> 0
//    DayOfWeek.MONDAY -> 1
//    DayOfWeek.TUESDAY -> 2
//    DayOfWeek.WEDNESDAY -> 3
//    DayOfWeek.THURSDAY -> 4
//    DayOfWeek.FRIDAY -> 5
//    DayOfWeek.SATURDAY -> 6
//}
//@Composable
//fun PickDaysGrid(
//    pickDays: List<PickDay>,
//    modifier: Modifier = Modifier.fillMaxWidth()
//) {
//    BoxWithConstraints(modifier = modifier) {
//
//        val screenWidth = maxWidth
//        val screenHeight = maxHeight
//
//        val boxSize = screenWidth * 0.018f
//        val textSize = screenWidth.value * 0.016f
//        val columnSpacing = screenWidth * 0.002f
//        val rowSpacing = screenWidth * 0.002f
//        val dayLabelSpacing = screenWidth * 0.01f
//
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//        val pickDayMap = pickDays.associateBy { it.completedDate }
//
//        val today = LocalDate.now()
//        val startDate = pickDays.minByOrNull { it.completedDate }?.let {
//            LocalDate.parse(it.completedDate, formatter)
//        } ?: today
//
//        // 일요일 시작 기준으로 패딩
//        val startPaddingCount = startDate.dayOfWeek.toSundayStartIndex()
//        val startDateWithPadding = startDate.minusDays(startPaddingCount.toLong())
//
//        // 날짜 리스트 생성 (시작 ~ 오늘)
//        val fullDates = generateSequence(startDateWithPadding) { it.plusDays(1) }
//            .takeWhile { !it.isAfter(today) }
//            .toList()
//
//        // PickDay 매핑
//        val displayData = fullDates.map { date ->
//            pickDayMap[date.format(formatter)]
//        }
//
//        // 7일씩 열 단위로 나누기
//        val columns = displayData.chunked(7)
//
//        // --- UI 출력 ---
//        Row(
//            modifier = Modifier.horizontalScroll(rememberScrollState()),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // 요일 레이블 (왼쪽 고정)
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
//                    Text(
//                        text = day,
//                        fontSize = textSize.sp,
//                        modifier = Modifier.height(screenHeight * 0.085f)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.width(dayLabelSpacing))
//
//            // 열(주 단위)로 잔디 출력
//            Row {
//                columns.forEach { column ->
//                    Column(
//                        verticalArrangement = Arrangement.spacedBy(rowSpacing),
//                        modifier = Modifier
//                            .padding(end = columnSpacing)
//                            .width(boxSize)
//                    ) {
//                        (0 until 7).forEach { i ->
//                            val pickDay = column.getOrNull(i)
//                            if (pickDay != null) {
//                                Box(
//                                    contentAlignment = Alignment.Center,
//                                    modifier = Modifier.size(boxSize)
//                                ) {
//                                    PickDayBox(pickDay = pickDay)
//                                }
//                            } else {
//                                Spacer(modifier = Modifier.size(boxSize))
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun TooltipBalloon(
//    pickDay: PickDay,
//    fontSize: TextUnit,
//    modifier: Modifier = Modifier
//) {
//    Box(
//        modifier = modifier
//            .background(color = Color(0xFF996633), shape = RoundedCornerShape(6.dp))
//            .padding(6.dp)
//    ) {
//        Text(
//            text = "${pickDay.pickCount}회 pick - ${pickDay.completedDate}",
//            color = Color.White,
//            fontSize = fontSize
//        )
//    }
//}


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
