package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PickDayBox(pickCount: Int) {
    val color = when(pickCount) {
        0 -> Color(0xFFF4F4F6)
        1 -> Color(0xFFFFF176)
        2 -> Color(0xFFFFD54F)
        else -> Color(0xFFFFA000)
    }
    Box(
        modifier = Modifier
            .size(18.dp)
//            .padding(1.dp)
            .background(color = color)
    )
}

@Preview(showBackground = true)
@Composable
fun PickDayBoxPreview(){
    PickDayBox(pickCount = 3)
}
