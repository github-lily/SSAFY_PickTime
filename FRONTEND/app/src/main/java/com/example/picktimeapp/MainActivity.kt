package com.example.picktimeapp.ui

//기본
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel



// 레이아웃
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

//image 관련
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Image
import androidx.compose.material3.ButtonDefaults

// 앱 관련
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.mypage.MyPageScreen
import com.example.picktimeapp.ui.mypage.MyPageViewModel
import com.example.picktimeapp.ui.theme.PickTimeAppTheme

// DI 관련
import dagger.hilt.android.AndroidEntryPoint

//color 관련
import com.example.picktimeapp.ui.theme.*

//user 관련
import com.example.picktimeapp.ui.login.LoginScreen
import com.example.picktimeapp.ui.login.LoginViewModel
import com.example.picktimeapp.ui.signup.SignupScreen



// font 관련
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picktimeapp.ui.signup.SignupViewModel


//font
val TitleFont = FontFamily(
    Font(R.font.hakgyoansimalrimche_regular, FontWeight.Normal),
    Font(R.font.hakgyoansimalrimche_bold, FontWeight.Bold)
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PickTimeAppTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var showMyPage by remember { mutableStateOf(false) }
    var showLogin by remember { mutableStateOf(false) }
    var showSignup by remember { mutableStateOf(false) }

    when {
        showLogin -> {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginClick = {
                    showLogin = false
                    showMyPage = true
                },
                onFindPasswordClick = { showLogin = false },
                onSignUpClick = {
                    showLogin = false
                    showSignup = true // 회원가입 화면으로 이동
                }
            )
        }

        showMyPage -> {
            val viewModel = MyPageViewModel()
            MyPageScreen(viewModel = viewModel)
        }

        showSignup -> {
            val viewModel: SignupViewModel = hiltViewModel()
            SignupScreen(
                viewModel = viewModel,
                onLoginClick = {
                    showSignup = false
                    showLogin = true
                },
                onSignUpClick = {
                    // 회원가입 완료 후 원하는 화면으로 전환 가능
                    showSignup = false
                    showLogin = true  // 로그인 화면으로
                }
            )
        }

        else -> {
            // 좌우로 나누는 Row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽 영역
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 타이틀 + 서브타이틀 묶기
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "피크 타임",
                            fontFamily = TitleFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 110.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        Text(
                            text = "즐겁고 재미있는 기타 학습",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 4.dp)
                                .offset(y = 30.dp) // 상하 간격
                        )
                    }


                    Spacer(modifier = Modifier.height(150.dp))

                    // 공통 버튼 Modifier
                    val buttonModifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(60.dp)

                    // 로그인 버튼
                    Button(
                        onClick = { showLogin = true },
                        modifier = buttonModifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brown40,
                            contentColor = DarkGreen10,
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("로그인", fontSize = 24.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 회원가입 버튼
                    Button(
                        onClick = { showSignup = true },
                        modifier = buttonModifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brown40,
                            contentColor = DarkGreen10
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("회원가입", fontSize = 24.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 마이페이지 버튼
                    Button(
                        onClick = { showMyPage = true },
                        modifier = buttonModifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brown40,
                            contentColor = DarkGreen10
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("마이페이지 확인용", fontSize = 24.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // 오른쪽 이미지 영역
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.girinduo),
                        contentDescription = "기타 치는 기린들",
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(end = 80.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PickTimeAppTheme {
        MainScreen()
    }
}