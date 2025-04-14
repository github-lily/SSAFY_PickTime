package com.example.picktimeapp.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.picktimeapp.ui.components.BackButton
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
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    var isNicknameEmpty by remember { mutableStateOf(false) }

    // 처음 진입 시 유저 정보 가져오기
    LaunchedEffect(Unit) {
        viewModel.loadUserInfo()
    }

    BoxWithConstraints (modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val inputWidth = screenWidth * 0.3f            // 예: 입력창 너비
        val inputHeight = screenHeight * 0.1f         // 예: 입력창 높이
        val buttonWidth = screenWidth * 0.08f
        val buttonHeight = inputHeight
        val spacing = screenHeight * 0.02f

        Box(
            modifier = Modifier.fillMaxSize()
                .padding(screenWidth * 0.02f)
        ) {
            BackButton(navController = navController)
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Column(horizontalAlignment = Alignment.Start) {
                Text (
                    text = "닉네임 수정",
                    fontWeight = FontWeight.Normal,
                    fontSize = (buttonHeight.value * 0.4f).sp
                )
                Spacer(modifier = Modifier.height(spacing))

                Row(verticalAlignment = Alignment.Top) {
                    Column {
                        NicknameInputField(
                            nickname = nickname,
                            placeholder = nickname,
                            onValueChange = {
                                viewModel.onNicknameChange(it)
                                if (it.isNotBlank()) {
                                    isNicknameEmpty = false
                                }
                            },
                            width = inputWidth,
                            height = inputHeight,
                            fontSize =  (inputHeight.value * 0.4f).sp
                        )

                        if (isNicknameEmpty) {
                            Text(
                                text = "닉네임을 입력해주세요",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, start = 4.dp),
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(screenWidth * 0.03f))
                    NicknameSubmitButton(
                        nickname = nickname,
                        width = buttonWidth,
                        height = buttonHeight,
                        fontSize =  (buttonHeight.value * 0.4f).sp,
                        onSuccess = {
                            if (nickname.isBlank()) {
                                isNicknameEmpty = true
                            } else {
                                viewModel.updateNickname(nickname) {
                                    navController.navigate(Routes.MYPAGE) {
                                        popUpTo(Routes.MYPAGE) { inclusive = true }
                                    }
                                }
                            }
                        }
                    )
                }

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
}

@Composable
fun NicknameInputField (
    nickname: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    width: Dp,
    height: Dp,
    fontSize: TextUnit
    ) {
    OutlinedTextField(
        value = nickname,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                fontSize = fontSize,
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
                        modifier = Modifier.size(height * 0.3f)
                    )
                }
            }
        },
        modifier = Modifier
            .width(width)
            .height(height)
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
            fontSize = fontSize,
            color = Gray70
        )
    )
}

@Composable
fun NicknameSubmitButton(
    nickname: String,
    onSuccess: () ->  Unit,
    width: Dp,
    height: Dp,
    fontSize: TextUnit,
){
    val isEnabled = nickname.isNotBlank()

    Button(
        onClick = {
            if(isEnabled) {
                onSuccess()
            }
        },
        enabled = isEnabled,
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