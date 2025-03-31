package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.picktimeapp.data.model.PickDay
import com.example.picktimeapp.ui.theme.Gray10
import com.example.picktimeapp.ui.theme.Yellow0
import com.example.picktimeapp.ui.theme.Yellow100
import com.example.picktimeapp.ui.theme.Yellow50

@Composable
fun PickDayBox(
    pickDay: PickDay,
) {
    val color = when(pickDay.pickCount) {
        0 -> Gray10
        1 -> Yellow0
        2 -> Yellow50
        else -> Yellow100
    }
    BoxWithConstraints {
        val boxSize = maxWidth * 1.0f
        Box(
            modifier = Modifier
                .size(boxSize)
                .background(color = color)
        )
    }
}

