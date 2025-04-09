package com.example.picktimeapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.picktimeapp.ui.login.LoginScreen
import com.example.picktimeapp.ui.login.LoginViewModel
import com.example.picktimeapp.ui.signup.SignupScreen
import com.example.picktimeapp.ui.signup.SignupViewModel
import com.example.picktimeapp.ui.welcome.WelcomeScreen
import com.example.picktimeapp.ui.guitarposition.GuitarPositionScreen
import com.example.picktimeapp.ui.game.GameModeScreen
import com.example.picktimeapp.ui.game.play.GamePlayScreen
import com.example.picktimeapp.ui.guitarposition.GameGuitarPositionScreen
import com.example.picktimeapp.ui.mypage.MyPageScreen
import com.example.picktimeapp.ui.mypage.MyPageViewModel
import com.example.picktimeapp.ui.mypage.EditNicknameScreen
import com.example.picktimeapp.ui.mypage.EditPasswordScreen
import com.example.picktimeapp.ui.mypage.PasswordCheckScreen
import com.example.picktimeapp.ui.practice.PracticeChordChangeScreen
import com.example.picktimeapp.ui.practice.PracticeChordCheckScreen
import com.example.picktimeapp.ui.practice.PracticeChordInfoScreen
import com.example.picktimeapp.ui.practice.PracticeChordListenScreen
import com.example.picktimeapp.ui.practice.PracticeChordPressScreen
import com.example.picktimeapp.ui.practice.PracticeListScreen
import com.example.picktimeapp.ui.practice.PracticeMusicScreen
import com.example.picktimeapp.ui.tunning.TuningScreen
import com.example.picktimeapp.ui.tunning.TuningViewModel

// Update Routes object
object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val MYPAGE = "mypage"
    const val EDIT_NICKNAME = "editNickname"
    const val EDIT_PASSWORD = "editPassword"
    const val PASSWORD_CHECK = "passwordCheck"
    const val GAME = "game"
    const val GAME_PLAY = "gameplay"
    const val GUITAR_TUNNING = "guitartunning"
    const val GAME_GUITAR_TUNNING = "gameguitartunning"

    // 연습모드
    const val PRACTICE_LIST = "practicelist"
    const val GUITAR_POSITION = "guitarposition/{stepId}"
    const val PRACTICE_CHORDINFO = "practicechordinfo"
    const val PRACTICE_CHORDCHECK = "practicechordcheck/{stepId}"
    const val PRACTICE_CHORDPRESS = "practicechordpress"
    const val PRACTICE_CHORDLISTEN = "practicechordlisten"
    const val PRACTICE_CHORDCHANGE = "practicechordchange/{stepId}"
    const val PRACTICE_MUSIC = "practicemusic/{stepId}"
    const val GAME_PLAY_WITH_ID = "game/{songId}"

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
                onNavigateToMusic = {
                    navController.navigate(Routes.PRACTICE_MUSIC) {
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

        // Password Check Screen
        composable(Routes.PASSWORD_CHECK) {
            PasswordCheckScreen(navController)
        }

        // password route edit
        composable(
            route = "editPassword/{originalPassword}"
        ) { backStackEntry ->
            val originalPassword = backStackEntry.arguments?.getString("originalPassword") ?: ""
            EditPasswordScreen(navController = navController, originalPassword = originalPassword)
        }

        // 🔥 Game Mode Position🔥
        composable(
            route = "${Routes.GAME_GUITAR_TUNNING}/{songId}",
            arguments = listOf(navArgument("songId") { type = NavType.IntType })
        ) { backStackEntry ->
            val songId = backStackEntry.arguments?.getInt("songId") ?: -1
            GameGuitarPositionScreen(navController = navController, gameId = songId)
        }




        // 🔥 Game Mode 🔥
        composable(Routes.GAME) {
            GameModeScreen(navController)
        }

        // 🔥Game Play Screen 🔥
        composable("game/{songId}") { backStackEntry ->
            val songId = backStackEntry.arguments?.getString("songId")?.toIntOrNull() ?: -1
            GamePlayScreen(navController = navController, songId = songId)
        }


// 🔥🔥🔥🔥🔥 연습모드 🔥🔥🔥🔥🔥

        // 🔥 PracticeList 🔥
        composable(Routes.PRACTICE_LIST) {
            PracticeListScreen(navController = navController)
        }

        // 🔥 Guitar Position 🔥
        composable(Routes.GUITAR_POSITION) { backStackEntry ->
            val stepId = backStackEntry.arguments?.getString("stepId")?.toIntOrNull() ?: -1
            GuitarPositionScreen(navController = navController, stepId = stepId)
        }


        // 🔥 Practice Chord Info🔥
        composable(
            route = "${Routes.PRACTICE_CHORDINFO}/{stepId}",
            arguments = listOf(navArgument("stepId") {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val stepId = backStackEntry.arguments?.getInt("stepId") ?: -1
            PracticeChordInfoScreen(navController = navController, stepId = stepId)
        }

        // 🔥 Practice Chord Check🔥
        composable(
            route = "${Routes.PRACTICE_CHORDCHECK}",
            arguments = listOf(navArgument("stepId") {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val stepId = backStackEntry.arguments?.getInt("stepId") ?: -1
            PracticeChordCheckScreen(navController = navController, stepId = stepId)
        }

        // 🔥 Practice Chord Press🔥
        composable(
            route = "${Routes.PRACTICE_CHORDPRESS}/{stepId}",
            arguments = listOf(navArgument("stepId") {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val stepId = backStackEntry.arguments?.getInt("stepId") ?: -1
            PracticeChordPressScreen(navController = navController, stepId = stepId)
        }

        // 🔥 Practice Chord Change 🔥
        composable(
            route = "${Routes.PRACTICE_CHORDCHANGE}",
            arguments = listOf(navArgument("stepId") {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val stepId = backStackEntry.arguments?.getInt("stepId") ?: -1
            PracticeChordChangeScreen(stepId = stepId, navController = navController)
        }



        // 🔥 Practice Chord Listen🔥
        composable(
            route = "${Routes.PRACTICE_CHORDLISTEN}/{stepId}",
            arguments = listOf(navArgument("stepId") {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val stepId = backStackEntry.arguments?.getInt("stepId") ?: -1
            PracticeChordListenScreen(navController = navController, stepId = stepId)
        }

        // 🔥 Practice Music 🔥
        composable(
            route = Routes.PRACTICE_MUSIC,
            arguments = listOf(navArgument("stepId") {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val stepId = backStackEntry.arguments?.getInt("stepId") ?: -1
            PracticeMusicScreen(stepId = stepId, navController = navController)
        }
//        composable(Routes.PRACTICE_MUSIC) { backStackEntry ->
//            val stepId = backStackEntry.arguments?.getString("stepId")?.toIntOrNull() ?: -1
//            PracticeMusicScreen(stepId = stepId, navController = navController)
//        }

        // 연습모드 API test용
        composable("practice-test") {
            PracticeMusicScreen(stepId = 4, navController = navController)
        }


        // 🔥 Tunning Mode 🔥
        composable(Routes.GUITAR_TUNNING) {
            val viewModel: TuningViewModel = hiltViewModel()
            TuningScreen(viewModel =  viewModel, navController = navController)
        }

    }
}