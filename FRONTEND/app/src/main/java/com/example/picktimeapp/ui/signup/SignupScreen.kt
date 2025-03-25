package com.example.picktimeapp.ui.signup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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
import androidx.compose.ui.unit.sp
import com.example.picktimeapp.ui.signup.SignupViewModel



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

    // 회원가입 성공 시 화면 전환
    LaunchedEffect(signUpResult) {
        if (signUpResult?.isSuccessful == true) {
            onSignUpClick()
        }
    }

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
            fontFamily = com.example.picktimeapp.ui.theme.TitleFont,
            fontWeight = FontWeight.Medium,
            fontSize = 70.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 👉 닉네임 입력창
        NameInputField(
            name = name,
            onNameChange = { viewModel.onNameChanged(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 👉 이메일 입력창
        EmailInputField(
            email = email,
            onEmailChange = { viewModel.onEmailChanged(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 👉 비밀번호 입력창
        PasswordInputField(
            password = password,
            onPasswordChange = { viewModel.onPasswordChanged(it) },
            isPasswordVisible = isPasswordVisible,
            onVisibilityToggle = { viewModel.togglePasswordVisibility() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 👉 비밀번호 확인 입력창
        PasswordCheckInputField(
            passwordCheck = passwordCheck,
            onPasswordCheckChange = { viewModel.onPasswordCheckChanged(it) },
            isPasswordCheckVisible = isPasswordCheckVisible,
            onVisibilityToggle = { viewModel.togglePasswordCheckVisibility() }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 👉 회원가입 버튼
        SignUpButton(
            onClick = {
                viewModel.signup()
            },
            enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && passwordCheck.isNotBlank()
        )

        // 에러메시지
        if (viewModel.errorMessage.value != null) {
            Text(
                text = viewModel.errorMessage.value ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 👉 하단 텍스트 버튼
        LoginFooterButtons(
            onLoginClick = onLoginClick
        )
    }
}


// ✅ Composable 함수

@Composable
fun NameInputField(
    name: String,
    onNameChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(0.4f)) {
        Text(
            text = "닉네임",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray70
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
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
                    text = "기타둥둥기린이",
                    color = Gray50,
                    style = MaterialTheme.typography.bodySmall
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
fun PasswordCheckInputField(
    passwordCheck: String,
    onPasswordCheckChange: (String) -> Unit,
    isPasswordCheckVisible: Boolean,
    onVisibilityToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(0.4f)) {
        Text(
            text = "비밀번호 확인",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray70
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = passwordCheck,
            onValueChange = onPasswordCheckChange,
            textStyle = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
                    style = MaterialTheme.typography.bodySmall
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
            text = "회원가입",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray10,
            fontWeight = FontWeight.Medium

        )
    }
}

@Composable
fun LoginFooterButtons(
    onLoginClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = onLoginClick) {
            Text(
                text = "이미 계정이 있으신가요?",
                style = MaterialTheme.typography.bodySmall,
                color = Gray70
            )
        }
    }
}

