package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.picktimeapp.data.model.PickDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun DayOfWeek.toSundayStartIndex(): Int = when (this) {
    DayOfWeek.SUNDAY -> 0
    DayOfWeek.MONDAY -> 1
    DayOfWeek.TUESDAY -> 2
    DayOfWeek.WEDNESDAY -> 3
    DayOfWeek.THURSDAY -> 4
    DayOfWeek.FRIDAY -> 5
    DayOfWeek.SATURDAY -> 6
}

@Composable
fun PickDaysGrid(
    pickDays: List<PickDay>,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var selectedTooltipKey by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(modifier = modifier) {

        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val boxSize = screenWidth * 0.018f
        val textSize = screenWidth.value * 0.016f
        val columnSpacing = screenWidth * 0.002f
        val rowSpacing = screenWidth * 0.002f
        val dayLabelSpacing = screenWidth * 0.01f

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val pickDayMap = pickDays.associateBy { it.completedDate }

        val today = LocalDate.now()
        val startDate = pickDays.minByOrNull { it.completedDate }?.let {
            LocalDate.parse(it.completedDate, formatter)
        } ?: today

        val startPaddingCount = startDate.dayOfWeek.toSundayStartIndex()
        val startDateWithPadding = startDate.minusDays(startPaddingCount.toLong())

        val fullDates = generateSequence(startDateWithPadding) { it.plusDays(1) }
            .takeWhile { !it.isAfter(today) }
            .toList()

        val displayData = fullDates.map { date ->
            pickDayMap[date.format(formatter)]
        }

        val columns = displayData.chunked(7)

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 요일 레이블
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        fontSize = textSize.sp,
                        modifier = Modifier.height(screenHeight * 0.085f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(dayLabelSpacing))

            // 잔디 출력
            Row {
                columns.forEach { column ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(rowSpacing),
                        modifier = Modifier.padding(end = columnSpacing)
                    ) {
                        (0 until 7).forEach { i ->
                            val pickDay = column.getOrNull(i)
                            if (pickDay != null) {
                                key(pickDay.completedDate) {
                                    PickDayBoxWithPopup(
                                        pickDay = pickDay,
                                        boxSize = boxSize,
                                        fontSize = (screenWidth.value * 0.014f).sp,
                                        isVisible = selectedTooltipKey == pickDay.completedDate,
                                        onClick = {
                                            selectedTooltipKey =
                                                if (selectedTooltipKey == pickDay.completedDate) null
                                                else pickDay.completedDate
                                        }
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


        // 팝업을 고정 위치에 띄우기
        val selectedPickDay = pickDays.find { it.completedDate == selectedTooltipKey }
        if (selectedPickDay != null) {
            Popup(
                offset = IntOffset(x = 1700, y = -100),
                properties = PopupProperties(focusable = false)
            ) {
                TooltipBalloon(
                    pickDay = selectedPickDay,
                    fontSize = (screenWidth.value * 0.014f).sp,
                    modifier = Modifier.widthIn(min = 120.dp)
                )
            }
        }
    }
}

@Composable
fun PickDayBoxWithPopup(
    pickDay: PickDay,
    boxSize: Dp,
    fontSize: TextUnit,
    isVisible: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(boxSize)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }
    ) {
        PickDayBox(pickDay = pickDay)
    }
}

@Composable
fun TooltipBalloon(
    pickDay: PickDay,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color = Color(0xFF996633), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = "${pickDay.pickCount}회 pick ${pickDay.completedDate}",
            color = Color.White,
            fontSize = fontSize,
            lineHeight = fontSize * 1.3
        )
    }
}
