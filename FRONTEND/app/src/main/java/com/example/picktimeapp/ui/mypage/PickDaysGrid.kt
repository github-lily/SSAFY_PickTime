package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.picktimeapp.data.model.PickDay
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp

@Composable
fun PickDaysGrid(
    pickDays: List<PickDay>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp, start = 8.dp)
) {
    Row (
        modifier = Modifier
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.Top
    ){
        Column (
//            verticalArrangement = Arrangement.spacedBy(2.dp),
            // 가운데 정렬
            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier.padding(top = 2.dp)
        ) {
            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            daysOfWeek.forEach {day ->
                Text(
                    text = day,
                    fontSize = 8.sp,
                    modifier = Modifier.height(20.dp)

                )
            }
        }
        // 요일이랑 잔디 사이 간격주기
        Spacer(modifier = Modifier.width(8.dp))

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
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier.padding(end = 1.dp)
                ) {
                    column.forEach { pickDay ->
                        if (pickDay != null) {
                            PickDayBox(pickCount = pickDay.pickCount)
                        } else {
                            Spacer(modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PickDaysGridPreview() {
    val dummyData = listOf(
        PickDay("2025-03-01", 0),
        PickDay("2025-03-02", 2),
        PickDay("2025-03-03", 1),
        PickDay("2025-03-04", 0),
        PickDay("2025-03-05", 3),
        PickDay("2025-03-06", 2),
        PickDay("2025-03-07", 0),
        PickDay("2025-03-08", 1),
        PickDay("2025-03-09", 2),
        PickDay("2025-03-10", 3),
        PickDay("2025-03-11", 0),
        PickDay("2025-03-12", 0),
        PickDay("2025-03-13", 1),
        PickDay("2025-03-14", 2)
    )

    PickDaysGrid(pickDays = dummyData)
}