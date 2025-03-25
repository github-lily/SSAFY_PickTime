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
import com.example.picktimeapp.ui.theme.Gray10
import com.example.picktimeapp.ui.theme.Yellow0
import com.example.picktimeapp.ui.theme.Yellow100
import com.example.picktimeapp.ui.theme.Yellow50

@Composable
fun PickDayBox(pickCount: Int) {
    val color = when(pickCount) {
        0 -> Gray10
        1 -> Yellow0
        2 -> Yellow50
        else -> Yellow100
    }
    Box(
        modifier = Modifier
            .size(26.dp)
            .background(color = color)
    )
}

@Preview(showBackground = true)
@Composable
fun PickDayBoxPreview(){
    PickDayBox(pickCount = 3)
}
