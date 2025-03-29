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
import com.example.picktimeapp.ui.game.play.GamePlayScreen
import com.example.picktimeapp.ui.mypage.MyPageScreen
import com.example.picktimeapp.ui.mypage.MyPageViewModel
import com.example.picktimeapp.ui.mypage.EditNicknameScreen
import com.example.picktimeapp.ui.mypage.EditPasswordScreen
import com.example.picktimeapp.ui.mypage.PasswordCheckScreen
import com.example.picktimeapp.ui.practice.PracticeChordInfoScreen
import com.example.picktimeapp.ui.practice.PracticeStep4Screen
import com.example.picktimeapp.ui.tunning.TunningScreen

// Update Routes object
object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val MYPAGE = "mypage"
    const val EDIT_NICKNAME = "editNickname"
    const val EDIT_PASSWORD = "editPassword"
    const val PASSWORD_CHECK = "passwordCheck"
    const val GUITAR_POSITION = "guitarposition"
    const val GAME = "game"
    const val GAME_PLAY = "gameplay"
    const val GUITAR_TUNNING = "guitartunning"
    const val PRACTICE_CHORDINFO = "practicechordinfo"
    const val PRACTICE_STEP_4 = "practice/{stepId}"

}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.WELCOME) {
        // 🔥 Welcome Screen 🔥
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
                    navController.navigate(Routes.GUITAR_POSITION) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                },
                onNavigateToStep4 = {
                    navController.navigate(Routes.PRACTICE_STEP_4) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                },
                onNavigateToChordInfo = {
                    navController.navigate(Routes.PRACTICE_CHORDINFO) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                },
            )
        }


        //🔥 Login Screen 🔥
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

        //🔥 Signup Screen 🔥
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

        //🔥 MyPage Screen 🔥
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

        // 🔥 Guitar Position 🔥
        composable(Routes.GUITAR_POSITION) {
            GuitarPositionScreen()
        }


        // 🔥 Game Mode 🔥
        composable(Routes.GAME) {
            GameModeScreen(navController)
        }

        // 🔥Game Play Screen 🔥
        composable(Routes.GAME_PLAY){
            GamePlayScreen(navController)
        }
        // 뭐 받아올 때 예시코드 --지우지 마시오!!!--
//        composable("${Routes.GAME_PLAY}/{title}") { backStackEntry ->
//            val title = backStackEntry.arguments?.getString("title") ?: ""
//            GamePlayScreen(navController = navController, title = title)
//        }

        // 🔥 Practice step4 Mode 🔥
        composable(Routes.PRACTICE_STEP_4) { backStackEntry ->
            val stepId = backStackEntry.arguments?.getString("stepId")?.toIntOrNull() ?: -1
            PracticeStep4Screen(stepId = stepId)
        }

        // 연습모드 API test용
        composable("practice-test") {
            PracticeStep4Screen(stepId = 4)
        }


        // 🔥 Practice Chord Info🔥
        composable(Routes.PRACTICE_CHORDINFO) {
            PracticeChordInfoScreen(navController)
        }



        // 🔥 Tunning Mode 🔥
        composable(Routes.GUITAR_TUNNING) {
            TunningScreen()
        }

    }
}