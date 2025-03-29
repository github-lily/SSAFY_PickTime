package com.example.picktimeapp.ui.mypage


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.picktimeapp.ui.theme.Brown20
import com.example.picktimeapp.ui.theme.Brown40
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.picktimeapp.ui.theme.Gray30
import com.example.picktimeapp.ui.theme.Gray50
import com.example.picktimeapp.ui.theme.Gray70

@Composable
fun EditPasswordScreen(
    navController: NavController,
    originalPassword: String, // 기존 비밀번호 받아오기
    viewModel: EditPasswordViewModel = hiltViewModel()
    ) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val isPasswordSame = newPassword == confirmPassword || confirmPassword.isEmpty()
    var triedToSave by remember { mutableStateOf(false) } // 저장 눌러야 에러메세지 확인가능

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PasswordInputField(
            label = "새 비밀번호",
            password = newPassword,
            onPasswordChange = {newPassword = it}
        )
        Spacer(modifier = Modifier.height(30.dp))
        Column (
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.width(600.dp)
        ){
            PasswordInputField(
                label = "새 비밀번호 확인",
                password = confirmPassword,
                onPasswordChange = {confirmPassword = it}
            )
//            PasswordMismatchMessage(isPasswordSame)
            if(triedToSave) {
                PasswordMismatchMessage(
                    newPassword = newPassword,
                    confirmPassword = confirmPassword,
                    originalPassword = originalPassword
                )
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        ActionButtons(
            navController = navController,
            password = newPassword,
            isPasswordSame = isPasswordSame,
            onSaveClick = { password ->
                triedToSave = true
                if (isPasswordSame && password.isNotBlank() && password != originalPassword) {
                    viewModel.updatePassword(
                        newPassword = password,
                        onSuccess = {
                            navController.navigate("mypage") {
                                popUpTo("mypage") { inclusive = true }
                            }
                        }
                    )
                }
            }
        )
    }
}


// 첫 번째 입력란
@Composable
fun PasswordInputField (
    label: String,
    password: String,
    onPasswordChange: (String) -> Unit
){
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
//        label = {
//            Text(
//                label,
//                fontSize = 40.sp,
//                color = Gray50
//                ) },
        placeholder = { Text(text = label, fontSize = 30.sp, color = Gray50) },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(600.dp)
            .height(100.dp)
            .background(Color.White),
//            .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Brown40,
            unfocusedBorderColor = Gray30,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = Brown40
        ),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 40.sp,
            color = Gray70
        )
    )
}

// 에러메세지
@Composable
//fun PasswordMismatchMessage(
//    isPasswordSame: Boolean
//){
//    if(!isPasswordSame){
//        Text(
//            text = "비밀번호가 일치하지 않습니다.",
//            color = Color.Red,
//            style = MaterialTheme.typography.bodySmall,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 20.dp, start = 4.dp),
//            textAlign = TextAlign.Start
//        )
//    }
//}
fun PasswordMismatchMessage(
    newPassword: String,
    confirmPassword: String,
    originalPassword: String
) {
    val message = when {
        newPassword.isBlank() || confirmPassword.isBlank() ->
            "비밀번호를 입력해주세요."

        newPassword != confirmPassword ->
            "비밀번호가 일치하지 않습니다."

        newPassword == originalPassword ->
            "기존 비밀번호와 동일합니다."

        else -> null
    }

    message?.let {
        Text(
            text = it,
            color = Color.Red,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 4.dp),
            textAlign = TextAlign.Start
        )
    }
}

// 버튼
@Composable
fun ActionButtons(
    navController: NavController,
    password: String,
    isPasswordSame: Boolean,
    onSaveClick: (String) -> Unit
    ){
    Row (horizontalArrangement = Arrangement.spacedBy(30.dp)){
        Button(
            onClick = { navController.navigate("mypage"){
                popUpTo("mypage") { inclusive = true }
            } },
            colors = ButtonDefaults.buttonColors(
                containerColor = Brown20
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .height(80.dp)
                .width(285.dp)
        ) {
            Text("취소", color = Color.Black,fontSize = 30.sp)
        }
        Button(
//            onClick = {navController.navigate("mypage"){
//                popUpTo("mypage") { inclusive = true }
//            } },
            onClick = { onSaveClick(password) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Brown40
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .height(80.dp)
                .width(285.dp)
        ){
            Text("저장", color = Color.White,fontSize = 30.sp)
        }
    }
}
