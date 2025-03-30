package com.example.picktimeapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.picktimeapp.ui.theme.TitleFont
import com.example.picktimeapp.ui.theme.Gray90
import androidx.compose.ui.unit.coerceAtLeast
import com.example.picktimeapp.R
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeTopBar(
    titleText: String = "코드연습",
    onPauseClick: () -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val topBarHeight = (screenHeight * 0.1f).coerceAtLeast(64.dp)
        val titleFontSize = (screenWidth * 0.025f).value.sp
        val iconButtonSize = (screenHeight * 0.09f).coerceIn(48.dp, 80.dp)
        val iconSize = iconButtonSize * 0.7f

        CenterAlignedTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(topBarHeight),
            title = {
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = titleText,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Medium,
                        fontFamily = TitleFont,
                        color = Gray90
                    )
                }
            },
            actions = {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = topBarHeight * 0.15f, end = screenWidth * 0.02f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    IconButton(
                        onClick = onPauseClick,
                        modifier = Modifier.size(iconButtonSize)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.pause_btn),
                            contentDescription = "Pause",
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        )
    }
}
