package com.example.picktimeapp.ui.nav
//// 이 파일은 마이페이지부터 연결되는 route를 위한 파일입니다..
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.example.picktimeapp.ui.mypage.EditNicknameScreen
//import com.example.picktimeapp.ui.mypage.EditPasswordScreen
//import com.example.picktimeapp.ui.mypage.MyPageScreen
//import com.example.picktimeapp.ui.mypage.MyPageViewModel
//import com.example.picktimeapp.ui.mypage.PasswordCheckScreen
//
//@Composable
//fun MyNavGraph(navController: NavHostController){
//    NavHost(
//        navController = navController,
//        startDestination = "mypage"
//    ){
//        composable("mypage") {
//            val viewModel = hiltViewModel<MyPageViewModel>()
//            MyPageScreen(
//                viewModel = viewModel,
//                navController = navController
//            )
//        }
//
//        // 비밀번호 확인 화면
//        composable("passwordCheck") {
//            PasswordCheckScreen(navController)
//        }
//
//        // 비밀번호 변경 화면
//        composable("editPassword") {
//            EditPasswordScreen(navController)
//        }
//
//        // 닉네임 변경 화면
//        composable("editNickname") {
//            EditNicknameScreen(navController)
//
//        }
//    }
//}
//
