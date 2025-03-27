package com.example.picktimeapp.ui.welcome

import androidx.compose.runtime.Composable


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.picktimeapp.R
import com.example.picktimeapp.ui.theme.*
import com.example.picktimeapp.ui.login.LoginViewModel
import com.example.picktimeapp.ui.login.LoginScreen
import com.example.picktimeapp.ui.signup.SignupViewModel
import com.example.picktimeapp.ui.signup.SignupScreen

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateToMyPage: () -> Unit,
    onNavigateToGuitarPosition: () -> Unit,
    onNavigateToGame:() -> Unit
) {
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
                        onClick = {
                            onNavigateToLogin()
                        },
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
                        onClick = {
                            onNavigateToSignup()
                        },
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
                        onClick = {
                            onNavigateToMyPage()
                        },
                        modifier = buttonModifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brown40,
                            contentColor = DarkGreen10
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("마이페이지", fontSize = 24.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    // 기타 위치 확인용
                    Button(
                        onClick = {
                            onNavigateToGuitarPosition()
                        },
                        modifier = buttonModifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brown40,
                            contentColor = DarkGreen10
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("기타 카메라 테스트", fontSize = 24.sp, fontWeight = FontWeight.Medium)
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

