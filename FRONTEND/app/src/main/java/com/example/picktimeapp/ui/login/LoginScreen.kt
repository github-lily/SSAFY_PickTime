package com.example.picktimeapp.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.example.picktimeapp.ui.theme.*
import com.example.picktimeapp.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.picktimeapp.ui.login.LoginViewModel


@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginClick: () -> Unit,
    onFindPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    // 상태 관리
    val email = viewModel.email.value
    val password = viewModel.password.value
    val isPasswordVisible = viewModel.isPasswordVisible.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 로고
        Text(
            text = "피크 타임",
            fontFamily = com.example.picktimeapp.ui.TitleFont,
            fontWeight = FontWeight.Medium,
            fontSize = 70.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 👉 이메일 입력창
        EmailInputField(
            email = email,
            onEmailChange = { viewModel.onEmailChanged(it) }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 👉 비밀번호 입력창
        PasswordInputField(
            password = password,
            onPasswordChange = { viewModel.onPasswordChanged(it) },
            isPasswordVisible = isPasswordVisible,
            onVisibilityToggle = { viewModel.togglePasswordVisibility() }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 👉 로그인 버튼
        LoginButton(
            onClick = {
                viewModel.login()
                onLoginClick() // 성공 시 다음 화면으로 전환하고 싶다면 여기에 조건 추가
            },
            enabled = email.isNotBlank() && password.isNotBlank()
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 👉 하단 텍스트 버튼
        LoginFooterButtons(
            onFindPasswordClick = onFindPasswordClick,
            onSignUpClick = onSignUpClick
        )
    }
}


// ✅ Composable 함수
@Composable
fun EmailInputField(
    email: String,
    onEmailChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(0.4f)) {
        Text(
            text = "이메일",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray70
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textStyle = MaterialTheme.typography.bodySmall,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Gray30,
                focusedBorderColor = Brown40,
                cursorColor = Brown40,
                unfocusedTextColor = Gray90,
                focusedTextColor = Gray90,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            placeholder = {
                Text(
                    text = "picktime@guitar.com",
                    color = Gray50,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            trailingIcon = {
                IconButton(onClick = { onEmailChange("") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clear), // ❗ X 버튼 아이콘 준비 필요
                        contentDescription = "Clear Email",
                        tint = Gray50
                    )
                }
            }
        )
    }
}


@Composable
fun PasswordInputField(
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onVisibilityToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(0.4f)) {
        Text(
            text = "비밀번호",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray70
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            textStyle = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Gray30,
                focusedBorderColor = Brown40,
                cursorColor = Brown40,
                unfocusedTextColor = Gray90,
                focusedTextColor = Gray90,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            placeholder = {
                Text(
                    text = "비밀번호를 입력하세요",
                    color = Gray50,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            trailingIcon = {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                        tint = Gray50
                    )
                }
            }
        )
    }
}
@Composable
fun LoginButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.4f)
            .padding(horizontal = 20.dp)
            .height(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Brown60,
            contentColor = DarkGreen10,
            disabledContainerColor = Brown40,
            disabledContentColor = Gray50
        ),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled
    ) {
        Text(
            text = "로그인",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray10,
            fontWeight = FontWeight.Medium

        )
    }
}

@Composable
fun LoginFooterButtons(
    onFindPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = onFindPasswordClick) {
            Text(
                text = "비밀번호 찾기",
                style = MaterialTheme.typography.bodySmall,
                color = Gray70
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "|",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Thin,
            color = Gray50
        )

        Spacer(modifier = Modifier.width(8.dp))

        TextButton(onClick = onSignUpClick) {
            Text(
                text = "회원가입",
                style = MaterialTheme.typography.bodySmall,
                color = Gray70
            )
        }
    }
}

