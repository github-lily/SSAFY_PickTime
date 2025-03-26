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
import com.example.picktimeapp.ui.nav.Routes
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
                    navController.navigate(Routes.MYPAGE)
                }
            )
        }

        // 다른 네비게이션 버튼들도 유사하게 수정
        Column {
            Spacer(modifier = Modifier.height(430.dp))
            IconNavigationButton(
                iconResId = R.drawable.tuning_icon,
                contentDescription = "튜닝페이지로 이동",
                onClick = {
                    navController.navigate(Routes.MYPAGE)
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
                    navController.navigate(Routes.MYPAGE) {
                        popUpTo(Routes.MYPAGE) { inclusive = true }
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
                    navController.navigate(Routes.Game) {
                        popUpTo(Routes.MYPAGE) { inclusive = true }
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