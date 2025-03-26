package com.example.picktimeapp.ui.components

import android.graphics.Paint.Align
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.theme.Brown20
import com.example.picktimeapp.ui.theme.Brown40
import com.example.picktimeapp.ui.theme.Brown60

@Composable
fun SideNavigation(navController: NavController) {
    Column (
        modifier = Modifier
            .fillMaxHeight()
            .width(180.dp)
            .background(Brown20),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 프로필 버튼 쪽
        Column {
            Spacer(modifier = Modifier.height(40.dp))
            ProfileNavigationButton(
                iconResId = R.drawable.profile_level_1,
                contentDescription = "마이페이지 이동",
                onClick = {
                    navController.navigate("mypage") {
                        //만약 네브바에서 프로필을 누르면 화면 전환하겠다.
                        // 그리고 마미페이지 앞에 화면들 앞의 페이지를 다 없애겠다.
                    popUpTo("mypage") { inclusive = true }
                    }
                }
            )
        }
        // 튜닝
        Column {
            Spacer(modifier = Modifier.height(430.dp))
            IconNavigationButton(
                iconResId = R.drawable.tuning_icon,
                contentDescription = "튜닝페이지로 이동",
                onClick = {
                    // 튜닝페이지는 이전으로 갈 수 있도록 합니다.
                    navController.navigate("mypage")
                }
            )
        }
        // 연습모드
        Column {
            Spacer(modifier = Modifier.height(40.dp))
            IconNavigationButton(
                iconResId = R.drawable.practice_icon,
                contentDescription = "연습모드페이지로 이동",
                onClick = {
                    navController.navigate("mypage") {
                        popUpTo("mypage") { inclusive = true }
                    }
                }
            )
        }
        // 게임모드

        Column {
            Spacer(modifier = Modifier.height(40.dp))
            IconNavigationButton(
                iconResId = R.drawable.game_icon,
                contentDescription = "게임모드페이지로 이동",
                onClick = {
                    navController.navigate("mypage") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

// 프로필용 버튼
@Composable
fun ProfileNavigationButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit
){
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(4.dp, Brown40, CircleShape)
            .clickable { onClick()},
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(120.dp)
        )
    }

}

//아이콘 버튼
@Composable
fun IconNavigationButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Brown40)
            .clickable { onClick()},
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(80.dp)
        )
    }

}