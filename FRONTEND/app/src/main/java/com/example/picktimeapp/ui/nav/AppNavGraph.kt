package com.example.picktimeapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.picktimeapp.ui.login.LoginScreen
import com.example.picktimeapp.ui.login.LoginViewModel
import com.example.picktimeapp.ui.signup.SignupScreen
import com.example.picktimeapp.ui.signup.SignupViewModel
import com.example.picktimeapp.ui.welcome.WelcomeScreen

// Update Routes object
object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val MYPAGE = "mypage"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.WELCOME) {
        // Welcome Screen
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Routes.SIGNUP) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                },
                onNavigateToMyPage = {
                    navController.navigate(Routes.MYPAGE) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(Routes.LOGIN) {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginClick = {
                    // 로그인 성공 시 마이페이지로 이동
                    navController.navigate(Routes.MYPAGE) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onFindPasswordClick = {
                    // 비밀번호 찾기 화면으로 이동 (필요시 구현)
                },
                onSignUpClick = {
                    // 회원가입 화면으로 이동
                    navController.navigate(Routes.SIGNUP)
                }
            )
        }

        // Signup Screen
        composable(Routes.SIGNUP) {
            val viewModel: SignupViewModel = hiltViewModel()
            SignupScreen(
                viewModel = viewModel,
                onLoginClick = {
                    // 로그인 화면으로 돌아가기
                    navController.popBackStack()
                },
                onSignUpClick = {
                    // 회원가입 성공 시 로그인 화면으로 이동
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SIGNUP) { inclusive = true }
                    }
                }
            )
        }

        // MyPage Screen
        composable(Routes.MYPAGE) {
            com.example.picktimeapp.ui.nav.MyNavGraph()
        }
    }
}