package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.picktimeapp.ui.theme.DarkGreen10
import com.example.picktimeapp.ui.theme.Pretendard

@Composable
fun PasswordCheckScreen(
    navController: NavController // 다음 화면 이동을 위한 코드
) {
    //사용자가 입력하면 비밀번호 저장용
    var password by remember { mutableStateOf("") }
    // 비밀번호 틀렸을 때 처음에는 안보여줌
    var errorMessage by remember { mutableStateOf<String?>(null) }

    //일단 비밀번호 텍스트랑 하단은 세로배치하고 하단 입력쪽은 박스로 묶어서 row로 만듦.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFAF5)),
        contentAlignment = Alignment.Center
    ) {

        Column (
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "비밀번호 확인",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 60.sp,
                modifier = Modifier.padding(bottom = 20.dp)

            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                OutlinedTextField(
                    value = password, //현재 사용자가 입력한 값
                    onValueChange = { password = it }, //비밀번호 바뀔 때
                    placeholder = { Text("임시비번 node임") },
                    singleLine = true, //한 줄만 입력 가능하게 하겠다.
                    visualTransformation = PasswordVisualTransformation(), //점점점으로 표시
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .width(600.dp)
                        .height(100.dp)
                )

                Spacer(modifier = Modifier.width(30.dp))

                Button(
                    onClick = {
                        if (password == "node") {
                            errorMessage = null
                            navController.navigate("editUserInfo")
                        } else {
                            errorMessage = "비밀번호가 일치하지 않습니다"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8A5C3E), // 버튼 배경색 (Material3는 containerColor)
                        contentColor = Color.White         // 글자 색
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .height(100.dp)
                        .width(150.dp)
                ) {
                    Text("확인", fontFamily = Pretendard,fontSize = 30.sp)
                }
            }
            errorMessage?.let {
                Spacer(modifier = Modifier.height(50.dp))
                Text(it, color = Color.Red, fontSize = 30.sp)
            }


        }

    }
}


@Preview(showBackground = true, widthDp = 2800, heightDp = 1752)
@Composable
fun PasswordCheckScreenPreview() {
    PasswordCheckScreen(navController = rememberNavController())
}