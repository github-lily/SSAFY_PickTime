package com.example.picktimeapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.picktimeapp.R

@Composable
fun BackButton(navController: NavController) {
    BoxWithConstraints {
        val iconSize = maxWidth * 0.03f  // 화면 너비의 3% 크기로 설정

        Image(
            painter = painterResource(id = R.drawable.back_icon),
            contentDescription = "뒤로가기",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(iconSize)
                .clickable { navController.popBackStack() }
        )
    }
}

// 사용예시
//BoxWithConstraints (modifier = Modifier.fillMaxSize()) {
//    val screenWidth = maxWidth
//
//    Box(
//        modifier = Modifier.fillMaxSize()
//            .padding(screenWidth * 0.02f)
//    ) {
//        BackButton(navController = navController)
//    }