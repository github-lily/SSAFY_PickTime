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
import com.example.picktimeapp.ui.guitarposition.GuitarPositionScreen
import com.example.picktimeapp.ui.game.GameModeScreen
import com.example.picktimeapp.ui.mypage.MyPageScreen
import com.example.picktimeapp.ui.mypage.MyPageViewModel
import com.example.picktimeapp.ui.mypage.EditNicknameScreen
import com.example.picktimeapp.ui.mypage.EditPasswordScreen
import com.example.picktimeapp.ui.mypage.PasswordCheckScreen

// Update Routes object
object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val MYPAGE = "mypage"
    const val EDIT_NICKNAME = "editNickname"
    const val EDIT_PASSWORD = "editPassword"
    const val PASSWORD_CHECK = "passwordCheck"
    const val GuitarPosition = "guitarposition"
    const val Game = "game"
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
                },
                onNavigateToGuitarPosition = {
                    navController.navigate(Routes.GuitarPosition) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                },
                onNavigateToGame = {
                    navController.navigate(Routes.Game) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
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
                    navController.navigate(Routes.MYPAGE) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onFindPasswordClick = { /* 구현 필요 */ },
                onSignUpClick = {
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
                    navController.popBackStack()
                },
                onSignUpClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SIGNUP) { inclusive = true }
                    }
                }
            )
        }

        // MyPage Screen
        composable(Routes.MYPAGE) {
            val viewModel: MyPageViewModel = hiltViewModel()
            MyPageScreen(viewModel = viewModel, navController = navController)
        }

        // Nickname Edit Screen
        composable(Routes.EDIT_NICKNAME) {
            EditNicknameScreen(navController)
        }

        // Password Edit Screen
        composable(Routes.EDIT_PASSWORD) {
            EditPasswordScreen(navController)
        }

        // Password Check Screen
        composable(Routes.PASSWORD_CHECK) {
            PasswordCheckScreen(navController)
        }

        // Guitar Position
        composable(Routes.GuitarPosition) {
            GuitarPositionScreen()
        }

        // Game Mode
        composable(Routes.Game) {
            GameModeScreen()
        }
    }
}