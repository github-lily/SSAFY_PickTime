package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.picktimeapp.ui.components.BackButton
import com.example.picktimeapp.ui.theme.Brown40
import com.example.picktimeapp.ui.theme.Gray30
import com.example.picktimeapp.ui.theme.Gray50
import com.example.picktimeapp.ui.theme.Gray70

@Composable
fun PasswordCheckScreen(
    navController: NavController, // 다음 화면 이동을 위한 코드
    viewModel: PasswordCheckViewModel = hiltViewModel()
) {
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints (
        modifier = Modifier.fillMaxSize()
    ){
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val inputWidth = screenWidth * 0.3f
        val inputHeight = screenHeight * 0.1f
        val buttonWidth = screenWidth * 0.08f
        val buttonHeight = inputHeight
        val spacing = screenHeight * 0.02f
        val fontSize = (inputHeight.value * 0.4f).sp
        val titleFontSize = (inputHeight.value * 0.5f).sp

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(screenWidth * 0.02f)) {
            BackButton(navController = navController)
        }
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "비밀번호 확인",
                    fontWeight = FontWeight.Normal,
                    fontSize = (buttonHeight.value * 0.4f).sp
                )
                Spacer(modifier = Modifier.height(spacing))

                //텍스트 필드랑 확인버튼은 가로배치
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    PasswordInputField(
                        password = password,
                        onValueChange = { password = it },
                        width = inputWidth,
                        height = inputHeight,
                        fontSize = (fontSize * 0.7f))

                    Spacer(modifier = Modifier.width(screenWidth * 0.03f))

                    PasswordSubmitButton(
                        password = password,
                        width = buttonWidth,
                        height = buttonHeight,
                        fontSize = fontSize,
                        onCheckPassword = { inputPassword ->
                            viewModel.checkPassword(
                                password = inputPassword,
                                onSuccess = {
                                    errorMessage = null
                                    navController.navigate("editPassword/$password")
                                },
                                onFailure = {
                                    errorMessage = "비밀번호가 일치하지 않습니다."
                                }
                            )
                        }
                    )
                }

                //비밀번호가 일치하지 않을 경우
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = (fontSize * 0.8f)
                    )
                }
            }
        }


    }
    }

@Composable
// string -> Unit 은 string으로 받는데 Unit(아무것도 반환하지 않음)을 의미 void랑 비슷
//왜 아무것도 반환하지 않냐면 변경하는 것이 목적이고 새로운 값을 반환하기 보단 기존 상태를 수정하는거니까
fun PasswordInputField(
    password: String,
    onValueChange: (String) -> Unit,
    width: Dp,
    height: Dp,
    fontSize: TextUnit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password, // 현재 비밀번호 값
        onValueChange = onValueChange, //비밀번호 값 변경 시 호출되는 콜백 함수
        placeholder = {
            Text(
                text = "현재 비밀번호를 입력해주세요.",
                fontSize = fontSize,
                color = Gray50
            ) },
        singleLine = true, //줄바꿈 방지
        shape = RoundedCornerShape(12.dp), //보더레디우스
        visualTransformation = // 트루면 일반 텍스트로 표시하고 펄스면 별로 표시
            if(isPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
            val description = if (isPasswordVisible) "숨기기" else "보기"

            IconButton(
                onClick = { isPasswordVisible = !isPasswordVisible },
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = description,
                    modifier = Modifier.size(height * 0.3f))
            }
        },
        modifier = Modifier
            .width(width)
            .height(height)
            .background(Color.White)
            .shadow(1.dp, shape = RoundedCornerShape(12.dp)),

        // 테두리 스타일 변경
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Brown40, //포커스 되면 갈색으로 바꿈
            unfocusedBorderColor = Gray30,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = Brown40,
        ),

        //이건 사용자가 입력할 때 나오는 텍스트 스타일
        textStyle = LocalTextStyle.current.copy(
            fontSize = fontSize,
            textDecoration = null,
            color = Gray70)
    )
}

@Composable
fun PasswordSubmitButton(
    password: String,
    width: Dp,
    height: Dp,
    fontSize: TextUnit,
    onCheckPassword: (String) -> Unit
){
    Button(
        onClick = {
            onCheckPassword(password)
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = Brown40,
            contentColor = Color.White
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .height(height)
            .width(width)
    ) {
        Text("확인", fontSize = fontSize)
    }
}


@Preview(showBackground = true, widthDp = 2800, heightDp = 1752)
@Composable
fun PasswordCheckScreenPreview() {
    PasswordCheckScreen(navController = rememberNavController())
}