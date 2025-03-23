package com.example.picktimeapp.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

//@Composable
//fun LoginScreen(viewModel: LoginViewModel = viewModel()) {
//    val email = viewModel.email
//    val password = viewModel.password
//
//    TextField(value = email, onValueChange = viewModel::onEmailChange)
//    TextField(value = password, onValueChange = viewModel::onPasswordChange)
//    Button(onClick = { viewModel.login() }) {
//        Text("로그인")
//    }
//}


@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {}
) {

}