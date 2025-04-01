package com.example.picktimeapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.nav.Routes
import com.example.picktimeapp.ui.theme.*


@Composable
fun SideNavigation(navController: NavController) {

    BoxWithConstraints {
        val boxWidth = maxWidth
        val boxHeight = maxHeight

        val navWidth = boxWidth * 0.1f
        val profileSize = boxWidth * 0.07f
        val iconSize = boxWidth * 0.07f
        val imageSize = iconSize * 0.65f

        val topSpacerHeight = boxHeight * 0.02f
        val midSpacerHeight = boxHeight * 0.4f
        val smallSpacer = boxHeight * 0.02f

        Column (
            modifier = Modifier
                .fillMaxHeight()
                .width(navWidth)
                .background(Brown20),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 프로필 버튼 쪽
            Column {
                Spacer(modifier = Modifier.height(topSpacerHeight))
                ProfileNavigationButton(
                    iconResId = R.drawable.profile_level_1,
                    contentDescription = "마이페이지 이동",
                    size = profileSize,
                    onClick = {
                        navController.navigate(Routes.MYPAGE)
                    }
                )
            }

            // 다른 네비게이션 버튼들도 유사하게 수정
            Column {
                Spacer(modifier = Modifier.height(midSpacerHeight))
                IconNavigationButton(
                    iconResId = R.drawable.tuning_icon,
                    contentDescription = "튜닝페이지로 이동",
                    size = iconSize,
                    imageSize = imageSize,
                    onClick = {
                        navController.navigate(Routes.GUITAR_TUNNING)
                    }
                )
            }

            // 연습모드
            Column {
                Spacer(modifier = Modifier.height(smallSpacer))
                IconNavigationButton(
                    iconResId = R.drawable.practice_icon,
                    contentDescription = "연습모드페이지로 이동",
                    size = iconSize,
                    imageSize = imageSize,
                    onClick = {
                        navController.navigate(Routes.PRACTICE_LIST)
//                        Step4 테스트용
//                        navController.navigate("practice-test") {
//                            popUpTo(Routes.MYPAGE) { inclusive = true }

                    }
                )
            }

            // 게임모드
            Column {
                Spacer(modifier = Modifier.height(smallSpacer))
                IconNavigationButton(
                    iconResId = R.drawable.game_icon,
                    contentDescription = "게임모드페이지로 이동",
                    size = iconSize,
                    imageSize = imageSize,
                    onClick = {
                        navController.navigate(Routes.GAME) {
                            popUpTo(Routes.MYPAGE) { inclusive = true }
                        }
                    }
                )
            }
        }


    }
}


// 프로필용 버튼
@Composable
fun ProfileNavigationButton(
    iconResId: Int,
    contentDescription: String,
    size: Dp,
    onClick: () -> Unit
){
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .border(4.dp, Brown40, CircleShape)
            .clickable { onClick()},
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(size)
        )
    }

}

//아이콘 버튼
@Composable
fun IconNavigationButton(
    iconResId: Int,
    contentDescription: String,
    size: Dp,
    imageSize: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brown40)
            .clickable { onClick()},
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(imageSize)
        )
    }

}