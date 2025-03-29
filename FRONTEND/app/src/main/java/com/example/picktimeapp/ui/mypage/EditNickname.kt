package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.picktimeapp.ui.nav.Routes
import com.example.picktimeapp.ui.theme.Brown40
import com.example.picktimeapp.ui.theme.Gray30
import com.example.picktimeapp.ui.theme.Gray50
import com.example.picktimeapp.ui.theme.Gray70
import com.example.picktimeapp.ui.theme.Gray90

@Composable
fun EditNicknameScreen (
    navController: NavController,
    viewModel: EditNicknameViewModel = hiltViewModel()
) {
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    // 처음 진입 시 유저 정보 가져오기
    LaunchedEffect(Unit) {
        viewModel.loadUserInfo()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(horizontalAlignment = Alignment.Start) {
            EditNicknameTitle()
            Spacer(modifier = Modifier.height(20.dp))

            Row (
                verticalAlignment = Alignment.CenterVertically
            )
            {
                NicknameInputField(
                    nickname = nickname,
                    placeholder = nickname,
                    onValueChange = {viewModel.onNicknameChange(it)}
                )
                Spacer(modifier = Modifier.width(30.dp))
                NicknameSubmitButton(
                    nickname = nickname,
                    // 현재 페이지를 스택에서 제거 후 전 단계로 이동하겠다.
                    onSuccess = {
                        viewModel.updateNickname(nickname) {
//                            navController.popBackStack()
                            navController.navigate(Routes.MYPAGE)
                        }
                    }
                )
            }

            // 에러 메시지 보여주기
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 20.sp
                )
            }

        }
    }
}

// 제목
@Composable
fun EditNicknameTitle (){
    Text(
        text = "닉네임 수정",
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp
    )
}

@Composable
fun NicknameInputField (nickname: String, placeholder: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = nickname,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 40.sp,
                color = Gray50
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        trailingIcon = {
            if (nickname.isNotEmpty()) {
                // 만약 어떠한 값을 입력했다면 해당 아이콘을 누르면 없애겠다.
                IconButton(onClick = { onValueChange("")}) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "입력 값 지우기",
                        tint = Gray90,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        modifier = Modifier
            .width(600.dp)
            .height(100.dp)
            .background(Color.White)
            .shadow(1.dp, shape = RoundedCornerShape(12.dp)),

        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Brown40,
            unfocusedBorderColor = Gray30,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = Brown40,
        ),

        textStyle = LocalTextStyle.current.copy(
            fontSize = 40.sp,
            color = Gray70
        )
    )
}

@Composable
fun NicknameSubmitButton(
    nickname: String,
    onSuccess: () ->  Unit
){
    Button(
        onClick = {
            if(nickname.isNotBlank()) {
                onSuccess()
            }else {
                // 나중에 백이랑 통신연결할 때 바꿀것!
                onSuccess()
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