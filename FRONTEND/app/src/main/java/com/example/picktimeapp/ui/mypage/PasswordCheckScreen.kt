package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.picktimeapp.ui.theme.Brown40
import com.example.picktimeapp.ui.theme.DarkGreen10
import com.example.picktimeapp.ui.theme.Gray30
import com.example.picktimeapp.ui.theme.Gray50
import com.example.picktimeapp.ui.theme.Gray70
import com.example.picktimeapp.ui.theme.Pretendard

@Composable
fun PasswordCheckScreen(
    navController: NavController // 다음 화면 이동을 위한 코드
) {
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(horizontalAlignment = Alignment.Start) {
            PasswordTitle()
            Spacer(modifier = Modifier.height(20.dp))

            //텍스트 필드랑 확인버튼은 가로배치
            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                PasswordInputField(password) { password = it }
                Spacer(modifier = Modifier.width(30.dp))
                PasswordSubmitButton(
                    password = password,
                    onSuccess = { navController.navigate("editPassword")},
                    onError = { errorMessage = it}
                )
            }

            //비밀번호가 일치하지 않을 경우
            errorMessage?.let {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 30.sp
                )
            }
        }
    }
    }


@Composable
fun PasswordTitle() {
    Text(
        text = "비밀번호 확인",
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp
    )
}

@Composable
// string -> Unit 은 string으로 받는데 Unit(아무것도 반환하지 않음)을 의미 void랑 비슷
//왜 아무것도 반환하지 않냐면 변경하는 것이 목적이고 새로운 값을 반환하기 보단 기존 상태를 수정하는거니까
fun PasswordInputField(password: String, onValueChange: (String) -> Unit) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password, // 현재 비밀번호 값
        onValueChange = onValueChange, //비밀번호 값 변경 시 호출되는 콜백 함수
        placeholder = {
            Text(
                text = "현재 비밀번호 입력 qq",
                fontSize = 40.sp,
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
                    modifier = Modifier.size(32.dp))
            }
        },
        modifier = Modifier
            .width(600.dp)
            .height(100.dp)
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
            fontSize = 40.sp,
            textDecoration = null,
            color = Gray70)
    )
}

@Composable
fun PasswordSubmitButton(
    password: String,
    onSuccess: () -> Unit,
    onError: (String?) -> Unit
){
    Button(
        onClick = {
            if (password == "qq") {
                onError(null)
                onSuccess()
            } else {
                onError("비밀번호가 일치하지 않습니다.")
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Brown40,
            contentColor = Color.White
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .height(100.dp)
            .width(150.dp)
    ) {
        Text("확인", fontSize = 40.sp)
    }
}


@Preview(showBackground = true, widthDp = 2800, heightDp = 1752)
@Composable
fun PasswordCheckScreenPreview() {
    PasswordCheckScreen(navController = rememberNavController())
}