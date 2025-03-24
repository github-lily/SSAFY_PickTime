package com.example.picktimeapp.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.picktimeapp.ui.mypage.MyPageScreen
import com.example.picktimeapp.ui.mypage.MyPageViewModel
import com.example.picktimeapp.ui.mypage.PasswordCheckScreen

@Composable
fun MyNavGraph(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "mypage"
    ){
        composable("mypage") {
            val viewModel = hiltViewModel<MyPageViewModel>()
            MyPageScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        // 비밀번호 확인 화면
        composable("passwordCheck") {
            PasswordCheckScreen(navController = navController)
        }

        composable("editUserInfo") {
            // 테스트용으로 화면 구성
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("회원정보 수정 화면", fontSize = 24.sp)
            }
        }
    }
}

