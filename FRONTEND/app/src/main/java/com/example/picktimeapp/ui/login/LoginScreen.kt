package com.example.picktimeapp.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picktimeapp.ui.theme.Brown40
import com.example.picktimeapp.ui.theme.Gray30
import com.example.picktimeapp.ui.theme.DarkGreen10
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.picktimeapp.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel()
) {
    val email = viewModel.email
    val password = viewModel.password

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ===== 이메일 타이틀 =====
        Text(
            text = "이메일",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp, bottom = 4.dp)
        )

        // ===== 이메일 입력창 =====
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.onEmailChange(it) },
            placeholder = { Text("picktime@guitar.com") },
            trailingIcon = {
                if (email.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onEmailChange("") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.clear),
                            contentDescription = "이메일 초기화"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Brown40,
                unfocusedBorderColor = Gray30,
                cursorColor = Brown40
            ),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ===== 비밀번호 타이틀 =====
        Text(
            text = "비밀번호",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp, bottom = 4.dp)
        )

        // ===== 비밀번호 입력창 =====
        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.onPasswordChange(it) },
            placeholder = { Text("picktime@guitar.com") },
            trailingIcon = {
                if (password.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onPasswordChange("") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.clear),
                            contentDescription = "비밀번호 초기화"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Brown40,
                unfocusedBorderColor = Gray30,
                cursorColor = Brown40
            ),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ===== 로그인 버튼 =====
        Button(
            onClick = { viewModel.login() },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Brown40,
                contentColor = DarkGreen10
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("로그인", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 하단 링크 =====
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = { /* 비밀번호 찾기 */ }) {
                Text("비밀번호 찾기", fontSize = 14.sp)
            }

            Text(" | ", fontSize = 14.sp)

            TextButton(onClick = { /* 회원가입 이동 */ }) {
                Text("회원가입", fontSize = 14.sp)
            }
        }
    }
}
