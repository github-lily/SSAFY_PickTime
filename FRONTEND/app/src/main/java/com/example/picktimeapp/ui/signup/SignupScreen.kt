package com.example.picktimeapp.ui.signup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll




@Composable
fun SignupScreen(
    viewModel: SignupViewModel,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    // 상태 관리
    val name by viewModel.name
    val email by viewModel.email
    val password by viewModel.password
    val passwordCheck by viewModel.passwordcheck
    val isPasswordVisible by viewModel.isPasswordVisible
    val isPasswordCheckVisible by viewModel.isPasswordCheckVisible

    val signUpResult = viewModel.signUpResult.value

    // 스크롤
    val scrollState = rememberScrollState()

    // 회원가입 성공 시 화면 전환
    LaunchedEffect(signUpResult) {
        if (signUpResult?.isSuccessful == true) {
            onSignUpClick()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = screenWidth * 0.05f, vertical = screenHeight * 0.05f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 로고
            Text(
                text = "피크 타임",
                fontFamily = com.example.picktimeapp.ui.theme.TitleFont,
                fontWeight = FontWeight.Medium,
                fontSize = (screenWidth * 0.09f).coerceAtMost(70.dp).value.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.07f))

            // 👉 닉네임 입력창
            NameInputField(
                name = name,
                onNameChange = { viewModel.onNameChanged(it) },screenWidth,
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

            // 👉 이메일 입력창
            EmailInputField(
                email = email,
                onEmailChange = { viewModel.onEmailChanged(it) },
                screenWidth
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

            // 👉 비밀번호 입력창
            PasswordInputField(
                password = password,
                onPasswordChange = { viewModel.onPasswordChanged(it) },
                isPasswordVisible = isPasswordVisible,
                onVisibilityToggle = { viewModel.togglePasswordVisibility() },
                screenWidth
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

            // 👉 비밀번호 확인 입력창
            PasswordCheckInputField(
                passwordCheck = passwordCheck,
                onPasswordCheckChange = { viewModel.onPasswordCheckChanged(it) },
                isPasswordCheckVisible = isPasswordCheckVisible,
                onVisibilityToggle = { viewModel.togglePasswordCheckVisibility() },
                screenWidth
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.1f))

            // 👉 회원가입 버튼
            SignUpButton(
                onClick = {
                    viewModel.signup()
                },
                enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && passwordCheck.isNotBlank(),
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )

            // 에러메시지
            if (viewModel.errorMessage.value != null) {
                Text(
                    text = viewModel.errorMessage.value ?: "",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(horizontal = screenWidth * 0.03f)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 👉 하단 텍스트 버튼
            LoginFooterButtons(
                onLoginClick = onLoginClick,
                screenWidth
            )
        }
    }
}


// ✅ Composable 함수
@Composable
fun NameInputField(
    name: String,
    onNameChange: (String) -> Unit,
    screenWidth: Dp,
) {
    Column(modifier = Modifier.width(screenWidth * 0.6f)) {
        Text(
            text = "닉네임",
            fontWeight = FontWeight.SemiBold,
            fontSize = (screenWidth * 0.04f).coerceAtMost(28.dp).value.sp,
            color = Gray70,
            modifier = Modifier.padding(start = screenWidth * 0.06f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenWidth * 0.06f),
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
                    text = "기타둥둥기린이",
                    color = Gray50,
                    fontSize = (screenWidth * 0.04f).coerceAtMost(22.dp).value.sp
                )
            },
            trailingIcon = {
                IconButton(onClick = { onNameChange("") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clear),
                        contentDescription = "Clear Name",
                        tint = Gray50
                    )
                }
            }
        )
    }
}

@Composable
fun EmailInputField(
    email: String,
    onEmailChange: (String) -> Unit,
    screenWidth: Dp
) {
    Column(modifier = Modifier.width(screenWidth * 0.6f)) {
        Text(
            text = "이메일",
            fontWeight = FontWeight.SemiBold,
            fontSize = (screenWidth * 0.04f).coerceAtMost(28.dp).value.sp,
            color = Gray70,
            modifier = Modifier.padding(start = screenWidth * 0.06f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenWidth * 0.06f),
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
                    fontSize = (screenWidth * 0.04f).coerceAtMost(22.dp).value.sp
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
    onVisibilityToggle: () -> Unit,
    screenWidth: Dp
) {
    Column(modifier = Modifier.width(screenWidth * 0.6f)) {
        Text(
            text = "비밀번호",
            fontWeight = FontWeight.SemiBold,
            fontSize = (screenWidth * 0.04f).coerceAtMost(28.dp).value.sp,
            color = Gray70,
            modifier = Modifier.padding(start = screenWidth * 0.06f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            textStyle = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenWidth * 0.06f),
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
                    text = "8자 이상, 숫자 + 영어 조합",
                    color = Gray50,
                    fontSize = (screenWidth * 0.04f).coerceAtMost(22.dp).value.sp
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
fun PasswordCheckInputField(
    passwordCheck: String,
    onPasswordCheckChange: (String) -> Unit,
    isPasswordCheckVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    screenWidth: Dp
) {
    Column(modifier = Modifier.width(screenWidth * 0.6f)) {
        Text(
            text = "비밀번호 확인",
            fontWeight = FontWeight.SemiBold,
            fontSize = (screenWidth * 0.04f).coerceAtMost(28.dp).value.sp,
            color = Gray70,
            modifier = Modifier.padding(start = screenWidth * 0.06f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = passwordCheck,
            onValueChange = onPasswordCheckChange,
            textStyle = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenWidth * 0.06f),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (isPasswordCheckVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                    text = "비밀번호를 한번 더 입력하세요",
                    color = Gray50,
                    fontSize = (screenWidth * 0.04f).coerceAtMost(22.dp).value.sp
                )
            },
            trailingIcon = {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector = if (isPasswordCheckVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordCheckVisible) "비밀번호 숨기기" else "비밀번호 보기",
                        tint = Gray50
                    )
                }
            }
        )
    }
}

@Composable
fun SignUpButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    screenWidth: Dp,
    screenHeight: Dp

) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(screenWidth * 0.6f)
            .padding(horizontal = screenWidth * 0.06f)
            .height(screenHeight * 0.10f),
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
            text = "회원가입",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray10,
            fontWeight = FontWeight.Medium,
            fontSize = (screenWidth * 0.04f).coerceAtMost(28.dp).value.sp
        )
    }
}

@Composable
fun LoginFooterButtons(
    onLoginClick: () -> Unit,
    screenWidth : Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = screenWidth * 0.06f),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = onLoginClick) {
            Text(
                text = "이미 계정이 있으신가요?",
                style = MaterialTheme.typography.bodySmall,
                color = Gray70,
                fontSize = (screenWidth * 0.04f).coerceAtMost(20.dp).value.sp
            )
        }
    }
}

